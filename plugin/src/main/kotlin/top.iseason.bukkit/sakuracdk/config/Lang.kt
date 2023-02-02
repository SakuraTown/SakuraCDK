package top.iseason.bukkit.sakuracdk.config

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkittemplate.BukkitTemplate
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key
import top.iseason.bukkittemplate.debug.SimpleLogger
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils

@Key
@FilePath("lang.yml")
object Lang : SimpleYAMLConfig() {
    @Comment(
        "",
        "消息留空将不会显示，使用 '\\n' 或换行符 可以换行",
        "支持 & 颜色符号，1.17以上支持16进制颜色代码，如 #66ccff",
        "{0}、{1}、{2}、{3} 等格式为该消息独有的变量占位符",
        "所有消息支持PlaceHolderAPI",
        "以下是一些特殊消息, 大小写不敏感，可以通过 \\n 自由组合",
        "以 [BoardCast] 开头将以广播的形式发送，支持BungeeCord",
        "以 [Actionbar] 开头将发送ActionBar消息",
        "以 [Command] 开头将以消息接收者的身份运行命令",
        "以 [Console] 开头将以控制台的身份运行命令",
        "以 [OP-Command] 开头将赋予消息接收者临时op运行命令 (慎用)"
    )
    var readme = ""

    var prefix = "&a[&6${BukkitTemplate.getPlugin().description.name}&a] &f"
    var command__user_send_too_fast = "&c你发送的太快了，请稍后再试!"
    var command__user_database_closed = "&c数据异常，请联系管理员!"
    var command__user_cdk_unexist = "&cCDK不存在或已过期!"
    var command__user_cdk_is_expire = "&cCDK已过期!"
    var command__user_has_accepted = "&6你已经领取过该礼包了"
    var command__user_normal_brought_out = "&c礼包已领完!"
    var command__user_kit_is_expire = "&c礼包已过期!"
    var command__user_success = "&aCDK兑换成功!"
    var command__debug = "&6Debug模式: {0}"
    var command__reload = "&6配置已重载!"
    override fun onLoaded(section: ConfigurationSection) {
        MessageUtils.defaultPrefix = prefix
        SimpleLogger.prefix = prefix
    }
}