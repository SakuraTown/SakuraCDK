package top.iseason.bukkit.sakuracdk

import java.time.LocalDateTime
import java.util.regex.Pattern

object Utils {
    private val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()

    //获取一定时间间隔后的时间，例如 "1Y2M3W4d5h6m7s" 可随意组合
    fun parseTimeAfter(str: String): LocalDateTime {
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

    //获取一定时间间隔后的时间，例如 "1Y2M3W4d5h6m7s" 可随意组合
    fun parseTimeBefore(str: String): LocalDateTime {
        val compile = Pattern.compile("([0-9]+)([a-zA-Z]+)")
        val matcher = compile.matcher(str)
        var now = LocalDateTime.now()
        while (matcher.find()) {
            val num = matcher.group(1).toLong()
            when (matcher.group(2)) {
                "d" -> now = now.minusDays(num)
                "h" -> now = now.minusHours(num)
                "m" -> now = now.minusMinutes(num)
                "s" -> now = now.minusSeconds(num)
                "Y" -> now = now.minusYears(num)
                "M" -> now = now.minusMonths(num)
                "W" -> now = now.minusWeeks(num)
            }
        }
        return now
    }

    //将字符串替换为随机字符串
    fun replaceRandom(cdk: String): String {
        val cs = cdk.toCharArray()
        for (i in cs.indices) {
            if (cs[i] == 'X') {
                cs[i] = chars.random()
            }
        }
        return String(cs)
    }
}