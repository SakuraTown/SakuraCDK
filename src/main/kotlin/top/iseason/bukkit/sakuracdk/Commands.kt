package top.iseason.bukkit.sakuracdk

import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionDefault
import org.jetbrains.exposed.sql.transactions.transaction
import top.iseason.bukkit.bukkittemplate.command.Param
import top.iseason.bukkit.bukkittemplate.command.ParamSuggestCache
import top.iseason.bukkit.bukkittemplate.command.ParmaException
import top.iseason.bukkit.bukkittemplate.command.commandRoot
import top.iseason.bukkit.bukkittemplate.utils.sendColorMessage
import top.iseason.bukkit.bukkittemplate.utils.submit
import top.iseason.bukkit.sakuracdk.data.Config
import top.iseason.bukkit.sakuracdk.data.Kit
import top.iseason.bukkit.sakuracdk.data.KitYml
import top.iseason.bukkit.sakuracdk.data.KitsYml
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun command() {
    commandRoot(
        "sakuracdk",
        alias = arrayOf("cdk", "scdk"),
        description = "使用cdk兑换礼包",
        default = PermissionDefault.TRUE,
        isPlayerOnly = true,
        async = true,
        params = arrayOf(Param("[cdk]"))
    ) {
        onExecute {
            val cdk = getParam<String>(0)
            it.sendColorMessage(cdk)
            true
        }
    }
    commandRoot(
        "sakuracdkadmin",
        alias = arrayOf("cdkadmin"),
        description = "管理cdk",
        default = PermissionDefault.OP,
        isPlayerOnly = true,
        async = true,
        params = arrayOf(Param("[cdk]"))
    ) {
        node("reload", default = PermissionDefault.OP, async = true, description = "重载配置") {
            onExecute {
                Config.reload()
                KitsYml.load()
                true
            }
        }
        node(
            "kit",
            default = PermissionDefault.OP,
            description = "礼包相关命令",
        ) {
            node(
                "create", async = true, description = "创建礼包",
                params = arrayOf(
                    Param("[id]"),
                    Param("[数量]"),
                    Param("[过期时间]", suggest = listOf("1Y2M3W4d5h6m7s", "2022-08-20T16:22:41"))
                )
            ) {
                onExecute {
                    val id = getParam<String>(0)
                    if (KitsYml.kits.containsKey(id)) return@onExecute false
                    val amount = getParam<Int>(1)
                    val time = getParam<String>(2)
                    val expires = try {
                        LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    } catch (e: Exception) {
                        Utils.parseTime(time)
                    }
                    val kitYml = KitYml(id, amount, LocalDateTime.now(), expires)
                    KitsYml.kits[id] = kitYml
                    KitsYml.save(false)
                    it.sendColorMessage("&a创建&6 $id &a成功, 数量 &6 $amount &a,过期时间: &6 $expires")
                    true
                }
                onFailure("创建失败，ID已存在")
            }

            node(
                "delete", async = true, description = "删除礼包",
                params = arrayOf(
                    Param("[id]", suggestRuntime = {
                        KitsYml.kits.keys
                    })
                )
            ) {
                onExecute {
                    val id = getParam<String>(0)
                    if (!KitsYml.kits.containsKey(id)) return@onExecute false
                    KitsYml.kits.remove(id)
                    KitsYml.save(false)
                    onSuccess("&6$id &a已删除")
                    true
                }
                onFailure("&cID不存在!")
            }
            node(
                "addItem", async = true,
                description = "给礼包添加手上物品",
                isPlayerOnly = true,
                params = arrayOf(
                    Param("[id]", suggestRuntime = {
                        KitsYml.kits.keys
                    })
                )
            ) {
                onExecute {
                    val id = getParam<String>(0)
                    val kitYml = KitsYml.kits[id] ?: return@onExecute false
                    val itemInMainHand = (it as Player).inventory.itemInMainHand
                    if (itemInMainHand.type.isAir) throw ParmaException("&6请拿着物品!")
                    kitYml.itemStacksImpl.add(itemInMainHand)
                    KitsYml.save(false)
                    onSuccess("&6物品添加成功")
                    true
                }
                onFailure("&cID不存在!")
            }
            node(
                "give",
                description = "将礼包给予玩家,不会有记录",
                params = arrayOf(
                    Param("[id]", suggestRuntime = {
                        KitsYml.kits.keys
                    }),
                    Param("[player]", suggestRuntime = ParamSuggestCache.playerParam)
                )
            ) {
                onExecute {
                    val id = getParam<String>(0)
                    val kitYml = KitsYml.kits[id] ?: return@onExecute false
                    val player = getParam<Player>(1)
                    kitYml.applyPlayer(player)
                    onSuccess("&a礼包已给予 &6${player.name}")
                    true
                }
                onFailure("&cID不存在!")
            }
        }
        node("download", default = PermissionDefault.OP, async = true, description = "从数据库下载kits数据到yml中") {
            onExecute {
                try {
                    transaction {
                        val kits = Kit.all()
                        KitsYml.kits = hashMapOf()
                        for (kit in kits) {
                            KitsYml.kits[kit.id.value] = kit.toKitYml()
                        }
                        KitsYml.save()
                    }
                } catch (e: Throwable) {
                    return@onExecute false
                }
                true
            }
            onSuccess("&a数据已更新!")
            onFailure("&a数据更新异常!")
        }
    }
}
