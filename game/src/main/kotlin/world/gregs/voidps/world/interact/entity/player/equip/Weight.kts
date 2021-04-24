package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.weight

on<Registered> { player: Player ->
    updateWeight(player)
    player.events.on<Player, ItemChanged>({ container == "worn_equipment" || container == "inventory" }) {
        updateWeight(player)
    }
}

fun Container.weight(): Double {
    var weight = 0.0
    getItems().forEachIndexed { index, id ->
        val amount = getAmount(index)
        if (id.isNotBlank() && amount > 0) {
            weight += definitions.get(id)["weight", 0.0] * amount
        }
    }
    return weight
}

fun updateWeight(player: Player) {
    var weight = 0.0
    weight += player.equipment.weight()
    weight += player.inventory.weight()

    player["weight", true] = weight
    player.client?.weight(weight.toInt())
}