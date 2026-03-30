package content.skill.mining

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.carriesItem

object Pickaxe {
    private val pickaxes = listOf(
        Item("dragon_pickaxe"),
        Item("volatile_clay_pickaxe"),
        Item("sacred_clay_pickaxe"),
        Item("inferno_adze"),
        Item("rune_pickaxe"),
        Item("adamant_pickaxe"),
        Item("mithril_pickaxe"),
        Item("steel_pickaxe"),
        Item("iron_pickaxe"),
        Item("bronze_pickaxe"),
    )

    fun best(player: Player): Item? = pickaxes.firstOrNull { pickaxe -> player.hasRequirementsToUse(pickaxe, skills = setOf(Skill.Mining, Skill.Firemaking)) && player.carriesItem(pickaxe.id) }

    fun bestRequirements(player: Player, message: Boolean = false): Item? {
        val pickaxe = best(player)
        if (pickaxe == null) {
            if (message) {
                player.message("You need a pickaxe to mine this rock.")
                player.message("You do not have a pickaxe which you have the mining level to use.")
            }
            return null
        }
        if (player.hasRequirementsToUse(pickaxe, message, setOf(Skill.Mining, Skill.Firemaking))) {
            return pickaxe
        }
        return null
    }
}
