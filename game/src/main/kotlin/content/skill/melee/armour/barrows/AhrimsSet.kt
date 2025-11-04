package content.skill.melee.armour.barrows

import content.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.type.random

class AhrimsSet : Script {

    init {
        playerSpawn {
            if (hasFullSet()) {
                set("ahrims_set_effect", true)
            }
        }

        for (slot in BarrowsArmour.slots) {
            itemAdded("ahrims_*", "worn_equipment", slot, ::added)
            itemRemoved("ahrims_*", "worn_equipment", slot, ::removed)
        }

        characterCombatAttack(type = "magic") { character ->
            if (damage <= 0) {
                return@characterCombatAttack
            }
            if (!character.contains("ahrims_set_effect") || random.nextInt(4) != 0) {
                return@characterCombatAttack
            }
            val drain = target.levels.drain(Skill.Strength, 5)
            if (drain < 0) {
                target.gfx("ahrims_effect")
            }
        }
    }

    fun added(player: Player, update: ItemAdded) {
        if (player.hasFullSet()) {
            player["ahrims_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("ahrims_set_effect")
    }

    fun Player.hasFullSet() = BarrowsArmour.hasSet(
        this,
        "ahrims_staff",
        "ahrims_hood",
        "ahrims_robe_top",
        "ahrims_robe_skirt",
    )
}
