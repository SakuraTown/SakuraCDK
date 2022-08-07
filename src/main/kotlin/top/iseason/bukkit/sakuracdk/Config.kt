package top.iseason.bukkit.sakuracdk

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.Comment
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key
import top.iseason.bukkit.sakuracdk.data.Kit
import java.time.LocalDateTime

@FilePath("config.yml")
object Config : SimpleYAMLConfig() {


    override val onLoaded: ConfigurationSection.() -> Unit = {
//        println("loaded")
    }

    override val onSaved: (ConfigurationSection.() -> Unit) = {
//        println("saved")
    }

}