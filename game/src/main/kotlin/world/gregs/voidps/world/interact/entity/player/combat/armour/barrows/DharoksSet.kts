package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["dharoks_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("dharoks_set_effect") && !isDharoks(item) }) { player: Player ->
    player.clear("dharoks_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("dharoks_set_effect") && isDharoks(item) && it.hasFullSet() }) { player: Player ->
    player["dharoks_set_effect"] = true
}

fun isDharoks(item: Item) = item.id.startsWith("dharoks_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "dharoks_greataxe",
    "dharoks_helm",
    "dharoks_platebody",
    "dharoks_platelegs")