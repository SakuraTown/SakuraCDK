package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.bukkittemplate.command.CommandNode
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.sakuracdk.Config
import top.iseason.bukkit.sakuracdk.SakuraCDK
import top.iseason.bukkit.sakuracdk.Utils
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object CDKCreateNode : CommandNode(
    "randomCDK",
    default = PermissionDefault.OP,
    description = "创建随机cdk文件",
    async = true,
    params = arrayOf(Param("[id]"), Param("[amount]"))
) {
    init {
        onExecute = {
            val id = getParam<String>(0)
            val amount = getParam<Int>(1)
            val file = File(SakuraCDK.javaPlugin.dataFolder, "random${File.separatorChar}${id}.txt")
            if (file.exists()) throw ParmaException("&c文件已存在!")
            file.parentFile.mkdirs()
            file.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(file))).use { bw ->
                repeat(amount) {
                    bw.write(Utils.replaceRandom(Config.cdkTemplate))
                    bw.newLine()
                }
            }
            true
        }
        successMessage = "文件已创建!"
    }
}
