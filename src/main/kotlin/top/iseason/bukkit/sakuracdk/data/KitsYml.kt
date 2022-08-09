package top.iseason.bukkit.sakuracdk.data

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.Comment
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key
import top.iseason.bukkit.bukkittemplate.debug.info
import top.iseason.bukkit.bukkittemplate.debug.warn
import top.iseason.bukkit.sakuracdk.Config

@FilePath("kits.yml")
object KitsYml : SimpleYAMLConfig() {

    @Key
    @Comment("是否自动更新")
    var auto_Update = true

    @Key
    var kits = hashMapOf<String, KitYml>()

    val suggestKits: (CommandSender.() -> Collection<String>) = { kits.keys }

    override val onLoaded: (ConfigurationSection.() -> Unit) = onLoaded@{
        isAutoUpdate = auto_Update
        if (!Config.isConnected) {
            warn("&c数据异常，请联系管理员!")
            return@onLoaded
        }
        for (kit in kits.values) {
            try {
                kit.updateDataBase()
            } catch (e: Exception) {
                info("&c更新kit: ${kit.id} 失败!")
            }
        }
    }
}