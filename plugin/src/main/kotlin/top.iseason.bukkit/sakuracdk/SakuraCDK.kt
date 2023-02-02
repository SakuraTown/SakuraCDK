package top.iseason.bukkit.sakuracdk

import top.iseason.bukkit.sakuracdk.commands.cdkAdminCommands
import top.iseason.bukkit.sakuracdk.commands.userCommand
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.data.*
import top.iseason.bukkittemplate.KotlinPlugin
import top.iseason.bukkittemplate.command.CommandHandler
import top.iseason.bukkittemplate.command.ParamAdopter
import top.iseason.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.debug.SimpleLogger
import top.iseason.bukkittemplate.debug.info
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.toColor

object SakuraCDK : KotlinPlugin() {

    override fun onEnable() {
        SimpleLogger.prefix = "&a[&6${javaPlugin.description.name}&a]&r ".toColor()
        MessageUtils.defaultPrefix = SimpleLogger.prefix
        SimpleYAMLConfig.notifyMessage = "&7配置文件 &6%s &7已重载!"
        Config.load(false)
        DatabaseConfig.load(false)
        DatabaseConfig.initTables(Kits, Records, NormalCDKs, RandomCDKs, CDKs)
        ParamAdopter(KitYml::class, "$%s 不是一个有效的Kit") { KitsYml.kits[it] }.register()
        KitsYml.load(false)
        CDKsYml.load(false)
        userCommand()
        cdkAdminCommands()
        //如果使用命令模块，取消注释
        CommandHandler.updateCommands()
        info("&a插件已启用!")
    }

    override fun onDisable() {
        CDKsYml.onDisable()
        info("&6插件已卸载!")
    }

}