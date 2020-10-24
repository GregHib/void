package rs.dusk.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class CursedLog : Log {
    Cursed_Willow_Logs,
    Cursed_Magic_Logs;

    override val id: String = name.toLowerCase()
}