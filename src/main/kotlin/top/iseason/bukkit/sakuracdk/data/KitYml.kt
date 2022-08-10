package top.iseason.bukkit.sakuracdk.data

import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.utils.bukkit.ItemUtils
import top.iseason.bukkit.bukkittemplate.utils.bukkit.giveItems
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class KitYml(
    val id: String,
    val create: LocalDateTime,
    val expire: LocalDateTime
) : ConfigurationSerializable {

    //过期时间
    var commandsImpl = mutableListOf<String>()
    var itemStacksImpl = mutableListOf<ItemStack>()

    fun isExpire() = LocalDateTime.now().isAfter(expire)

    //上传数据库
    fun updateDataBase() {
        transaction {
            var kit = Kit.findById(this@KitYml.id)
            if (kit != null) {
                kit.create = create
                kit.expires = expire
            } else {
                kit = Kit.new(this@KitYml.id) {
                    this.create = create
                    this.expires = expires
                }
            }
            if (commandsImpl.isNotEmpty())
                kit.commands = commandsImpl.toDataString()
            if (itemStacksImpl.isNotEmpty())
                kit.itemStacks = ExposedBlob(ItemUtils.toByteArrays(itemStacksImpl))
        }
    }

    private fun Collection<String>.toDataString(): String {
        val temp = StringBuilder("")
        for (any in this) {
            if (any.isBlank()) continue
            temp.append(any).append(';')
        }
        temp.subSequence(0, temp.length - 1)
        return temp.toString()
    }

    //将礼包应用玩家
    fun applyPlayer(player: Player) {
        for (command in commandsImpl) {
            parseCommand(command, player)
        }
        player.giveItems(itemStacksImpl)
    }

    private fun parseCommand(command: String, player: Player) {
        var playerCommand = command.replace("%player%", player.name)
        if (playerCommand.startsWith("CMD:")) {
            playerCommand = playerCommand.removePrefix("CMD:")
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), playerCommand)
            } catch (_: Exception) {
            }
        } else if (playerCommand.startsWith("OP:")) {
            playerCommand = playerCommand.removePrefix("OP:")
            val setOp = !player.isOp
            if (setOp) {
                player.isOp = true
                try {
                    Bukkit.dispatchCommand(player, playerCommand)
                } catch (_: Exception) {
                } finally {
                    player.isOp = false
                }
            } else {
                Bukkit.dispatchCommand(player, playerCommand)
            }
        } else {
            try {
                Bukkit.dispatchCommand(player, playerCommand)
            } catch (_: Exception) {
            }
        }

    }

    override fun serialize(): MutableMap<String, Any> {
        val mutableMapOf = mutableMapOf<String, Any>()
        mutableMapOf["id"] = id
        mutableMapOf["create"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(create)
        mutableMapOf["expires"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(expire)
        if (commandsImpl.isNotEmpty())
            mutableMapOf["commands"] = commandsImpl
        if (itemStacksImpl.isNotEmpty())
            mutableMapOf["itemStacks"] = itemStacksImpl.map { ItemUtils.toBase64(it) }.toList()
        return mutableMapOf
    }

    companion object {
        @JvmStatic
        fun deserialize(args: Map<String, Any>): KitYml? {
            val id = args["id"] as? String ?: return null
            val time =
                LocalDateTime.parse(args["create"] as? String ?: return null, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val expires =
                LocalDateTime.parse(args["expires"] as? String ?: return null, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val kit = KitYml(id, time, expires)
            args["commands"]?.apply {
                kit.commandsImpl = (this as? List<String>)?.toMutableList() ?: return@apply
            }
            args["itemStacks"]?.apply {
                val items = (this as? List<String>)?.toMutableList() ?: return@apply
                kit.itemStacksImpl = items.mapNotNull {
                    try {
                        ItemUtils.fromBase64(it)
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()
            }
            return kit
        }
    }
}