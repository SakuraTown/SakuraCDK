package top.iseason.bukkit.sakuracdk.config

import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.Comment
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key

@FilePath("config.yml")
object Config : SimpleYAMLConfig() {
    @Key
    @Comment("", "随机cdk模板，X将被替换为随机字符")
    var cdkTemplate = "XXXX-XXXXXXXX"
}