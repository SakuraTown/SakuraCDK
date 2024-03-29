package top.iseason.bukkit.sakuracdk.entity

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import top.iseason.bukkittemplate.config.StringIdTable
import top.iseason.bukkittemplate.config.dbTransaction
import java.time.LocalDateTime

object Kits : StringIdTable() {
    val create = datetime("create").default(LocalDateTime.now())
    val expire = datetime("expires").default(LocalDateTime.now())
    val commands = text("commands")
    val itemStacks = blob("itemStacks").nullable()

}

//是否存在某个id
fun <T : Comparable<T>> IdTable<T>.has(id: T): Boolean {
    return try {
        var has = false
        dbTransaction {
            has = !this@has.slice(this@has.id).select { this@has.id eq id }.limit(1).empty()
        }
        has
    } catch (e: Exception) {
        false
    }
}
