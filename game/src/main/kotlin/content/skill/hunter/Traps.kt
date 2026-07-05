package content.skill.hunter

object Traps {
    fun max(level: Int, max: Int) = (1 + level / 20).coerceAtMost(max)
}