package top.iseason.bukkit.sakuracdk.data

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.time.LocalDateTime

abstract class BaseCDK(
    val id: String,
    val expire: LocalDateTime,
    val kits: List<KitYml>
) {
    abstract fun toSection(section: ConfigurationSection)
    abstract fun getCDKs(): List<String>
    abstract fun allowRepeat(): Boolean

    /**
     * 检查是否过期
     * @return true 表示过期
     */
    fun checkExpire(): Boolean = LocalDateTime.now().isAfter(expire)
    fun applyPlayer(player: Player): Boolean {
        var isSuccess = false
        for (kit in kits) {
            if (kit.isExpire()) continue
            isSuccess = true
            kit.applyPlayer(player)
        }
        return isSuccess
    }
}