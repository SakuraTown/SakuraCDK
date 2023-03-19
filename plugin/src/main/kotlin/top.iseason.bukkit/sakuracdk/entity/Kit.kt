package top.iseason.bukkit.sakuracdk.entity

import org.jetbrains.exposed.dao.id.EntityID
import top.iseason.bukkittemplate.config.StringEntity
import top.iseason.bukkittemplate.config.StringEntityClass
import top.iseason.bukkittemplate.utils.bukkit.ItemUtils


class Kit(
    id: EntityID<String>
) : StringEntity(id) {
    var create by Kits.create
    var expire by Kits.expire
    var commands by Kits.commands
    var itemStacks by Kits.itemStacks

    fun toKitYml(): KitYml {
        val kitYml = KitYml(id.value, create, expire)
        if (commands.isNotBlank()) {
            kitYml.commandsImpl = commands.split(";").filter { it.isNotBlank() }.toMutableList()
        }
        if (itemStacks != null) {
            kitYml.itemStacksImpl = ItemUtils.fromByteArrays(itemStacks!!.bytes).toMutableList()
        }
        return kitYml
    }

    companion object : StringEntityClass<Kit>(Kits)
}
