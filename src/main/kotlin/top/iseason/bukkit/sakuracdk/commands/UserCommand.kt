package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.bukkittemplate.command.commandRoot
import top.iseason.bukkit.bukkittemplate.utils.EasyCoolDown
import top.iseason.bukkit.bukkittemplate.utils.sendColorMessage
import top.iseason.bukkit.bukkittemplate.utils.submit
import top.iseason.bukkit.sakuracdk.Config
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
            val baseCDK = CDKsYml.cdkCache[cdk] ?: throw ParmaException("&cCDK不存在或已过期!")
            if (baseCDK.checkExpire()) throw ParmaException("&cCDK不存在或已过期!")
            val player = it as Player
            transaction {
                //不允许重复领取
                if (!baseCDK.allowRepeat()) {
                    //检查是否有该cdk的领取记录
                    if (!Records.slice(Records.id)
                            .select { Records.group eq baseCDK.id and (Records.uid eq player.uniqueId) }
                            .limit(1).empty()
                    ) {
                        throw ParmaException("&c你已经领取过该礼包了!")
                    }
                }
                //检查余量
                if (baseCDK is NormalCDK) {
                    val count = Records.slice(Records.id.count()).select { Records.group eq baseCDK.id }.count()
                    if (count > baseCDK.amount) throw ParmaException("&c该礼包已领完!")
                }
                //记录
                Record.new {
                    this.uid = player.uniqueId
                    this.cdk = cdk
                    this.group = baseCDK.id
                    this.acceptTime = LocalDateTime.now()
                }
            }
            if (baseCDK is RandomCDK) {
                baseCDK.removeCDK(cdk)
                CDKsYml.cdkCache.remove(cdk)
            }
            //发放礼品
            var isSuccess = false
            submit {
                if (!baseCDK.applyPlayer(player)) {
                    player.sendColorMessage("&c礼包已过期!")
                }
            }
            true
        }
        onSuccess("&a兑换成功!")
    }

}
