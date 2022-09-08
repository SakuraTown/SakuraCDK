package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.bukkittemplate.command.CommandNode
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParamSuggestCache
import top.iseason.bukkit.bukkittemplate.utils.MessageUtils.sendColorMessage
import top.iseason.bukkit.bukkittemplate.utils.MessageUtils.toColor
import top.iseason.bukkit.bukkittemplate.utils.bukkit.IOUtils.onItemInput
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
            if (KitsYml.kits.containsKey(id)) {
                it.sendColorMessage("&6创建失败，ID已存在")
                return@onExecute
            }
            val time = getParam<String>(1)
            val expires = try {
                LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                Utils.parseTimeAfter(time)
            }
            val kitYml = KitYml(id, LocalDateTime.now(), expires)
            KitsYml.kits[id] = kitYml
            val player = it as? Player
            if (player != null) {
                player.onItemInput(async = true) { inv ->
                    kitYml.itemStacksImpl = inv.filterNotNull().toMutableList()
                    KitsYml.save(false)
                    it.sendColorMessage("&a创建&6 $id &a成功,过期时间: &6 $expires")
                }
                return@onExecute
            }
            KitsYml.save(false)
            it.sendColorMessage("&a创建&6 $id &a成功,过期时间: &6 $expires")
        }
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
            if (!KitsYml.kits.containsKey(id)) {
                it.sendColorMessage("&6ID不存在!")
                return@onExecute
            }
            KitsYml.kits.remove(id)
            KitsYml.save(false)
            it.sendColorMessage("&6$id &a已删除")
            true
        }
    }
}

object KitEditNode : CommandNode(
    "edit", async = true,
    description = "编辑礼包物品",
    isPlayerOnly = true,
    params = arrayOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits)
    )
) {
    init {
        onExecute = onExecute@{ sender ->
            val player = sender as Player
            val kitYml = getParam<KitYml>(0)
            val createInventory = Bukkit.createInventory(null, 54, "&a请输入物品".toColor())
            kitYml.itemStacksImpl.forEach {
                createInventory.addItem(it)
            }
            player.onItemInput(createInventory, true) {
                kitYml.itemStacksImpl = createInventory.filterNotNull().toMutableList()
                KitsYml.save(false)
                sender.sendColorMessage("&6物品添加成功")
            }
        }
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
        }
    }
}
