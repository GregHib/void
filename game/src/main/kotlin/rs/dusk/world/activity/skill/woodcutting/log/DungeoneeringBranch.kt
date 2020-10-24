package rs.dusk.world.activity.skill.woodcutting.log

@Suppress("EnumEntryName")
enum class DungeoneeringBranch : Log {
    Tangle_Gum_Branches,
    Seeping_Elm_Branches,
    Blood_Spindle_Branches,
    Utuku_Branches,
    Spinebeam_Branches,
    Bovistrangler_Branches,
    Thigat_Branches,
    Corpsethorn_Branches,
    Entgallow_Branches,
    Grave_creeper_Branches;

    override val id: String = name.toLowerCase()
}