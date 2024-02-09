package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.visual.update.player.EquipSlot

val areas: AreaDefinitions by inject()
val area = areas["castle_wars"]

// TODO should be activated on game start not equip.

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

itemAdded("castle_wars_brace*", EquipSlot.Hands, "worn_equipment") { player: Player ->
    if (player.tile in area) {
        player["castle_wars_brace"] = true
    }
}

itemRemoved("castle_wars_brace*", EquipSlot.Hands, "worn_equipment") { player: Player ->
    if (player.tile in area) {
        player.clear("castle_wars_brace")
    }
}