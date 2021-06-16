package world.gregs.voidps.engine.entity.character.player.skill

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

    fun maximum(): Int = if (this == Dungeoneering) 120 else if (this == Constitution) 990 else 99

    companion object {
        val all = values()
        val count = all.size
    }
}