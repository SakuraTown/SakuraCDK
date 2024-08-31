package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.entity.CDKs
import top.iseason.bukkit.sakuracdk.entity.NormalCDK
import top.iseason.bukkit.sakuracdk.entity.RandomCDK
import top.iseason.bukkit.sakuracdk.entity.RandomCDKYml
import top.iseason.bukkit.sakuracdk.entity.Records
import top.iseason.bukkittemplate.command.CommandNode
import top.iseason.bukkittemplate.command.CommandNodeExecutor
import top.iseason.bukkittemplate.command.Param
import top.iseason.bukkittemplate.command.ParmaException
import top.iseason.bukkittemplate.config.dbTransaction
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessages

object CDKCreateNode : CommandNode(
    "randomCDK",
    default = PermissionDefault.OP,
    description = "创建随机cdk文件",
    async = true,
    params = listOf(Param("[id]"), Param("[amount]"))
) {
    override var onExecute: CommandNodeExecutor? = CommandNodeExecutor { params, sender ->
        val id = params.next<String>()
        val amount = params.next<Int>()

        dbTransaction {
            if (!CDKs.select(CDKs.group).where { CDKs.group eq id }.limit(1).empty()) {
                throw ParmaException("&c文件已存在!")
            }
        }
        val list = ArrayList<String>(amount)
        repeat(amount) {
            val replaceRandom = Utils.replaceRandom(Config.cdkTemplate)
            list.add(replaceRandom)
        }
        RandomCDKYml.saveTxt(id, list)
        sender.sendColorMessage("文件已创建!")
    }
}

object CDKInfoNode : CommandNode(
    "info",
    default = PermissionDefault.OP,
    description = "查看某个cdk的信息",
    async = true,
    params = listOf(Param("[cdk]"))
) {
    override var onExecute: CommandNodeExecutor? = CommandNodeExecutor { params, sender ->
        val cdk = params.next<String>()
        val mutableListOf = mutableListOf<String>()
        dbTransaction {
            val cdks = CDKs.selectAll().where { CDKs.id eq cdk }.limit(1).firstOrNull()
                ?: throw ParmaException("&6cdk不存在")
            mutableListOf.add("&aCDK: &6${cdk}")
            val group = cdks[CDKs.group]
            val type = cdks[CDKs.type]
            if (type == "random") {
                val findById = RandomCDK.findById(group) ?: throw ParmaException("&6cdk不存在")
                val count = CDKs.select(CDKs.id.count()).where { CDKs.group eq group and (CDKs.type eq "random") }
                    .first()[CDKs.id.count()]
                mutableListOf.add("&a类型: &6random")
                mutableListOf.add("&a群组: &6$group")
                mutableListOf.add("&a余量: &6$count")
                mutableListOf.add("&a过期: &6${findById.expire}")
                mutableListOf.add("&a礼包: &6${findById.kits}")
            } else if (type == "normal") {
                val findById = NormalCDK.findById(group) ?: throw ParmaException("cdk不存在")
                val count =
                    Records.select(Records.id.count()).where { Records.group eq group }.first()[Records.id.count()]
                mutableListOf.add("&a类型: &6normal")
                mutableListOf.add("&a总量: &6${findById.amount}")
                mutableListOf.add("&a余量: &6${findById.amount - count}")
                mutableListOf.add("&a过期: &6${findById.expire}")
                mutableListOf.add("&a礼包: &6${findById.kits}")
            } else throw ParmaException("cdk不存在")
            sender.sendColorMessages(mutableListOf)
        }
    }
}

