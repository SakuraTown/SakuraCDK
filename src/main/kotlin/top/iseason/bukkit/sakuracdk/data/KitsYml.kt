package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.Comment
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key
import top.iseason.bukkit.bukkittemplate.debug.info

@FilePath("kits.yml")
object KitsYml : SimpleYAMLConfig() {

    @Key
    @Comment("是否自动更新")
    var auto_Update = true

    @Key
    var kits = hashMapOf<String, KitYml>()
    override val onLoaded: (ConfigurationSection.() -> Unit) = {
        isAutoUpdate = auto_Update
        for (kit in kits.values) {
            try {
                kit.updateDataBase()
            } catch (e: Exception) {
                info("&c更新kit: ${kit.id} 失败!")
            }
        }
    }
}