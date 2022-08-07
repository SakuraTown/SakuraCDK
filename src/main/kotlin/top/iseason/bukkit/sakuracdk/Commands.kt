package top.iseason.bukkit.sakuracdk

import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.commandRoot

fun command1() {
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
            val param = getParam<String>(0)

            true
        }
    }
}
