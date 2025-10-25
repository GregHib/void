package world.gregs.voidps.engine.timer

import java.util.concurrent.TimeUnit

class TickTime {
    fun toNanos(duration: Int) = TimeUnit.MILLISECONDS.toNanos(toMillis(duration))
    fun toMicros(duration: Int) = TimeUnit.MILLISECONDS.toMicros(toMillis(duration))
    fun toMillis(duration: Int) = duration * 600L
    fun toSeconds(duration: Int) = TimeUnit.MILLISECONDS.toSeconds(toMillis(duration))
    fun toMinutes(duration: Int) = TimeUnit.MILLISECONDS.toMinutes(toMillis(duration))
    fun toHours(duration: Int) = TimeUnit.MILLISECONDS.toHours(toMillis(duration))
    fun toDays(duration: Int) = TimeUnit.MILLISECONDS.toDays(toMillis(duration))
    fun toClientTicks(duration: Int) = duration * 30
}

class ClientTickTime {
    fun toNanos(duration: Int) = TimeUnit.MILLISECONDS.toNanos(toMillis(duration))
    fun toMicros(duration: Int) = TimeUnit.MILLISECONDS.toMicros(toMillis(duration))
    fun toMillis(duration: Int) = duration * 20L
    fun toSeconds(duration: Int) = TimeUnit.MILLISECONDS.toSeconds(toMillis(duration))
    fun toMinutes(duration: Int) = TimeUnit.MILLISECONDS.toMinutes(toMillis(duration))
    fun toHours(duration: Int) = TimeUnit.MILLISECONDS.toHours(toMillis(duration))
    fun toDays(duration: Int) = TimeUnit.MILLISECONDS.toDays(toMillis(duration))
    fun toTicks(duration: Int) = duration / 30
}

val TICKS = TickTime()

val CLIENT_TICKS = ClientTickTime()

fun TimeUnit.toTicks(duration: Int): Int = (toMillis(duration.toLong()) / 600).toInt()

fun epochSeconds() = TimeUnit.MILLISECONDS.toSeconds(currentTime()).toInt()

fun epochMinutes() = TimeUnit.MILLISECONDS.toMinutes(currentTime()).toInt()

private var currentTime: () -> Long = { System.currentTimeMillis() }

fun setCurrentTime(time: () -> Long) {
    currentTime = time
}

fun resetCurrentTime() {
    currentTime = { System.currentTimeMillis() }
}
