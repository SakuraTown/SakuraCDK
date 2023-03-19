package top.iseason.bukkit.sakuracdk.config

import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key

@Key
@FilePath("lang.yml")
object Lang : top.iseason.bukkittemplate.config.Lang() {

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

}