package top.iseason.bukkit.sakuracdk.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import top.iseason.bukkit.sakuracdk.entity.*
import top.iseason.bukkit.sakuracdk.entity.NormalCDKYml
import top.iseason.bukkittemplate.config.SimpleYAMLConfig
import top.iseason.bukkittemplate.config.annotations.FilePath
import top.iseason.bukkittemplate.config.dbTransaction

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

    fun updateAllData() {
        dbTransaction {
            CDKs.deleteAll()
            updateNormalDataT()
            updateRandomDataT()
        }
    }

    /**
     * 将配置的随机key同步至数据库
     */
    fun updateRandomData() {
        dbTransaction {
            CDKs.deleteWhere { type eq "random" }
            updateRandomDataT()
        }
    }

    fun updateRandomDataT() {
        val values = cdkCache.values
        val randoms = values.filterIsInstance<RandomCDKYml>()
        RandomCDKs.deleteAll()
        val hashMapOf = hashMapOf<String, Set<String>>()
        RandomCDKs.batchInsert(randoms) {
            this[RandomCDKs.id] = it.id
            this[RandomCDKs.expire] = it.expire
            this[RandomCDKs.kits] = it.getKitsString()
            this[RandomCDKs.repeat] = it.allowRepeat()
            hashMapOf[it.id] = it.cdkSet
        }
        val flatMap = hashMapOf.flatMap { (group, cdks) ->
            cdks.map { it to group }
        }
        CDKs.batchInsert(flatMap) {
            this[CDKs.id] = it.first
            this[CDKs.group] = it.second
            this[CDKs.type] = "random"
        }
    }

    /**
     * 将配置的随机key同步至数据库
     */
    fun updateNormalData() {
        dbTransaction {
            CDKs.deleteWhere { type eq "normal" }
            updateNormalDataT()
        }
    }

    fun updateNormalDataT() {
        val values = cdkCache.values
        val normals = values.filterIsInstance<NormalCDKYml>()
        NormalCDKs.deleteAll()
        NormalCDKs.batchInsert(normals) {
            this[NormalCDKs.id] = it.id
            this[NormalCDKs.expire] = it.expire
            this[NormalCDKs.kits] = it.getKitsString()
            this[NormalCDKs.amount] = it.amount
        }
        CDKs.batchInsert(normals) {
            this[CDKs.id] = it.id
            this[CDKs.group] = it.id
            this[CDKs.type] = "normal"
        }

    }

    //将CDK配置下载至本地
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