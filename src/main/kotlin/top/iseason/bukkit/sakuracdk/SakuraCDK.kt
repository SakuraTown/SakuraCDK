package top.iseason.bukkit.sakuracdk

import org.bukkit.configuration.serialization.ConfigurationSerialization
import top.iseason.bukkit.bukkittemplate.KotlinPlugin
import top.iseason.bukkit.bukkittemplate.command.CommandBuilder
import top.iseason.bukkit.bukkittemplate.config.ConfigWatcher
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.debug.SimpleLogger
import top.iseason.bukkit.bukkittemplate.debug.info
import top.iseason.bukkit.bukkittemplate.utils.toColor
import top.iseason.bukkit.sakuracdk.data.Config
import top.iseason.bukkit.sakuracdk.data.KitYml
import top.iseason.bukkit.sakuracdk.data.KitsYml

object SakuraCDK : KotlinPlugin() {

    override fun onAsyncLoad() {
    }

    override fun onEnable() {

        SimpleLogger.prefix = "&a[&6${javaPlugin.description.name}&a]&r ".toColor()
        SimpleYAMLConfig.loadMessage = "&7配置文件 &6%s &7已重载!"
        SimpleYAMLConfig.saveMessage = "&7配置文件 &6%s &7已保存!"
        info("&a插件已启用!")
    }

    override fun onAsyncEnable() {
        ConfigurationSerialization.registerClass(KitYml::class.java)
        Config.load(false)
        Config.reload()
        KitsYml.load(false)
        command()
        //如果使用命令模块，取消注释
        CommandBuilder.onEnable()
    }

    override fun onDisable() {
        Config.closeDB()
        //如果使用命令模块，取消注释
        CommandBuilder.onDisable()
//        //如果使用配置模块，取消注销
        ConfigWatcher.onDisable()

        info("&6插件已卸载!")
    }

}