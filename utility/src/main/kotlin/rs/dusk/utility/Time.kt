package rs.dusk.utility

import java.util.concurrent.TimeUnit

class TickTime {
    fun toNanos(duration: Long) = TimeUnit.MILLISECONDS.toNanos(toMillis(duration))
    fun toMicros(duration: Long) = TimeUnit.MILLISECONDS.toMicros(toMillis(duration))
    fun toMillis(duration: Long) = duration * 600
    fun toSeconds(duration: Long) = TimeUnit.MILLISECONDS.toSeconds(toMillis(duration))
    fun toMinutes(duration: Long) = TimeUnit.MILLISECONDS.toMinutes(toMillis(duration))
    fun toHours(duration: Long) = TimeUnit.MILLISECONDS.toHours(toMillis(duration))
    fun toDays(duration: Long) = TimeUnit.MILLISECONDS.toDays(toMillis(duration))
}

val TICKS = TickTime()

fun TimeUnit.toTicks(duration: Long): Long {
    return toMillis(duration) / 600L
}
