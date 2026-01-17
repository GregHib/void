package content.skill.melee.armour

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CastleWarsBrace : Script {

    init {
        entered("castle_wars") {
            if (equipped(EquipSlot.Hands).id.startsWith("castle_wars_brace")) {
                set("castle_wars_brace", true)
            }
        }

        exited("castle_wars") {
            if (equipped(EquipSlot.Hands).id.startsWith("castle_wars_brace")) {
                clear("castle_wars_brace")
            }
        }

        // TODO should be activated on game start not equip.
        itemAdded("castle_wars_brace*", "worn_equipment", EquipSlot.Hands) {
            if (tile in AreaTypes["castle_wars"]) {
                set("castle_wars_brace", true)
            }
        }

        itemRemoved("castle_wars_brace*", "worn_equipment", EquipSlot.Hands) {
            if (tile in AreaTypes["castle_wars"]) {
                clear("castle_wars_brace")
            }
        }
    }
}
