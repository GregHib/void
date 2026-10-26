package content.skill.slayer

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class BlackMask : Script {

    init {
        // Charges are invoked at random when starting combat with any opponent
        combatStart { target ->
            if (!equipped(EquipSlot.Hat).id.startsWith("black_mask_")) {
                return@combatStart
            }
            if (random.nextInt(Settings["slayer.blackMaskChargeChance", 50]) != 0) {
                return@combatStart
            }
            if (equipment.discharge(this, EquipSlot.Hat.index)) {
                target.levels.drain(Skill.Defence, random.nextInt(3, 11))
            }
        }

        itemOption("Remove-charge", "black_mask_*") { (item, slot) ->
            inventory.replace(slot, item.id, "black_mask")
        }
    }
}
