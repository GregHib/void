package content.skill.melee.armour.barrows

import content.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.type.random

class KarilsSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("karils_set_effect", true)
            }
        }

        for (slot in BarrowsArmour.slots) {
            itemAdded("karils_*", "worn_equipment", slot, ::added)
            itemRemoved("karils_*", "worn_equipment", slot, ::removed)
        }

        characterCombatAttack("karils_crossbow*", "range") { character ->
            if (damage <= 0 || target !is Player || !character.contains("karils_set_effect") || random.nextInt(4) != 0) {
                return@characterCombatAttack
            }
            if (target.levels.drain(Skill.Agility, multiplier = 0.20) < 0) {
                target.gfx("karils_effect")
            }
        }
    }

    fun added(player: Player, update: ItemAdded) {
        if (player.hasFullSet()) {
            player["karils_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("karils_set_effect")
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "karils_crossbow",
        "karils_coif",
        "karils_top",
        "karils_skirt",
    )
}
