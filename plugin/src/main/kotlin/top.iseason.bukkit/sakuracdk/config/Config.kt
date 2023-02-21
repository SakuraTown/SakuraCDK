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
    @Comment("是否压缩加密物品，压缩完无法直接编辑，只能从命令修改", "1.7不支持编辑物品，请保持此项为true")
    var enciphered = true

    @Key
    @Comment("用户cdk命令冷却时间，单位毫秒")
    var command_cooldown = 3000L

    @Key
    @Comment("用户cdk兑换命令，重启生效")
    var command_name = "sakuracdk"

    @Key
    @Comment("用户cdk兑换命令别名，重启生效")
    var command_alias = listOf("cdk", "scdk")

}