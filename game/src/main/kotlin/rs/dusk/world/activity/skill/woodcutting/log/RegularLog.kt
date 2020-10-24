package rs.dusk.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class RegularLog : Log {
    Logs,
    Achey_Tree_Logs,
    Oak_Logs,
    Willow_Logs,
    Teak_Logs,
    Maple_Logs,
    Acadia_Logs,
    Mahogany_Logs,
    Arctic_Pine_Logs,
    Eucalyptus_Logs,
    Yew_Logs,
    Magic_Logs;

    override val id: String = name.toLowerCase()
}