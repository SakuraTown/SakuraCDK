package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkit.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkit.sakuracdk.SakuraCDK
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

@FilePath("cdk.yml")
object CDKsYml : SimpleYAMLConfig() {

    //储存所有cdk
    val cdkCache = hashMapOf<String, BaseCDK>()

    override val onLoaded: (ConfigurationSection.() -> Unit) = {
        cdkCache.clear()
        for (key in this.getKeys(false)) {
            val section = getConfigurationSection(key) ?: continue
            val type = section.getString("type") ?: continue
            val cdk: BaseCDK = when (type.lowercase()) {
                "normal" -> NormalCDKYml.fromSection(key, section)
                "random" -> RandomCDKYml.fromSection(key, section)
                else -> null
            } ?: continue
            cdkCache[cdk.id] = cdk
        }
//        uploadAll()
    }

    //将CDK数据上传数据库
    fun uploadAll() {
        try {
            for (cdkGroup in cdkCache.values) {
                cdkGroup.upLoadData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateAllData() {
        transaction {
            NormalCDKs.deleteAll()
            RandomCDKs.deleteAll()
        }
        for (cdkGroup in cdkCache.values) {
            cdkGroup.upLoadData()
        }
    }

    /**
     * 将随机key同步至数据库
     */
    fun updateRandomData() {
        transaction {
            CDKs.deleteWhere { CDKs.type eq "random" }
            val file = File(SakuraCDK.javaPlugin.dataFolder, "random")
            if (!file.exists()) return@transaction
            file.listFiles()?.forEach {
                if (!it.isFile) return@forEach
                var name = it.name
                if (!name.endsWith(".txt")) return@forEach
                name = name.removeSuffix(".txt")
                BufferedReader(FileReader(it))
                    .lines()
                    .forEach { cdk ->
                        val cdkStr = cdk.trim()
                        if (cdkStr.isBlank()) return@forEach
                        CDKs.insert { cdks ->
                            cdks[CDKs.id] = cdkStr
                            cdks[group] = name
                            cdks[type] = "random"
                        }
                    }
            }
        }
    }

    //将CDK数据下载至本地
    fun downloadAll() {
        transaction {
            for (normalCDK in NormalCDK.all()) {
                val value = normalCDK.id.value
                var section = this@CDKsYml.config.getConfigurationSection(value)
                if (section == null) {
                    section = this@CDKsYml.config.createSection(value)
                }
                normalCDK.toYml().toSection(section)
            }
            for (randomCDK in RandomCDK.all()) {
                val value = randomCDK.id.value
                var section = this@CDKsYml.config.getConfigurationSection(value)
                if (section == null) {
                    section = this@CDKsYml.config.createSection(value)
                }
                randomCDK.toYml().toSection(section)
            }
        }
        (this@CDKsYml.config as YamlConfiguration).save(configPath)
    }

    fun onDisable() {
        for (cdkGroup in cdkCache.values) {
            if (cdkGroup is RandomCDKYml) cdkGroup.saveCDK()
        }
    }
}