package top.iseason.bukkit.sakuracdk

import org.bukkit.configuration.serialization.ConfigurationSerialization
import top.iseason.bukkit.bukkittemplate.KotlinPlugin
import top.iseason.bukkit.bukkittemplate.command.CommandBuilder
import top.iseason.bukkit.bukkittemplate.command.TypeParam
import top.iseason.bukkit.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.debug.SimpleLogger
import top.iseason.bukkit.bukkittemplate.debug.info
import top.iseason.bukkit.bukkittemplate.utils.MessageUtils
import top.iseason.bukkit.bukkittemplate.utils.toColor
import top.iseason.bukkit.sakuracdk.commands.cdkAdminCommands
import top.iseason.bukkit.sakuracdk.commands.userCommand
import top.iseason.bukkit.sakuracdk.config.Config
import top.iseason.bukkit.sakuracdk.data.*

object SakuraCDK : KotlinPlugin() {


    override fun onEnable() {
//        SimpleLogger.isDebug = true
        SimpleLogger.prefix = "&a[&6${javaPlugin.description.name}&a]&r ".toColor()
        MessageUtils.defaultPrefix = SimpleLogger.prefix
        SimpleYAMLConfig.notifyMessage = "&7配置文件 &6%s &7已重载!"
        info("&a插件已启用!")
    }

    override fun onAsyncEnable() {
        SakuraCDK.javaPlugin.saveResource("cdk.yml", false)
        SakuraCDK.javaPlugin.saveResource("kits.yml", false)
        Config.load(false)
        ConfigurationSerialization.registerClass(KitYml::class.java)
        DatabaseConfig.load(false)
        DatabaseConfig.initTables(Kits, Records, NormalCDKs, RandomCDKs, CDKs)
        TypeParam(KitYml::class, { "$it 不是一个有效的Kit" }) { KitsYml.kits[it] }
        KitsYml.load(false)
        CDKsYml.load(false)
        userCommand()
        cdkAdminCommands()
        //如果使用命令模块，取消注释
        CommandBuilder.updateCommands()

    }

    override fun onDisable() {
        CDKsYml.onDisable()
        info("&6插件已卸载!")
    }

}