package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NormalCDKYml(
    id: String,
    val amount: Int,
    expire: LocalDateTime,
    kits: List<KitYml>
) : BaseCDK(id, expire, kits) {

    override fun upLoadData() {
        transaction {
            val findById = NormalCDK.findById(this@NormalCDKYml.id)
            if (findById != null) {
                findById.expire = expire
                findById.kits = getKitsString()
                findById.amount = amount
                return@transaction
            }
            NormalCDK.new(this@NormalCDKYml.id) {
                this.expire = expire
                this.kits = getKitsString()
                this.amount = amount
            }
        }
    }


    override fun toSection(section: ConfigurationSection) {
        section["type"] = "normal"
        section["amount"] = amount
        section["expire"] = expire.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        section["kits"] = kits.map { it.id }
    }

    override fun getCDKs(): List<String> = listOf(id)
    override fun allowRepeat(): Boolean = false

    companion object {
        fun fromSection(id: String, section: ConfigurationSection): NormalCDKYml? {
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
            return NormalCDKYml(id, amount, expire, mutableListOf)
        }
    }
}

// id 为cdk 群组
object NormalCDKs : StringIdTable() {
    //随机cdk的群组
    val expire = datetime("expire").default(LocalDateTime.now())
    val kits = text("kits").default("")
    val amount = integer("amount").default(10)
}

class NormalCDK(id: EntityID<String>) : StringEntity(id) {
    companion object : StringEntityClass<NormalCDK>(NormalCDKs)

    var expire by NormalCDKs.expire
    var kits by NormalCDKs.kits
    var amount by NormalCDKs.amount

    fun toYml(): NormalCDKYml {
        val kitYmls = mutableListOf<KitYml>()
        transaction {
            for (s in kits.split(";")) {
                val findById = Kit.findById(s) ?: continue
                kitYmls.add(findById.toKitYml())
            }
        }
        return NormalCDKYml(id.value, amount, expire, kitYmls)
    }
}
