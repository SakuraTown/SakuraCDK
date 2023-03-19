package top.iseason.bukkit.sakuracdk.entity

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import top.iseason.bukkittemplate.config.dbTransaction
import top.iseason.bukkittemplate.hook.PlaceHolderHook
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessages

data class Rewards(
    val id: String,
    val players: List<String>,
    val commands: List<String>,
    val commandsRepeat: List<String>,
    val message: List<String>
) {

    fun applyOnline() {
        val playerList = players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
            applyFor(it, 1)
        }
    }

    fun applyOnLogin(player: Player) {
        val total = dbTransaction {
            Records.slice(Records.id.count()).select { Records.group eq this@Rewards.id }
                .first()[Records.id.count()]
        }.toInt()
        val accepted = dbTransaction {
            RewardRecords.slice(RewardRecords.count)
                .select { RewardRecords.group eq this@Rewards.id and (RewardRecords.player eq player.name) }
                .firstOrNull()?.get(RewardRecords.count) ?: 0
        }
        if (accepted >= total) return
        applyFor(player, total - accepted)
    }

    fun applyFor(player: Player, count: Int) {
        commands.forEach {
            kotlin.runCatching {
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    PlaceHolderHook.setPlaceHolder(
                        it.replace("%player%", player.name)
                            .replace("%remain%", count.toString()), player
                    )
                )
            }.getOrElse { it.printStackTrace() }
        }
        repeat(count) {
            commandsRepeat.forEach {
                kotlin.runCatching {
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        PlaceHolderHook.setPlaceHolder(
                            it.replace("%player%", player.name), player
                        )
                    )
                }.getOrElse { it.printStackTrace() }
            }
        }
        player.sendColorMessages(message)
        dbTransaction {
            val record = RewardRecord
                .find { RewardRecords.group eq this@Rewards.id and (RewardRecords.player eq player.name) }
                .firstOrNull()
            if (record == null) RewardRecord.new {
                this.group = this@Rewards.id
                this.player = player.name
                this.count = 1
            } else {
                record.count += 1
            }
        }
    }

    companion object {
        fun fromSection(section: ConfigurationSection, id: String): Rewards {
            return Rewards(
                id,
                section.getStringList("players"),
                section.getStringList("commands"),
                section.getStringList("commandsRepeat"),
                section.getStringList("message")
            )
        }
    }
}