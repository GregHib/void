package world.gregs.voidps.engine.entity.character.player.skill

enum class Skill {
    Attack,
    Defence,
    Strength,
    Constitution,
    Ranged,
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

    fun maximum(): Int = if (this == Dungeoneering) 120 else if (this == Constitution) 990 else 99

    companion object {
        val all = entries.toTypedArray()
        val count = all.size
    }
}