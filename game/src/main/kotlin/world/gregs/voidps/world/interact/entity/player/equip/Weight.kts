package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.weight

on<Registered> { player: Player ->
    updateWeight(player)
    player.events.on<Player, ItemChanged>({ container == "worn_equipment" || container == "inventory" }) {
        updateWeight(player)
    }
}

fun Container.weight(): Double = getItems().sumOf { it.def["weight", 0.0] * it.amount }

fun updateWeight(player: Player) {
    var weight = 0.0
    weight += player.equipment.weight()
    weight += player.inventory.weight()

    player["weight"] = weight
    player.client?.weight(weight.toInt())
}