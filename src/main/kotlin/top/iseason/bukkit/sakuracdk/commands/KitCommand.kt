package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.bukkittemplate.command.CommandNode
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParamSuggestCache
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.bukkittemplate.utils.bukkit.checkAir
import top.iseason.bukkit.bukkittemplate.utils.sendColorMessage
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.data.KitYml
import top.iseason.bukkit.sakuracdk.data.KitsYml
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object KitNode : CommandNode(
    "kit",
    default = PermissionDefault.OP,
    description = "礼包相关命令",
)

object KitCreateNode : CommandNode(
    "create", async = true, description = "创建礼包",
    params = arrayOf(
        Param("[id]"),
        Param("[过期时间]", suggest = listOf("1Y2M3W4d5h6m7s", "2022-08-20T16:22:41"))
    )
) {
    init {
        onExecute = onExecute@{
            val id = getParam<String>(0)
            if (KitsYml.kits.containsKey(id)) return@onExecute false
            val time = getParam<String>(1)
            val expires = try {
                LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                Utils.parseTimeAfter(time)
            }
            val kitYml = KitYml(id, LocalDateTime.now(), expires)
            KitsYml.kits[id] = kitYml
            KitsYml.save(false)
            it.sendColorMessage("&a创建&6 $id &a成功,过期时间: &6 $expires")
            true
        }
        failureMessage = "创建失败，ID已存在"
    }
}

object KitDeleteNode : CommandNode(
    "delete", async = true, description = "删除礼包",
    params = arrayOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits)
    )
) {
    init {
        onExecute = onExecute@{
            val id = getParam<String>(0)
            if (!KitsYml.kits.containsKey(id)) return@onExecute false
            KitsYml.kits.remove(id)
            KitsYml.save(false)
            it.sendColorMessage("&6$id &a已删除")
            true
        }
        failureMessage = "&cID不存在!"
    }
}

object KitAddItemNode : CommandNode(
    "addItem", async = true,
    description = "给礼包添加手上物品",
    isPlayerOnly = true,
    params = arrayOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits)
    )
) {
    init {
        onExecute = onExecute@{
            val kitYml = getParam<KitYml>(0)
            val inventory = (it as Player).inventory
            val itemInMainHand = inventory.getItem(inventory.heldItemSlot) ?: throw ParmaException("&6请拿着物品!")
            if (itemInMainHand.type.checkAir()) throw ParmaException("&6请拿着物品!")
            kitYml.itemStacksImpl.add(itemInMainHand)
            KitsYml.save(false)
            it.sendColorMessage("&6物品添加成功")
            true
        }
        failureMessage = "&cID不存在!"
    }
}

object KitGiveNode : CommandNode(
    "give",
    description = "将礼包给予玩家,不会有记录",
    params = arrayOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits),
        Param("[player]", suggestRuntime = ParamSuggestCache.playerParam)
    )
) {
    init {
        onExecute = onExecute@{
            val kitYml = getParam<KitYml>(0)
            val player = getParam<Player>(1)
            kitYml.applyPlayer(player)
            it.sendColorMessage("&a礼包已给予 &6${player.name}")
            true
        }
        failureMessage = "&cID不存在!"
    }
}
