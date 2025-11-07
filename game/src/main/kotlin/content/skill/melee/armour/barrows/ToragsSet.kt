package content.skill.melee.armour.barrows

import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
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

        combatAttack("melee", handler = ::attack)
        npcCombatAttack(style = "melee", handler = ::attack)
    }

    fun attack(source: Character, attack: world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack) {
        val (target, damage, _, weapon) = attack
        if (damage <= 0 || target !is Player || !weapon.id.startsWith("torags_hammers") || !source.contains("torags_set_effect") || random.nextInt(4) != 0) {
            return
        }
        if (target.runEnergy > 0) {
            target.runEnergy -= target.runEnergy / 5
            target.gfx("torags_effect")
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
