package world.gregs.voidps.world.activity.skill.runecrafting

import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.network.visual.update.player.EquipSlot

val omni = listOf(
    "air",
    "mind",
    "water",
    "earth",
    "fire",
    "body",
    "cosmic",
    "law",
    "nature",
    "chaos",
    "death",
    "blood"
)

on<Registered>({ it.equipped(EquipSlot.Hat).id.endsWith("_tiara") }) { player: Player ->
    toggleAltar(player, player.equipped(EquipSlot.Hat), true)
}

on<ItemChanged>({ inventory == "worn_equipment" && index == EquipSlot.Hat.index && item.id.endsWith("_tiara") }) { player: Player ->
    toggleAltar(player, item, true)
}

on<ItemChanged>({ inventory == "worn_equipment" && index == EquipSlot.Hat.index && oldItem.id.endsWith("_tiara") }) { player: Player ->
    toggleAltar(player, oldItem, false)
}

fun toggleAltar(player: Player, item: Item, unlocked: Boolean) {
    val type = item.id.removeSuffix("_tiara")
    if (type == "omni") {
        for (t in omni) {
            player["${t}_altar_ruins"] = unlocked
        }
    } else {
        player["${type}_altar_ruins"] = unlocked
    }
}

val objectDefinitions: ObjectDefinitions by inject()

on<ItemOnObject>({ item.id.endsWith("_talisman") && target.id == "${item.id.removeSuffix("_talisman")}_altar_ruins" }) { player: Player ->
    val id = target.def.transforms?.getOrNull(1) ?: return@on
    val definition = objectDefinitions.get(id)
    player.mode = Interact(player, target, ObjectOption(player, target, definition, "Enter"), approachRange = -1)
}