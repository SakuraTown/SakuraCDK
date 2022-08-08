package top.iseason.bukkit.sakuracdk

import java.time.LocalDateTime
import java.util.regex.Pattern

object Utils {
    //获取一定时间间隔后的时间，例如 "1Y2M3W4d5h6m7s" 可随意组合
    fun parseTime(str: String): LocalDateTime {
        val compile = Pattern.compile("([0-9]+)([a-zA-Z]+)")
        val matcher = compile.matcher(str)
        var now = LocalDateTime.now()
        while (matcher.find()) {
            val num = matcher.group(1).toLong()
            when (matcher.group(2)) {
                "d" -> now = now.plusDays(num)
                "h" -> now = now.plusHours(num)
                "m" -> now = now.plusMinutes(num)
                "s" -> now = now.plusSeconds(num)
                "Y" -> now = now.plusYears(num)
                "M" -> now = now.plusMonths(num)
                "W" -> now = now.plusWeeks(num)
            }
        }
        return now
    }
}