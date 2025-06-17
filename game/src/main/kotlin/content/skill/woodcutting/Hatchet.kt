package content.skill.woodcutting

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.holdsItem

object Hatchet {
    private val hatchets = listOf(
        Item("inferno_adze"),
        Item("volatile_clay_hatchet"),
        Item("sacred_clay_hatchet"),
        Item("dragon_hatchet"),
        Item("rune_hatchet"),
        Item("adamant_hatchet"),
        Item("mithril_hatchet"),
        Item("black_hatchet"),
        Item("steel_hatchet"),
        Item("iron_hatchet"),
        Item("bronze_hatchet"),
    )

    fun best(player: Player): Item? = hatchets.firstOrNull { hasRequirements(player, it) && player.holdsItem(it.id) }

    fun hasRequirements(player: Player, hatchet: Item, message: Boolean = false): Boolean = player.hasRequirementsToUse(hatchet, message, setOf(Skill.Firemaking, Skill.Woodcutting))
}
