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
    Dungeoneering,
    ;

    fun maximum(): Int = if (this == Dungeoneering) {
        120
    } else if (this == Constitution) {
        990
    } else {
        99
    }

    companion object {
        val all = entries.toTypedArray()
        val nonHealth = entries.filter { it != Constitution }.toTypedArray()
        val count = all.size

        private val skills = mapOf(
            "Attack" to Attack,
            "Defence" to Defence,
            "Strength" to Strength,
            "Constitution" to Constitution,
            "Ranged" to Ranged,
            "Prayer" to Prayer,
            "Magic" to Magic,
            "Cooking" to Cooking,
            "Woodcutting" to Woodcutting,
            "Fletching" to Fletching,
            "Fishing" to Fishing,
            "Firemaking" to Firemaking,
            "Crafting" to Crafting,
            "Smithing" to Smithing,
            "Mining" to Mining,
            "Herblore" to Herblore,
            "Agility" to Agility,
            "Thieving" to Thieving,
            "Slayer" to Slayer,
            "Farming" to Farming,
            "Runecrafting" to Runecrafting,
            "Hunter" to Hunter,
            "Construction" to Construction,
            "Summoning" to Summoning,
            "Dungeoneering" to Dungeoneering,
        )

        fun of(name: String): Skill? = skills[name]
    }
}
