package rs.dusk.world.activity.skill

enum class Skill {
    Attack,
    Defence,
    Strength,
    Constitution,
    Range,
    Prayer,
    Magic,
    Cooking,
    Woodcutting,
    Fletching,
    Fishing,
    Firemaking,
    Crafting,
    Smithing,
    Mining,
    Herblore,
    Agility,
    Thieving,
    Slayer,
    Farming,
    Runecrafting,
    Hunter,
    Construction,
    Summoning,
    Dungeoneering;

    val combat: Boolean = ordinal <= 6 || ordinal == 23
}