package top.iseason.bukkit.sakuracdk.hook

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import top.iseason.bukkit.sakuracdk.Utils
import top.iseason.bukkit.sakuracdk.config.CDKsYml
import top.iseason.bukkit.sakuracdk.config.RewardsYml
import top.iseason.bukkit.sakuracdk.entity.NormalCDKYml
import top.iseason.bukkit.sakuracdk.entity.RandomCDKYml
import top.iseason.bukkittemplate.BukkitTemplate

object PAPIExpansion : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return BukkitTemplate.getPlugin().name
    }

    override fun getAuthor(): String {
        return "Iseason"
    }

    override fun getVersion(): String {
        return BukkitTemplate.getPlugin().description.version
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null
        val split = params.split('_')
        val first = split.first()
        return when (first.lowercase()) {
            "cdks" -> {
                val index = kotlin.runCatching { split[1].toInt() }.getOrElse { 0 }
                return RewardsYml.rewards.values.mapNotNull {
                    if (it.players.contains(player.name)) it.id else null
                }.getOrNull(index) ?: ""
            }

            "cdk" -> {
                val second = split.getOrNull(1) ?: return null
                val third = split.getOrNull(2) ?: return null
                val baseCDK = CDKsYml.cdkCache[third] ?: return null
                when (second.lowercase()) {
                    "amount" -> if (baseCDK is RandomCDKYml)
                        baseCDK.cdkSet.size
                    else if (baseCDK is NormalCDKYml) baseCDK.amount else null

                    "expiretime" -> Utils.formater.format(baseCDK.expire)
                    "isexpire" -> baseCDK.checkExpire()
                    "allowrepeat" -> baseCDK.allowRepeat()
                    else -> null
                }?.toString()
            }

            else -> null
        }


    }
}