package content.quest.member.ghosts_ahoy

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Bedsheet : Script {
    init {
        itemAdded("bedsheet", "worn_equipment", EquipSlot.Hat) {
            transform("ahoy_ghost_disguise")
        }

        itemAdded("bedsheet_ectoplasm", "worn_equipment", EquipSlot.Hat) {
            transform("ahoy_ghost_disguise_green")
        }

        itemRemoved("bedsheet", "worn_equipment", EquipSlot.Hat) {
            if (this.transform == "ahoy_ghost_disguise" || this.transform == "ahoy_ghost_disguise_green") {
                clearTransform()
            }
        }

        itemRemoved("bedsheet_ectoplasm", "worn_equipment", EquipSlot.Hat) {
            if (this.transform == "ahoy_ghost_disguise" || this.transform == "ahoy_ghost_disguise_green") {
                clearTransform()
            }
        }

        exited("port_phasmatys") {
            val id = equipped(EquipSlot.Hat).id
            if (id.startsWith("bedsheet")) {
                if (equipment.remove(EquipSlot.Hat.index, id)) {
                    addOrDrop(id)
                }
            }
        }
    }
}