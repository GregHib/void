package rs.dusk.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class PyreLog : Log {
    Pyre_Logs,
    Oak_Pyre_Logs,
    Willow_Pyre_Logs,
    Teak_Pyre_Logs,
    Arctic_Pyre_Logs,
    Maple_Pyre_Logs,
    Mahogany_Pyre_Logs,
    Eucalyptus_Pyre_Logs,
    Yew_Pyre_Logs,
    Magic_Pyre_Logs;

    override val id: String = name.toLowerCase()
}