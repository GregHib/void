package rs.dusk.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class JadinkoRoot : Log {
    Straight_Root,
    Curly_Root;

    override val id: String = name.toLowerCase()
}