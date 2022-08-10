package top.iseason.bukkit.sakuracdk.data

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.Comment
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.bukkittemplate.config.annotations.Key
import top.iseason.bukkit.bukkittemplate.debug.warn
import top.iseason.bukkit.sakuracdk.config.Config

@FilePath("kits.yml")
object KitsYml : SimpleYAMLConfig() {

    @Key
    @Comment("是否自动更新")
    var auto_Update = true

    @Key
    var kits = hashMapOf<String, KitYml>()

    val suggestKits: (CommandSender.() -> Collection<String>) = { kits.keys }

    override val onLoaded: (ConfigurationSection.() -> Unit) = onLoaded@{
        isAutoUpdate = auto_Update
        if (!Config.isConnected) {
            warn("&c数据异常，请联系管理员!")
            return@onLoaded
        }
        uploadData()
    }

    fun uploadData() {
        for (kit in kits.values) {
            kit.updateDataBase()
        }
    }

    //从数据库下载数据到本地
    fun downloadData() {
        try {
            transaction {
                val kits = Kit.all()
                KitsYml.kits = hashMapOf()
                for (kit in kits) {
                    KitsYml.kits[kit.id.value] = kit.toKitYml()
                }
                save(false)
            }
        } catch (e: Throwable) {
            return
        }
    }

    fun updateData() {
        transaction {
            Kits.deleteAll()
        }
        uploadData()
    }
}