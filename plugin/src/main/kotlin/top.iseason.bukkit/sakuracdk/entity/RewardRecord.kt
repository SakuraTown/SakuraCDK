package top.iseason.bukkit.sakuracdk.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


class RewardRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RewardRecord>(RewardRecords)

    var group by RewardRecords.group
    var player by RewardRecords.player
    var count by RewardRecords.count
}

object RewardRecords : IntIdTable() {
    val group = varchar("group", 255)
    val player = varchar("player", 255)
    var count = integer("count")
}