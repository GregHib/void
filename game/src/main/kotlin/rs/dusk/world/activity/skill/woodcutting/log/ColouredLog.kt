package rs.dusk.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class ColouredLog : Log {
    Blue_Logs,
    Green_Logs,
    Purple_Logs,
    Red_Logs,
    White_Logs;

    override val id: String = name.toLowerCase()
}