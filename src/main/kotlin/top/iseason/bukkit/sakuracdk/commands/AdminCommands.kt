package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.commandRoot
import top.iseason.bukkit.sakuracdk.Config
import top.iseason.bukkit.sakuracdk.data.KitsYml


fun cdkAdminCommands() {
    commandRoot(
        "sakuracdkadmin",
        alias = arrayOf("cdkadmin"),
        description = "管理cdk",
        default = PermissionDefault.OP,
        isPlayerOnly = true,
        async = true,
        params = arrayOf(Param("[cdk]"))
    ) {
        node("reload", default = PermissionDefault.OP, async = true, description = "重载配置") {
            onExecute {
                Config.reload()
                KitsYml.load()
                true
            }
        }
        node(KitNode) {
            node(KitCreateNode)
            node(KitDeleteNode)
            node(KitAddItemNode)
            node(KitGiveNode)
            node(KitDownloadNode)
        }
        node(CDKCreateNode)
    }
}
