package top.iseason.bukkit.sakuracdk.data

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import top.iseason.bukkit.bukkittemplate.config.dbTransaction
import top.iseason.bukkit.bukkittemplate.utils.bukkit.EntityUtils.giveItems
import top.iseason.bukkit.bukkittemplate.utils.bukkit.ItemUtils
import top.iseason.bukkit.bukkittemplate.utils.bukkit.ItemUtils.toBase64
import top.iseason.bukkit.bukkittemplate.utils.bukkit.ItemUtils.toByteArray
import top.iseason.bukkit.bukkittemplate.utils.bukkit.ItemUtils.toSection
import top.iseason.bukkit.sakuracdk.config.Config
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
        dbTransaction {
            var kit = Kit.findById(this@KitYml.id)
            if (kit != null) {
                kit.create = this@KitYml.create
                kit.expire = this@KitYml.expire
            } else {
                kit = Kit.new(this@KitYml.id) {
                    this.create = this@KitYml.create
                    this.expire = this@KitYml.expire
                }
            }
            if (commandsImpl.isNotEmpty())
                kit.commands = commandsImpl.toDataString()
            if (itemStacksImpl.isNotEmpty())
                kit.itemStacks = ExposedBlob(itemStacksImpl.toByteArray())
        }
    }

    private fun Collection<String>.toDataString(): String {
        val temp = StringBuilder()
        for (any in this) {
            if (any.isBlank()) continue
            temp.append(any).append(';')
        }
        temp.deleteCharAt(temp.length - 1)
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
        if (itemStacksImpl.isNotEmpty()) {
            if (Config.enciphered)
                mutableMapOf["itemStacks"] = itemStacksImpl.map { it.toBase64() }.toList()
            else mutableMapOf["itemStacks"] = itemStacksImpl.toSection()
        }
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
            val any = args["itemStacks"] ?: return kit
            if (Config.enciphered) {
                any.apply {
                    val items = (this as? List<String>)?.toMutableList() ?: return@apply
                    kit.itemStacksImpl = items.mapNotNull {
                        try {
                            ItemUtils.fromBase64ToItemStack(it)
                        } catch (e: Exception) {
                            null
                        }
                    }.toMutableList()
                }
            } else {
                val yamlConfiguration = YamlConfiguration()
                val list = any as? List<Map<*, *>> ?: return kit
                kit.itemStacksImpl = list.mapNotNull {
                    ItemUtils.fromSection(yamlConfiguration.createSection("1", it), true)
                }.toMutableList()
            }
            return kit
        }
    }
}