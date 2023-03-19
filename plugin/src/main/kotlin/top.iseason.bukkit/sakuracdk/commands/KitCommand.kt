package top.iseason.bukkit.sakuracdk.commands

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.config.KitsYml
import top.iseason.bukkit.sakuracdk.entity.KitYml
import top.iseason.bukkittemplate.command.CommandNode
import top.iseason.bukkittemplate.command.CommandNodeExecutor
import top.iseason.bukkittemplate.command.Param
import top.iseason.bukkittemplate.command.ParamSuggestCache
import top.iseason.bukkittemplate.utils.bukkit.IOUtils.onItemInput
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessage
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.toColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object KitNode : CommandNode(
    "kit",
    default = PermissionDefault.OP,
    description = "礼包相关命令",
)

object KitCreateNode : CommandNode(
    "create", async = true, description = "创建礼包",
    params = listOf(
        Param("[id]"),
        Param("[过期时间]", suggest = listOf("1Y2M3W4d5h6m7s", "2022-08-20T16:22:41"))
    )
) {
    override var onExecute: CommandNodeExecutor? = CommandNodeExecutor { params, sender ->
        val id = params.next<String>()
        if (KitsYml.kits.containsKey(id)) {
            sender.sendColorMessage("&6创建失败，ID已存在")
            return@CommandNodeExecutor
        }
        val time = params.next<String>()
        val expires = try {
            LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            Utils.parseTimeAfter(time)
        }
        val kitYml = KitYml(id, LocalDateTime.now(), expires)
        KitsYml.kits[id] = kitYml

        val player = sender as? Player
        if (player != null) {
            player.onItemInput(async = true) { inv ->
                kitYml.itemStacksImpl = inv.filterNotNull().toMutableList()
                KitsYml.kitsSection.createSection(id, kitYml.serialize())
                KitsYml.save(false)
                sender.sendColorMessage("&a创建&6 $id &a成功,过期时间: &6 $expires")
            }
            return@CommandNodeExecutor
        }
        KitsYml.kitsSection.createSection(id, kitYml.serialize())
        KitsYml.save(false)
        sender.sendColorMessage("&a创建&6 $id &a成功,过期时间: &6 $expires")
    }
}


object KitDeleteNode : CommandNode(
    "delete", async = true, description = "删除礼包",
    params = listOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits)
    )
) {
    override var onExecute: CommandNodeExecutor? = CommandNodeExecutor { params, sender ->
        val id = params.next<String>()
        if (!KitsYml.kits.containsKey(id)) {
            sender.sendColorMessage("&6ID不存在!")
            return@CommandNodeExecutor
        }
        KitsYml.removeKit(id)
        KitsYml.save(false)
        sender.sendColorMessage("&6$id &a已删除")
    }
}


object KitEditNode : CommandNode(
    "edit", async = true,
    description = "编辑礼包物品",
    isPlayerOnly = true,
    params = listOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits)
    )
) {
    override var onExecute: CommandNodeExecutor? = CommandNodeExecutor { params, sender ->
        val player = sender as Player
        val kitYml = params.next<KitYml>()
        val createInventory = Bukkit.createInventory(null, 54, "&a请输入物品".toColor())
        createInventory.addItem(*kitYml.itemStacksImpl.toTypedArray())
        player.onItemInput(createInventory, true) {
            kitYml.itemStacksImpl = createInventory.filterNotNull().toMutableList()
            KitsYml.kitsSection.set(kitYml.id, kitYml.serialize())
            KitsYml.save(false)
            sender.sendColorMessage("&6物品添加成功")
        }
    }
}


object KitGiveNode : CommandNode(
    "give",
    description = "将礼包给予玩家,不会有记录",
    params = listOf(
        Param("[id]", suggestRuntime = KitsYml.suggestKits),
        Param("[player]", suggestRuntime = ParamSuggestCache.playerParam)
    )
) {
    override var onExecute: CommandNodeExecutor? = CommandNodeExecutor { params, sender ->
        val kitYml = params.next<KitYml>()
        val player = params.next<Player>()
        kitYml.applyPlayer(player)
        sender.sendColorMessage("&a礼包已给予 &6${player.name}")
    }

}
