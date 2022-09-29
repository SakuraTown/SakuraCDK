package top.iseason.bukkit.sakuracdk.data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

//领取记录
object Records : IntIdTable() {
    val uid = uuid("uid")
    val cdk = varchar("cdk", 60)

    //cdk的承载体
    val group = varchar("group", 60)
    val acceptTime = datetime("time")

    init {
        id.index(isUnique = true)
    }
}