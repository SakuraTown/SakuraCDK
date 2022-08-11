package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.command.CommandNode
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.bukkittemplate.utils.sendColorMessages
import top.iseason.bukkit.sakuracdk.SakuraCDK
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.data.CDKs
import top.iseason.bukkit.sakuracdk.data.NormalCDK
import top.iseason.bukkit.sakuracdk.data.RandomCDK
import top.iseason.bukkit.sakuracdk.data.Records
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
            transaction {
                if (!CDKs.select { CDKs.group eq id }.limit(1).empty()) {
                    throw ParmaException("&c文件已存在!")
                }
            }
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
                            it[CDKs.group] = id
                            it[CDKs.type] = "random"
                        }
                    }
                }
            }
            true
        }
        successMessage = "文件已创建!"
    }
}

object CDKInfoNode : CommandNode(
    "info",
    default = PermissionDefault.OP,
    description = "查看某个cdk的信息",
    async = true,
    params = arrayOf(Param("[cdk]"))
) {
    init {
        onExecute = {
            val cdk = getParam<String>(0)
            val mutableListOf = mutableListOf<String>()
            transaction {
                val cdks = CDKs.select { CDKs.id eq cdk }.limit(1).firstOrNull() ?: throw ParmaException("&6cdk不存在")
                mutableListOf.add("&aCDK: &6${cdk}")
                val group = cdks[CDKs.group]
                val type = cdks[CDKs.type]
                if (type == "random") {
                    val findById = RandomCDK.findById(group) ?: throw ParmaException("cdk不存在")
                    val count =
                        CDKs.slice(CDKs.id.count()).select { CDKs.group eq group and (CDKs.type eq "random") }
                            .first()[CDKs.id.count()]
                    mutableListOf.add("&a类型: &6random")
                    mutableListOf.add("&a群组: &6$group")
                    mutableListOf.add("&a余量: &6$count")
                    mutableListOf.add("&a过期: &6${findById.expire}")
                    mutableListOf.add("&a礼包: &6${findById.kits}")
                } else if (type == "normal") {
                    val findById = NormalCDK.findById(group) ?: throw ParmaException("cdk不存在")
                    val count = Records.slice(Records.id.count()).select { Records.group eq group }
                        .first()[Records.id.count()]
                    mutableListOf.add("&a类型: &6normal")
                    mutableListOf.add("&a总量: &6${findById.amount}")
                    mutableListOf.add("&a余量: &6${findById.amount - count}")
                    mutableListOf.add("&a过期: &6${findById.expire}")
                    mutableListOf.add("&a礼包: &6${findById.kits}")
                } else throw ParmaException("cdk不存在")
                it.sendColorMessages(mutableListOf)
            }
            true
        }
    }
}
