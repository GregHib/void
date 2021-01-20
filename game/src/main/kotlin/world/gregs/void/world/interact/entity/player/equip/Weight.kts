package world.gregs.void.world.interact.entity.player.equip

import world.gregs.void.engine.entity.character.contain.Container
import world.gregs.void.engine.entity.character.contain.ContainerModification
import world.gregs.void.engine.entity.character.contain.equipment
import world.gregs.void.engine.entity.character.contain.inventory
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerRegistered
import world.gregs.void.engine.entity.character.set
import world.gregs.void.engine.entity.definition.ItemDefinitions
import world.gregs.void.engine.event.then
import world.gregs.void.network.codec.game.encode.WeightEncoder
import world.gregs.void.utility.inject

val definitions: ItemDefinitions by inject()
val weightEncoder: WeightEncoder by inject()

PlayerRegistered then {
    updateWeight(player)
    val listener: (List<ContainerModification>) -> Unit = {
        updateWeight(player)
    }
    player.equipment.listeners.add(listener)
    player.inventory.listeners.add(listener)
}

fun Container.weight(): Double {
    var weight = 0.0
    getItems().forEachIndexed { index, id ->
        val amount = getAmount(index)
        if(id != -1 && amount > 0) {
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
    weightEncoder.encode(player, weight.toInt())
}