package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NormalCDK(
    id: String,
    val cdk: String,
    val amount: Int,
    expire: LocalDateTime,
    kits: List<KitYml>
) : BaseCDK(id, expire, kits) {

    override fun toSection(section: ConfigurationSection) {
        section["cdk"] = cdk
        section["amount"] = amount
        section["expire"] = expire.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        section["kits"] = kits.map { it.id }
    }

    override fun getCDKs(): List<String> = listOf(cdk)
    override fun allowRepeat(): Boolean = false


    companion object {
        fun fromSection(id: String, section: ConfigurationSection): NormalCDK? {
            val cdk = section.getString("cdk") ?: return null
            if (!section.contains("amount")) return null
            val amount = section.getInt("amount")
            val expireStr = section.getString("expire") ?: return null
            val expire = try {
                LocalDateTime.parse(expireStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                return null
            }
            val kitsStr = section.getStringList("kits")
            val mutableListOf = mutableListOf<KitYml>()
            for (kits in kitsStr) {
                mutableListOf.add(KitsYml.kits[kits] ?: continue)
            }
            return NormalCDK(id, cdk, amount, expire, mutableListOf)
        }
    }
}