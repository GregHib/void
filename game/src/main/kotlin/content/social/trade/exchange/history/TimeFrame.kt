package content.social.trade.exchange.history

import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

enum class TimeFrame(
    private val unit: TimeUnit,
    private val count: Int,
) {
    Day(TimeUnit.MINUTES, 5),
    Week(TimeUnit.HOURS, 1),
    Month(TimeUnit.HOURS, 6),
    Year(TimeUnit.DAYS, 1);

    /**
     * Returns the start of the time bucket that contains the given timestamp.
     * Buckets are aligned to midnight UTC, and sized based on the TimeFrame
     *
     * Example:
     *  - For [Day] (5 min): 00:07 → 00:05
     *  - For [Week] (1 hr): 02:45 → 02:00
     */
    fun start(millis: Long): Long {
        val zone = ZoneOffset.UTC
        val instant = Instant.ofEpochMilli(millis)
        val dateTime = instant.atZone(zone)
        val midnight = dateTime.toLocalDate().atStartOfDay(zone)
        val bucketStart = when (unit) {
            TimeUnit.MINUTES -> midnight.plusMinutes((dateTime.minute / count * count).toLong()).withSecond(0).withNano(0)
            TimeUnit.HOURS -> midnight.plusHours((dateTime.hour / count * count).toLong()).withMinute(0).withSecond(0).withNano(0)
            TimeUnit.DAYS -> midnight
            else -> error("Unsupported unit: $unit")
        }
        return bucketStart.toInstant().toEpochMilli()
    }
}