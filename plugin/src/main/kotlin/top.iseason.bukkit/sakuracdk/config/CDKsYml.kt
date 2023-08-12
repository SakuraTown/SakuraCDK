package top.iseason.bukkit.sakuracdk.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import top.iseason.bukkit.sakuracdk.SakuraCDK
import top.iseason.bukkit.sakuracdk.entity.*
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.dbTransaction
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

@FilePath("cdk.yml")
object CDKsYml : SimpleYAMLConfig() {

    //储存所有cdk
    val cdkCache = hashMapOf<String, BaseCDK>()

    override fun onLoaded(section: ConfigurationSection) {
        cdkCache.clear()
        for (key in section.getKeys(false)) {
            val sec = section.getConfigurationSection(key) ?: continue
            val type = sec.getString("type") ?: continue
            val cdk: BaseCDK = when (type.lowercase()) {
                "normal" -> NormalCDKYml.fromSection(key, sec)
                "random" -> RandomCDKYml.fromSection(key, sec)
                else -> null
            } ?: continue
            cdkCache[cdk.id] = cdk
        }
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
        dbTransaction {
            NormalCDKs.deleteAll()
            RandomCDKs.deleteAll()
        }
        uploadAll()
    }

    /**
     * 将随机key同步至数据库
     */
    fun updateRandomData() {
        dbTransaction {
            CDKs.deleteWhere { type eq "random" }
            val file = File(SakuraCDK.javaPlugin.dataFolder, "random")
            if (!file.exists()) return@dbTransaction
            file.listFiles()?.forEach {
                if (!it.isFile) return@forEach
                var name = it.name
                if (!name.endsWith(".txt")) return@forEach
                name = name.removeSuffix(".txt")
                BufferedReader(FileReader(it)).use { br ->
                    br.lines().forEach forEach2@{ cdk ->
                        val cdkStr = cdk.trim()
                        if (cdkStr.isBlank()) return@forEach2
                        CDKs.insert { cdks ->
                            cdks[CDKs.id] = cdkStr
                            cdks[group] = name
                            cdks[type] = "random"
                        }
                    }
                }
            }
        }
    }

    //将CDK数据下载至本地
    fun downloadAll() {
        dbTransaction {
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