package content.skill.melee.armour

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class CastleWarsBrace {

    val areas: AreaDefinitions by inject()
    val area = areas["castle_wars"]

    init {
        enterArea("castle_wars") {
            if (player.equipped(EquipSlot.Hands).id.startsWith("castle_wars_brace")) {
                player["castle_wars_brace"] = true
            }
        }

        exitArea("castle_wars") {
            if (player.equipped(EquipSlot.Hands).id.startsWith("castle_wars_brace")) {
                player.clear("castle_wars_brace")
            }
        }

        itemAdded("castle_wars_brace*", EquipSlot.Hands, "worn_equipment") { player ->
            if (player.tile in area) {
                player["castle_wars_brace"] = true
            }
        }

        itemRemoved("castle_wars_brace*", EquipSlot.Hands, "worn_equipment") { player ->
            if (player.tile in area) {
                player.clear("castle_wars_brace")
            }
        }
    }

    // TODO should be activated on game start not equip.
}
