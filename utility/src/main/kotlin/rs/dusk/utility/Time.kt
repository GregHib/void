package rs.dusk.utility

object Time {
    fun minutesToTicks(minutes: Int) = (minutes * 60000) / 600
    fun ticksToSeconds(ticks: Long) = (ticks * 600) / 1000
}