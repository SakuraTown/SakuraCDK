package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.deleteWhere
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.config.Lang
import top.iseason.bukkit.sakuracdk.data.CDKs
import top.iseason.bukkit.sakuracdk.data.CDKsYml
import top.iseason.bukkit.sakuracdk.data.KitsYml
import top.iseason.bukkit.sakuracdk.data.Records
import top.iseason.bukkittemplate.command.Param
import top.iseason.bukkittemplate.command.command
import top.iseason.bukkittemplate.command.executor
import top.iseason.bukkittemplate.command.node
import top.iseason.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkittemplate.config.dbTransaction
import top.iseason.bukkittemplate.debug.SimpleLogger
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.formatBy
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import java.time.LocalDateTime

fun cdkAdminCommands() {
    command(
        "sakuracdkadmin"
    ) {
        alias = arrayOf("cdkadmin")
        description = "管理cdk"
        default = PermissionDefault.OP
        isPlayerOnly = true
        async = true
        params = listOf(Param("[cdk]"))
        node("reload") {
            default = PermissionDefault.OP
            async = true
            description = "重载配置"
            executor { params, sender ->
                KitsYml.load()
            }
        }
        node(
            "download"
        ) {
            default = PermissionDefault.OP
            async = true
            description = "将数据库的数据下载到本地"
            params = listOf(Param("<type>", listOf("all", "random", "cdk", "kit")))
            executor { params, sender ->
                val option = params.nextOrNull<String>() ?: "all"
                sender.sendColorMessage("&6开始下载数据...")
                try {
                    when (option) {
                        "random" -> CDKs.downloadRandomData()
                        "cdk" -> CDKsYml.downloadAll()
                        "kit" -> KitsYml.downloadData()
                        else -> {
                            CDKs.downloadRandomData()
                            CDKsYml.downloadAll()
                            KitsYml.downloadData()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                sender.sendColorMessage("&a数据 &6${option} &a下载完成!")
            }
        }
        node(
            "update"
        ) {
            default = PermissionDefault.OP
            async = true
            description = "将数据同步至数据库"
            params = listOf(Param("<type>", listOf("all", "random", "cdk", "kit")))
            executor { params, sender ->
                val option = params.nextOrNull<String>() ?: "all"
                sender.sendColorMessage("&6开始上传数据...")
                try {
                    when (option) {
                        "random" -> {
                            CDKsYml.onDisable()
                            CDKsYml.updateRandomData()
                        }

                        "cdk" -> CDKsYml.updateAllData()
                        "kit" -> KitsYml.updateData()
                        else -> {
                            CDKsYml.onDisable()
                            CDKsYml.updateRandomData()
                            CDKsYml.updateAllData()
                            KitsYml.updateData()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                sender.sendColorMessage("&a数据 &6${option} &a上传完成!")
            }
        }
        node(
            "delete"
        ) {
            default = PermissionDefault.OP
            async = true
            description = "删除一定时间外的礼包领取记录"
            params = listOf(Param("[time]", listOf("1d", "1w", "all")))
            executor { params, sender ->
                val param = params.next<String>()
                val parseTime = Utils.parseTimeBefore(param)
                dbTransaction {
                    Records.deleteWhere { Records.acceptTime.between(LocalDateTime.of(2022, 1, 1, 0, 0), parseTime) }
                }
                sender.sendColorMessage("&a 已删除 &6$parseTime &a之前的记录")
            }
        }
        node(CDKInfoNode)
        node(KitNode).apply {
            node(KitCreateNode)
            node(KitDeleteNode)
            node(KitEditNode)
            node(KitGiveNode)
        }
        node(CDKCreateNode)

        node("debug") {
            default = PermissionDefault.OP
            async = true
            description = "切换deug模式"
            executor { _, sender ->
                SimpleLogger.isDebug = !SimpleLogger.isDebug
                sender.sendColorMessage(Lang.command__debug.formatBy(SimpleLogger.isDebug))
            }
        }
        node("reload") {
            default = PermissionDefault.OP
            async = true
            description = "重载命令"
            executor { _, sender ->
                Config.load()
                DatabaseConfig.reConnected()
                KitsYml.load()
                CDKsYml.load()
                sender.sendColorMessage(Lang.command__reload)
            }
        }
    }
}
