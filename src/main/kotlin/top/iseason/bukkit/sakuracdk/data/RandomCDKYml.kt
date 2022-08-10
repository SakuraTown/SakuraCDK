package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.sakuracdk.SakuraCDK
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.streams.toList

class RandomCDKYml(
    // id
    val group: String,
    expire: LocalDateTime,
    kits: List<KitYml>,
    val cdks: HashSet<String> = hashSetOf(),
) : BaseCDK(group, expire, kits) {

    var allowRepeat = false

    override fun upLoadData() {
        transaction {
            val findById = RandomCDK.findById(group)
            if (findById != null) {
                findById.expire = expire
                findById.kits = getKitsString()
                findById.repeat = allowRepeat
                return@transaction
            }
            RandomCDK.new(group) {
                this.expire = expire
                this.kits = getKitsString()
                this.repeat = allowRepeat
            }
        }
    }


    fun removeCDK(cdk: String) {
        cdks.remove(cdk)
        transaction {
            CDKs.deleteWhere { CDKs.id eq cdk }
        }
    }

    fun saveCDK() {
        if (cdks.isEmpty()) return
        saveTxt(group, cdks)
    }

    override fun toSection(section: ConfigurationSection) {
        section["type"] = "random"
        section["repeat"] = allowRepeat
        section["expire"] = expire.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        section["kits"] = kits.map { it.id }
    }

    override fun getCDKs(): List<String> = cdks.toList()
    override fun allowRepeat(): Boolean = allowRepeat

    companion object {
        fun fromSection(id: String, section: ConfigurationSection): RandomCDKYml? {
            val expireStr = section.getString("expire") ?: return null
            val expire = try {
                LocalDateTime.parse(expireStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                return null
            }
            val kitsStr = section.getStringList("kits")
            val kits = mutableListOf<KitYml>()
            for (str in kitsStr) {
                kits.add(KitsYml.kits[str] ?: continue)
            }
            val file = File(SakuraCDK.javaPlugin.dataFolder, "random${File.separatorChar}${id}.txt")
            var hashSetOf = hashSetOf<String>()
            if (file.exists()) {
                hashSetOf =
                    BufferedReader(FileReader(file))
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .toList()
                        .toHashSet()
            }
            val randomCDK = RandomCDKYml(id, expire, kits, hashSetOf)
            randomCDK.allowRepeat = section.getBoolean("repeat")
            return randomCDK
        }

        fun saveTxt(group: String, cdkList: Collection<String>) {
            val file = File(SakuraCDK.javaPlugin.dataFolder, "random${File.separatorChar}${group}.txt")
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            BufferedWriter(OutputStreamWriter(FileOutputStream(file, false))).use { bw ->
                for (cdk in cdkList) {
                    bw.write(cdk)
                    bw.newLine()
                }
            }
        }
    }

}

// id 为cdk 群组
object RandomCDKs : StringIdTable() {
    //随机cdk的群组
    val expire = datetime("expire").default(LocalDateTime.now())
    val kits = text("kits").default("")
    val repeat = bool("repeat").default(false)
}

class RandomCDK(id: EntityID<String>) : StringEntity(id) {
    companion object : StringEntityClass<RandomCDK>(RandomCDKs)

    var expire by RandomCDKs.expire
    var kits by RandomCDKs.kits
    var repeat by RandomCDKs.repeat

    fun toYml(): RandomCDKYml {
        val kitYmls = mutableListOf<KitYml>()
        var cdks: HashSet<String> = hashSetOf()
        val group = id.value
        transaction {
            for (s in kits.split(";")) {
                val findById = Kit.findById(s) ?: continue
                kitYmls.add(findById.toKitYml())
            }
            cdks = CDKs.slice(CDKs.id).select { CDKs.group eq group }.map { it[CDKs.id].value }.toHashSet()
        }
        val randomCDKYml = RandomCDKYml(group, expire, kitYmls, cdks)
        randomCDKYml.allowRepeat = repeat
        return randomCDKYml
    }
}
