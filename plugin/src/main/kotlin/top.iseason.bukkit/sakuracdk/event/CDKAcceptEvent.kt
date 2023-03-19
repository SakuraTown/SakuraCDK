package top.iseason.bukkit.sakuracdk.event

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.iseason.bukkit.sakuracdk.entity.BaseCDK

class CDKAcceptEvent(
    val player: Player,
    val cdk: BaseCDK
) : Event(true), Cancellable {
    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }

    private var cancel = false
    override fun isCancelled() = cancel

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }
}