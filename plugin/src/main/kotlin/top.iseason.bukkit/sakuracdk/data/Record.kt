package top.iseason.bukkit.sakuracdk.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Record(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Record>(Records)

    var uid by Records.uid
    var cdk by Records.cdk
    var group by Records.group
    var acceptTime by Records.acceptTime
}