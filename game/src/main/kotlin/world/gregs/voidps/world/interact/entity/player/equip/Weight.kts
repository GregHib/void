package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.weight

on<ItemChanged>({ container == "worn_equipment" || container == "inventory" }) { player: Player ->
    updateWeight(player)
}

on<Registered> { player: Player ->
    updateWeight(player)
}

fun Container.weight(): Double = items.sumOf { it.def["weight", 0.0] * it.amount }

fun updateWeight(player: Player) {
    var weight = 0.0
    weight += player.equipment.weight()
    weight += player.inventory.weight()

    player["weight"] = weight
    player.client?.weight(weight.toInt())
}