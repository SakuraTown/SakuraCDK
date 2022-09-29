package top.iseason.bukkit.sakuracdk.data

import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.jetbrains.exposed.sql.deleteAll
import top.iseason.bukkittemplate.config.DatabaseConfig
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.Comment
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.annotations.Key
import top.iseason.bukkittemplate.config.dbTransaction
import top.iseason.bukkittemplate.debug.warn

@FilePath("kits.yml")
object KitsYml : SimpleYAMLConfig() {

    @Key
    @Comment("是否自动更新")
    var auto_update = true

    @Key("kits")
    var kitsSection: MemorySection = YamlConfiguration()
    var kits = mutableMapOf<String, KitYml>()
    val suggestKits: (CommandSender.() -> Collection<String>) = { kits.keys }

    override fun onLoaded(section: ConfigurationSection) {
        isAutoUpdate = auto_update
        if (!DatabaseConfig.isConnected) {
            warn("&c数据异常，请联系管理员!")
            return@onLoaded
        }
        kits.clear()
        kitsSection.getKeys(false).forEach {
            val keys = kitsSection.getConfigurationSection(it)!!.getValues(true)
            kits[it] = KitYml.deserialize(keys) ?: return@forEach
        }
//        uploadData()
    }

    override fun onSaved(section: ConfigurationSection) {
        kitsSection.getKeys(false).forEach { kitsSection[it] = null }
        kits.forEach { (t, u) ->
            kitsSection[t] = u.serialize()
        }
    }

    fun uploadData() {
        for (kit in kits.values) {
            kit.updateDataBase()
        }
    }

    //从数据库下载数据到本地
    fun downloadData() {
        try {
            dbTransaction {
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
        dbTransaction {
            Kits.deleteAll()
        }
        uploadData()
    }
}