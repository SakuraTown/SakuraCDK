package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.config.Lang
import top.iseason.bukkit.sakuracdk.entity.*
import top.iseason.bukkit.sakuracdk.event.CDKAcceptEvent
import top.iseason.bukkittemplate.command.Param
import top.iseason.bukkittemplate.command.ParmaException
import top.iseason.bukkittemplate.command.command
import top.iseason.bukkittemplate.command.executor
import top.iseason.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkittemplate.config.dbTransaction
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.other.EasyCoolDown
import top.iseason.bukkittemplate.utils.other.submit
import java.time.LocalDateTime

fun userCommand() {
    command(
        Config.command_name
    ) {
        alias = Config.command_alias.toTypedArray()
        description = "使用cdk兑换礼包"
        isPlayerOnly = true
        async = true
        params = listOf(Param("[cdk]"))
        executor { params, sender ->
            if (EasyCoolDown.check(
                    sender,
                    Config.command_cooldown
                )
            ) throw ParmaException(Lang.command__user_send_too_fast)
            if (!DatabaseConfig.isConnected) throw ParmaException(Lang.command__user_database_closed)
            val cdk = params.next<String>()
            var cdkYml: BaseCDK? = null
            val player = sender as Player
            val uniqueId = player.uniqueId
            var groupTemp: String?
            dbTransaction {
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
                    if (!Records.select { Records.group eq group and (Records.uid eq uniqueId) }.limit(1).empty()) {
                        throw ParmaException(Lang.command__user_has_accepted)
                    }
                    if (cdkYml is NormalCDKYml) {
                        val exist = Records.slice(Records.id.count()).select { Records.group eq group }
                            .first()[Records.id.count()]
                        if (exist >= (cdkYml as NormalCDKYml).amount) throw ParmaException(Lang.command__user_normal_brought_out)
                    }
                }
                val cdkAcceptEvent = CDKAcceptEvent(player, cdkYml!!)
                Bukkit.getPluginManager().callEvent(cdkAcceptEvent)
                if (cdkAcceptEvent.isCancelled) throw ParmaException("领取失败!")
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
        }
    }
}
