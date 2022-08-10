package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.command.CommandNode
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.sakuracdk.SakuraCDK
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.data.CDKs
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
                transaction {
                    repeat(amount) {
                        val replaceRandom = Utils.replaceRandom(Config.cdkTemplate)
                        bw.write(replaceRandom)
                        bw.newLine()
                        CDKs.insert {
                            it[CDKs.id] = replaceRandom
                            it[group] = id
                        }
                    }
                }
            }
            true
        }
        successMessage = "文件已创建!"
    }
}
