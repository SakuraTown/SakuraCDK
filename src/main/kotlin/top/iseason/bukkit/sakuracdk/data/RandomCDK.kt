package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import top.iseason.bukkit.sakuracdk.SakuraCDK
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.streams.toList

class RandomCDK(
    id: String,
    val file: String,
    expire: LocalDateTime,
    kits: List<KitYml>,
    val cdks: HashSet<String> = hashSetOf(),
) : BaseCDK(id, expire, kits) {

    var allowRepeat = false

    fun removeCDK(cdk: String) {
        cdks.remove(cdk)
    }

    fun saveCDK() {
        if (cdks.isEmpty()) return
        val file = File(SakuraCDK.javaPlugin.dataFolder, "random${File.separatorChar}${file}.txt")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, false))).use { bw ->
            for (cdk in cdks) {
                bw.write(cdk)
                bw.newLine()
            }
        }
    }

    override fun toSection(section: ConfigurationSection) {
        section["file"] = id
        section["repeat"] = allowRepeat
        section["expire"] = expire.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        section["kits"] = kits.map { it.id }
    }

    override fun getCDKs(): List<String> = cdks.toList()
    override fun allowRepeat(): Boolean = allowRepeat

    companion object {
        fun fromSection(id: String, section: ConfigurationSection): RandomCDK? {
            val path = section.getString("file") ?: return null
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
            val file = File(SakuraCDK.javaPlugin.dataFolder, "random${File.separatorChar}${path}.txt")
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
            val randomCDK = RandomCDK(id, path, expire, kits, hashSetOf)
            randomCDK.allowRepeat = section.getBoolean("repeat")
            return randomCDK
        }
    }
}