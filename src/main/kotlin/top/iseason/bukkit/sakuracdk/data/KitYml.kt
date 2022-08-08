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
    val amount: Int,
    val create: LocalDateTime,
    val expires: LocalDateTime
) : ConfigurationSerializable {

    //过期时间
    var commandsImpl = mutableListOf<String>()
    var itemStacksImpl = mutableListOf<ItemStack>()

    //上传数据库
    fun updateDataBase() {
//        var kit: Kit? = null
        transaction {
//            addLogger(StdOutSqlLogger)
            var kit = Kit.findById(this@KitYml.id)
            if (kit != null) {
                kit.amount = amount
                kit.create = create
                kit.expires = expires
                if (commandsImpl.isNotEmpty())
                    kit.commands = commandsImpl.toDataString()
                if (itemStacksImpl.isNotEmpty())
                    kit.itemStacks = ExposedBlob(ItemUtils.toByteArrays(itemStacksImpl))
                Unit
            } else {
                Kit.new(this@KitYml.id) {
                    this.amount = amount
                    this.create = create
                    this.expires = expires
                    this.amount = amount
                    if (commandsImpl.isNotEmpty())
                        this.commands = commandsImpl.toDataString()
                    if (itemStacksImpl.isNotEmpty())
                        this.itemStacks = ExposedBlob(ItemUtils.toByteArrays(itemStacksImpl))
                }
            }
        }
    }

    private fun Collection<String>.toDataString(): String {
        var temp = ""
        for (any in this) {
            temp += "$any;"
        }
        return temp
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
                    player.isOp = false
                } catch (_: Exception) {
                    player.isOp = false
                }
                player.isOp = false
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
        mutableMapOf["amount"] = amount
        mutableMapOf["create"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(create)
        mutableMapOf["expires"] = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(expires)
        mutableMapOf["commands"] = commandsImpl
        mutableMapOf["itemStacks"] = itemStacksImpl.map { ItemUtils.toBase64(it) }.toList()
        return mutableMapOf
    }

    companion object {
        @JvmStatic
        fun deserialize(args: Map<String, Any>): KitYml? {
            val id = args["id"] as? String ?: return null
            val count = args["amount"] as? Int ?: return null
            val time =
                LocalDateTime.parse(args["create"] as? String ?: return null, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val expires =
                LocalDateTime.parse(args["expires"] as? String ?: return null, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val kit = KitYml(id, count, time, expires)
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