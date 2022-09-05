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
import top.iseason.bukkit.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkit.bukkittemplate.utils.EasyCoolDown
import top.iseason.bukkit.bukkittemplate.utils.sendColorMessage
import top.iseason.bukkit.bukkittemplate.utils.submit
import top.iseason.bukkit.sakuracdk.config.Lang
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
            if (EasyCoolDown.check(it, 1000)) throw ParmaException(Lang.command__user_send_too_fast)
            if (!DatabaseConfig.isConnected) throw ParmaException(Lang.command__user_database_closed)
            val cdk = getParam<String>(0).trim()
            var cdkYml: BaseCDK? = null
            val player = it as Player
            val uniqueId = player.uniqueId
            var groupTemp: String?
            transaction {
                val cdkResult = CDKs.select { CDKs.id eq cdk }.limit(1).firstOrNull()
                    ?: throw ParmaException(Lang.command__user_cdk_unexist)
                val group = cdkResult[CDKs.group]
                groupTemp = group
                val type = cdkResult[CDKs.type]
                cdkYml = when (type) {
                    "random" -> {
                        RandomCDK.findById(group)?.toYml() ?: throw ParmaException(Lang.command__user_cdk_unexist)
                    }

                    "normal" -> {
                        NormalCDK.findById(group)?.toYml() ?: throw ParmaException(Lang.command__user_cdk_unexist)
                    }

                    else -> throw ParmaException(Lang.command__user_cdk_unexist)
                }
                if (cdkYml!!.checkExpire()) throw ParmaException(Lang.command__user_cdk_is_expire)
                //检查重复领取
                if (!cdkYml!!.allowRepeat()) {
                    if (!Records.select { Records.cdk eq cdk and (Records.uid eq uniqueId) }.limit(1).empty()) {
                        throw ParmaException(Lang.command__user_has_accepted)
                    }
                    if (cdkYml is NormalCDKYml) {
                        val exist = Records.slice(Records.id.count()).select { Records.group eq group }
                            .first()[Records.id.count()]
                        if (exist > (cdkYml as NormalCDKYml).amount) throw ParmaException(Lang.command__user_normal_brought_out)
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
            if (cdkYml == null) throw ParmaException(Lang.command__user_cdk_unexist)
            //发放礼品
            submit {
                if (!cdkYml!!.applyPlayer(player)) {
                    player.sendColorMessage(Lang.command__user_kit_is_expire)
                } else {
                    player.sendColorMessage(Lang.command__user_success)
                }
            }
            true
        }
    }
}
