package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.bukkittemplate.command.commandRoot
import top.iseason.bukkit.bukkittemplate.utils.EasyCoolDown
import top.iseason.bukkit.bukkittemplate.utils.sendColorMessage
import top.iseason.bukkit.bukkittemplate.utils.submit
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.data.*
import java.time.LocalDateTime

fun userCommand() {
    commandRoot(
        "sakuracdk",
        alias = arrayOf("cdk", "scdk"),
        description = "使用cdk兑换礼包",
        default = PermissionDefault.TRUE,
        isPlayerOnly = true,
        async = true,
        params = arrayOf(Param("[cdk]"))
    ) {
        onExecute {
            if (EasyCoolDown.check(it, 1000)) throw ParmaException("&c你发送的太快了，请稍后再试!")
            if (!Config.isConnected) throw ParmaException("&c数据异常，请联系管理员!")
            val cdk = getParam<String>(0).trim()
            var cdkYml: BaseCDK? = null
            val player = it as Player
            val uniqueId = player.uniqueId
            var groupTemp: String?
            transaction {
                val cdkResult = CDKs.select { CDKs.id eq cdk }.limit(1).firstOrNull()
                    ?: throw ParmaException("&cCDK不存在或已过期!")
                val group = cdkResult[CDKs.group]
                groupTemp = group
                val type = cdkResult[CDKs.type]
                cdkYml = when (type) {
                    "random" -> {
                        RandomCDK.findById(group)?.toYml() ?: throw ParmaException("&cCDK不存在或已过期!")
                    }

                    "normal" -> {
                        NormalCDK.findById(group)?.toYml() ?: throw ParmaException("&cCDK不存在或已过期!")
                    }

                    else -> throw ParmaException("&cCDK不存在或已过期!")
                }
                if (cdkYml!!.checkExpire()) throw ParmaException("&cCDK已过期!")
                //检查重复领取
                if (!cdkYml!!.allowRepeat()) {
                    if (!Records.select { Records.cdk eq cdk and (Records.uid eq uniqueId) }.limit(1).empty()) {
                        throw ParmaException("&c你已经领取过该礼包了")
                    }
                    if (cdkYml is NormalCDKYml) {
                        val exist = Records.slice(Records.id.count()).select { Records.group eq group }
                            .first()[Records.id.count()]
                        if (exist > (cdkYml as NormalCDKYml).amount) throw ParmaException("&c礼包已领完!")
                    }
                }
                Record.new {
                    this.uid = uniqueId
                    this.cdk = cdk
                    this.group = groupTemp!!
                    this.acceptTime = LocalDateTime.now()
                }
                if (type == "random") {
                    CDKs.deleteWhere { CDKs.id eq cdk }
                    (cdkYml as RandomCDKYml).removeCDK(cdk)
                }
            }
            if (cdkYml == null) throw ParmaException("&cCDK不存在或已过期!")
            //发放礼品
            submit {
                if (!cdkYml!!.applyPlayer(player)) {
                    player.sendColorMessage("&c礼包已过期!")
                } else {
                    player.sendColorMessage("&aCDK兑换成功!")
                }
            }
            true
        }
    }
}
