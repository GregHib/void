package content.skill.melee.armour

import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class RingOfRecoil : Script {

    init {
        combatDamage { (source, type, damage) ->
            if (source == this || type == "deflect" || type == "poison" || type == "disease" || type == "healed" || damage < 1) {
                return@combatDamage
            }
            if (equipped(EquipSlot.Ring).id != "ring_of_recoil") {
                return@combatDamage
            }
            if (source is NPC && source.def["immune_deflect", false]) {
                return@combatDamage
            }
            val charges = equipment.charges(this, EquipSlot.Ring.index)
            val deflect = (10 + (damage / 10)).coerceAtMost(charges)
            if (equipment.discharge(this, EquipSlot.Ring.index, deflect)) {
                source.directHit(deflect, "deflect", source = this)
            }
        }

        itemOption("Check", "ring_of_recoil", "worn_equipment") {
            val charges = equipment.charges(this, EquipSlot.Ring.index)
            message("You can inflict $charges more points of damage before a ring will shatter.")
        }
    }
}
