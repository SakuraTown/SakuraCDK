package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.deleteWhere
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.command
import top.iseason.bukkit.bukkittemplate.command.node
import top.iseason.bukkit.bukkittemplate.config.dbTransaction
import top.iseason.bukkit.bukkittemplate.utils.MessageUtils.sendColorMessage
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.data.CDKs
import top.iseason.bukkit.sakuracdk.data.CDKsYml
import top.iseason.bukkit.sakuracdk.data.KitsYml
import top.iseason.bukkit.sakuracdk.data.Records
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
        params = arrayOf(Param("[cdk]"))
        node("reload") {
            default = PermissionDefault.OP
            async = true
            description = "重载配置"
            onExecute = {
                KitsYml.load()
            }
        }
        node(
            "download"
        ) {
            default = PermissionDefault.OP
            async = true
            description = "将数据库的数据下载到本地"
            params = arrayOf(Param("<type>", listOf("all", "random", "cdk", "kit")))
            onExecute = {
                val option = getOptionalParam<String>(0) ?: "all"
                it.sendColorMessage("&6开始下载数据...")
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
                it.sendColorMessage("&a数据 &6${option} &a下载完成!")
            }
        }
        node(
            "update"
        ) {
            default = PermissionDefault.OP
            async = true
            description = "将数据同步至数据库"
            params = arrayOf(Param("<type>", listOf("all", "random", "cdk", "kit")))
            onExecute = {
                val option = getOptionalParam<String>(0) ?: "all"
                it.sendColorMessage("&6开始上传数据...")
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
                it.sendColorMessage("&a数据 &6${option} &a上传完成!")
            }
        }
        node(
            "delete"
        ) {
            default = PermissionDefault.OP
            async = true
            description = "删除一定时间外的礼包领取记录"
            params = arrayOf(Param("[time]", listOf("1d", "1w", "all")))
            onExecute = {
                val param = getParam<String>(0)
                val parseTime = Utils.parseTimeBefore(param)
                dbTransaction {
                    Records.deleteWhere { Records.acceptTime.between(LocalDateTime.of(2022, 1, 1, 0, 0), parseTime) }
                }
                it.sendColorMessage("&a 已删除 &6$parseTime &a之前的记录")
            }
        }
        node(CDKInfoNode)
        node(KitNode).apply {
            node(KitCreateNode)
            node(KitDeleteNode)
            node(KitAddItemNode)
            node(KitGiveNode)
        }
        node(CDKCreateNode)
    }
}
