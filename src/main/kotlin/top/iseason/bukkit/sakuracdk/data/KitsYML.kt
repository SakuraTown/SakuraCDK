package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key

@FilePath("kits.yml")
object KitsYML : SimpleYAMLConfig() {
    @Key
    var kits = mutableListOf<KitYml>()
    override val onLoaded: (ConfigurationSection.() -> Unit) = {
        for (kit in kits) {
            kit.toKitDataBase()
        }
    }
}