package top.iseason.bukkit.sakuracdk.data

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Kits : StringIdTable() {
    var amount = integer("amount").default(10)
    val create = datetime("create").default(LocalDateTime.now())
    val expires = datetime("expires").default(LocalDateTime.now())
    val commands = text("commands").nullable()
    val itemStacks = blob("itemStacks").nullable()

    //是否存在某个id
    fun has(id: String): Boolean {
        return try {
            var has = false
            transaction {
                has = !Kits.slice(Kits.id).select { Kits.id eq id }.limit(1).empty()
            }
            has
        } catch (e: Exception) {
            false
        }
    }
}

open class StringIdTable(name: String = "", columnName: String = "id") : IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(columnName, 50).entityId()
    final override val primaryKey = PrimaryKey(id)
}