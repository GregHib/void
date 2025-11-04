package content.skill.melee.armour.barrows

import content.entity.combat.hit.characterCombatAttack
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.type.random

class ToragsSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("torags_set_effect", true)
            }
        }

        for (slot in BarrowsArmour.slots) {
            itemAdded("torags_*", "worn_equipment", slot, ::added)
            itemRemoved("torags_*", "worn_equipment", slot, ::removed)
        }

        characterCombatAttack("torags_hammers*", "melee") { character ->
            if (damage <= 0 || target !is Player || !character.contains("torags_set_effect") || random.nextInt(4) != 0) {
                return@characterCombatAttack
            }
            if (target.runEnergy > 0) {
                target.runEnergy -= target.runEnergy / 5
                target.gfx("torags_effect")
            }
        }
    }

    fun added(player: Player, update: ItemAdded) {
        if (player.hasFullSet()) {
            player["torags_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("torags_set_effect")
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "torags_hammers",
        "torags_helm",
        "torags_platebody",
        "torags_platelegs",
    )
}
