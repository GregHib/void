package world.gregs.voidps.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class MiscLog : Log {
    Bark,
    Blisterwood_Logs;

    override val id: String = name.toLowerCase()
}