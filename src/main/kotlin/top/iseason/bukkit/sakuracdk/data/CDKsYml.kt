package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath

@FilePath("cdk.yml")
object CDKsYml : SimpleYAMLConfig() {

    //储存所有cdk
    val cdkCache = hashMapOf<String, BaseCDK>()
    val cdkGroups = mutableListOf<BaseCDK>()
    override val onLoaded: (ConfigurationSection.() -> Unit) = {
        for (key in this.getKeys(false)) {
            val section = getConfigurationSection(key) ?: continue
            var cdk: BaseCDK? = NormalCDK.fromSection(key, section)
            if (cdk == null) {
                cdk = RandomCDK.fromSection(key, section) ?: continue
            }
            cdkGroups.add(cdk)
            val cdKs = cdk.getCDKs()
            for (key in cdKs) {
                cdkCache[key] = cdk
            }
        }
    }

    fun onDisable() {
        for (cdkGroup in cdkGroups) {
            if (cdkGroup is RandomCDK) cdkGroup.saveCDK()
        }
    }
}