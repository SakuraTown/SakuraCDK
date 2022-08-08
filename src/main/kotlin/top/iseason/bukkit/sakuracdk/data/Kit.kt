package top.iseason.bukkit.sakuracdk.data

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import top.iseason.bukkit.bukkittemplate.utils.bukkit.ItemUtils


class Kit(
    id: EntityID<String>
) : StringEntity(id) {

    var amount by Kits.amount
    var create by Kits.create
    var expires by Kits.expires
    var commands by Kits.commands
    var itemStacks by Kits.itemStacks

    fun toKitYml(): KitYml {
        val kitYml = KitYml(id.value, amount, create, expires)
        if (commands != null) {
            kitYml.commandsImpl = commands!!.split(";").toMutableList()
        }
        if (itemStacks != null) {
            kitYml.itemStacksImpl = ItemUtils.fromByteArrays(itemStacks!!.bytes).toMutableList()
        }
        return kitYml
    }

    companion object : StringEntityClass<Kit>(Kits)
}

abstract class StringEntity(id: EntityID<String>) : Entity<String>(id)

abstract class StringEntityClass<out E : Entity<String>> constructor(
    table: IdTable<String>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<String>) -> E)? = null
) : EntityClass<String, E>(table, entityType, entityCtor)