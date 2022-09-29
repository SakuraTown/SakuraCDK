package top.iseason.bukkit.sakuracdk.config

import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key

@FilePath("config.yml")
object Config : SimpleYAMLConfig() {
    @Key
    @Comment("", "随机cdk模板，X将被替换为随机字符")
    var cdkTemplate = "XXXX-XXXXXXXX"

    //加密数据
    @Key
    @Comment("是否压缩加密物品，压缩完无法直接编辑，只能从命令修改")
    var enciphered = true

}