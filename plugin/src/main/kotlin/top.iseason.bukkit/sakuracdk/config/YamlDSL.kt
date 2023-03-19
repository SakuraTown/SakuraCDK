package top.iseason.bukkit.sakuracdk.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class YamlDsl(val yml: ConfigurationSection) {
    infix fun String.to(any: Any) {
        yml.set(this, any)
    }

    infix fun String.to(action: YamlDsl.() -> Unit) = YamlDsl(yml.createSection(this)).apply {
        action(this)
    }.yml
}

fun yml(action: YamlDsl.() -> Unit) = YamlDsl(YamlConfiguration()).apply {
    action(this)
}.yml


