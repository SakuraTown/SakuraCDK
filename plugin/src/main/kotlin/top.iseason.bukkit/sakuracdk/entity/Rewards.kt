package top.iseason.bukkit.sakuracdk.entity

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import top.iseason.bukkittemplate.config.dbTransaction
import top.iseason.bukkittemplate.hook.PlaceHolderHook
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.sendColorMessages
import top.iseason.bukkittemplate.utils.other.submit

data class Rewards(
    val id: String,
    val players: List<String>,
    val commands: List<String>,
    val commandsRepeat: List<String>,
    val message: List<String>
) {

    fun applyOnline() {
        players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
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
        submit {
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
        }
        var times = 0
        val max = count / 5 + 1
        val last = count % 5
        var submit: BukkitTask? = null
        submit = submit(period = 1) {
            times++
            repeat(if (times == max) last else 5) {
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
            if (times == max) submit?.cancel()
        }
        player.sendColorMessages(message.map {
            it.replace("%times%", count.toString())
        })
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
                section.getStringList("commands-repeat"),
                section.getStringList("message")
            )
        }
    }
}