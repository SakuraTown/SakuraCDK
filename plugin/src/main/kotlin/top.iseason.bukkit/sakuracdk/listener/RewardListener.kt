package top.iseason.bukkit.sakuracdk.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.config.RewardsYml
import top.iseason.bukkit.sakuracdk.event.CDKAcceptEvent
import top.iseason.bukkittemplate.utils.other.submit

object RewardListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onCDKAcceptEvent(event: CDKAcceptEvent) {
        if (!Config.enableOwnerReward) return
        val reward = RewardsYml.rewards[event.cdk.id] ?: return
        submit(async = true) {
            reward.applyOnline()
        }
    }

    fun onLogin(player: Player) {
        submit(RewardsYml.loginDelay, async = true) {
            RewardsYml.rewards.mapNotNull {
                if (it.value.players.contains(player.name)) {
                    it.value
                } else null
            }.forEach {
                it.applyOnLogin(player)
            }
        }
    }
}