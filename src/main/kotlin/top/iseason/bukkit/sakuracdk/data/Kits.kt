package top.iseason.bukkit.sakuracdk.data

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime

object Kits : StringIdTable() {
    val count = integer("count")
    val create = datetime("create")
    val expires = datetime("expires")
    val commands = text("commands")
    val itemStacks = blob("itemStacks")
}

open class StringIdTable(name: String = "", columnName: String = "id") : IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(columnName, 20).entityId()
    final override val primaryKey = PrimaryKey(id)
}