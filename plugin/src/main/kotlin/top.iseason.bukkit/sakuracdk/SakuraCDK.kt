package top.iseason.bukkit.sakuracdk

import fr.xephi.authme.events.LoginEvent
import org.bstats.bukkit.Metrics
import org.bukkit.event.player.PlayerLoginEvent
import top.iseason.bukkit.sakuracdk.commands.cdkAdminCommands
import top.iseason.bukkit.sakuracdk.commands.userCommand
import top.iseason.bukkit.sakuracdk.config.*
import top.iseason.bukkit.sakuracdk.entity.*
import top.iseason.bukkit.sakuracdk.hook.AuthMeHook
import top.iseason.bukkit.sakuracdk.listener.RewardListener
import top.iseason.bukkittemplate.BukkitPlugin
import top.iseason.bukkittemplate.command.CommandHandler
import top.iseason.bukkittemplate.command.ParamAdopter
import top.iseason.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.debug.info
import top.iseason.bukkittemplate.utils.bukkit.EventUtils.listen
import top.iseason.bukkittemplate.utils.bukkit.EventUtils.register

object SakuraCDK : BukkitPlugin {

    override fun onEnable() {
        Metrics(javaPlugin, 17617)
        Lang.load(false)
        SimpleYAMLConfig.notifyMessage = "&7配置文件 &6%s &7已重载!"
        Config.load(false)
        DatabaseConfig.load(false)
        DatabaseConfig.initTables(Kits, Records, NormalCDKs, RandomCDKs, CDKs, RewardRecords)
        ParamAdopter(KitYml::class, "$%s 不是一个有效的Kit") { KitsYml.kits[it] }.register()
        KitsYml.load(false)
        CDKsYml.load(false)
        userCommand()
        cdkAdminCommands()
        //如果使用命令模块，取消注释
        CommandHandler.updateCommands()
        AuthMeHook.checkHooked()
        if (Config.enableOwnerReward) {
            RewardsYml.load(false)
            RewardListener.register()
            if (AuthMeHook.hasHooked) {
                listen<LoginEvent> {
                    RewardListener.onLogin(player)
                }
            } else {
                listen<PlayerLoginEvent> {
                    RewardListener.onLogin(player)
                }
            }
        }
        info("&a插件已启用!")
    }

    override fun onDisable() {
        CDKsYml.onDisable()
        info("&6插件已卸载!")
    }

}