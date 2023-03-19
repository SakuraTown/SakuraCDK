package top.iseason.bukkit.sakuracdk.config

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.sakuracdk.entity.Rewards
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key

@FilePath("rewards.yml")
object RewardsYml : SimpleYAMLConfig() {

    @Key
    @Comment("", "玩家登陆后检查奖励的延迟")
    var loginDelay = 100L

    @Key("rewards")
    @Comment(
        "",
        "给某种cdk绑定玩家，当cdk被领取时给予玩家奖励",
        "在默认给的例子中:",
        "groupId 是cdk的id",
        "以下是 groupId 下的条目",
        "players 是cdk被领取之后影响的玩家ID",
        "commands 是cdk被领取之后运行的命令，只运行一次 %player%:玩家id %remain%:未发放奖励的次数",
        "commands-repeat 是cdk被领取之后运行的命令，重复运行未发放奖励的次数 %player%:玩家id",
        "message 是发放奖励时的消息 %player%:玩家id"
    )
    var rewardsYml = yml {
        "groupId" to {
            "players" to listOf("Iceason")
            "commands" to listOf("eco give %player% %total% %remain%")
            "commands-repeat" to listOf("say hello %player%")
            "message" to listOf("&a你的cdk被领取了 %times% 次")
        }
    }
    val rewards = mutableMapOf<String, Rewards>()
    override fun onLoaded(section: ConfigurationSection) {
        rewards.clear()
        for (key in rewardsYml.getKeys(false)) {
            rewards[key] = Rewards.fromSection(rewardsYml.getConfigurationSection(key)!!, key)
        }
    }
}

