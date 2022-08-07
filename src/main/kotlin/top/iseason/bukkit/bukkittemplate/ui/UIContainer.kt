package top.iseason.bukkit.bukkittemplate.ui

import org.bukkit.entity.HumanEntity

/**
 * 多页UI
 */
@Suppress("unused")
open class UIContainer(
    /**
     * 储存多页数组
     */
    protected val pages: Array<Pageable?>
) {
    // 页码
    val size = pages.size
    protected val viewers = mutableMapOf<HumanEntity, Int>()

    /**
     * 翻页时调用
     * @param from 源页码
     * @param to 目标页码
     */
    open var onPageChanged: ((from: Int, to: Int) -> Unit)? = null

    /**
     * 获取当前页码的UI
     */
    open fun getCurrentPage(player: HumanEntity): BaseUI? {
        val index = viewers[player] ?: 0
        val pageable = pages[index] ?: return null
        pageable.container = this
        return pageable.getUI()
    }

    /**
     * 定位玩家视图到下一页
     */
    open fun nextPage(player: HumanEntity) {
        val next = ((viewers[player] ?: 0) + 1) % size
        player.openInventory(setPage(next, player)?.inventory ?: return)
    }

    /**
     * 定位玩家视图到上一页
     */
    open fun lastPage(player: HumanEntity) {
        var last = (viewers[player] ?: 0) - 1
        if (last < 0) last += size
        player.openInventory(setPage(last, player)?.inventory ?: return)
    }

    /**
     * 定位到第 page 页
     */
    open fun setPage(page: Int, player: HumanEntity): BaseUI? {
        require(page in 0..size) { "page $page is not exist!" }
        onPageChanged?.invoke(viewers[player] ?: 0, page)
        viewers[player] = page
        return getCurrentPage(player)
    }

    /**
     * 为某个玩家打开UI
     */
    fun openFor(player: HumanEntity) {
        require(pages.isNotEmpty()) { "Your pageable ui must possess at lease 1 page" }
        val currentPage = getCurrentPage(player) ?: return
        player.openInventory(currentPage.inventory)
    }

}