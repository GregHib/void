package world.gregs.voidps.world.activity.skill.mining

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.holdsItem

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
        Item("bronze_pickaxe")
    )


    fun best(player: Player): Item? {
        return pickaxes.firstOrNull { pickaxe -> player.hasRequirementsToUse(pickaxe, skills = setOf(Skill.Mining, Skill.Firemaking)) && player.holdsItem(pickaxe.id) }
    }
}