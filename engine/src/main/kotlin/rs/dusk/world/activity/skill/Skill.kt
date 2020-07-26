package rs.dusk.world.activity.skill

enum class Skill {
    ATTACK,
    DEFENCE,
    STRENGTH,
    CONSTITUTION,
    RANGE,
    PRAYER,
    MAGIC,
    COOKING,
    WOODCUTTING,
    FLETCHING,
    FISHING,
    FIREMAKING,
    CRAFTING,
    SMITHING,
    MINING,
    HERBLORE,
    AGILITY,
    THIEVING,
    SLAYER,
    FARMING,
    RUNECRAFTING,
    HUNTER,
    CONSTRUCTION,
    SUMMONING,
    DUNGEONEERING;

    val combat: Boolean = ordinal <= 6 || ordinal == 23
}