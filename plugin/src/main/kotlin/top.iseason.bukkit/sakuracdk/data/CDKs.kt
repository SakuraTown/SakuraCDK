package top.iseason.bukkit.sakuracdk.data

import org.jetbrains.exposed.sql.select
import top.iseason.bukkittemplate.config.dbTransaction

object CDKs : StringIdTable() {
    val group = varchar("group", 30)
    val type = varchar("type", 15)

    //将数据库储存的随机cdk下载到本地
    fun downloadRandomData() {
        val mutableListOf = mutableMapOf<String, MutableList<String>>()
        //查询数据
        dbTransaction {
            CDKs.select { type eq "random" }.forEach {
                val group = it[group]
                var strings = mutableListOf[group]
                if (strings == null) {
                    strings = mutableListOf()
                    mutableListOf[group] = strings
                }
                strings.add(it[CDKs.id].value)
            }
        }
        mutableListOf.forEach { (group, cdks) ->
            RandomCDKYml.saveTxt(group, cdks)
        }
    }

}