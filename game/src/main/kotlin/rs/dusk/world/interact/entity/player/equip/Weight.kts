package rs.dusk.world.interact.entity.player.equip

import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.ContainerModification
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerSpawn
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.event.then
import rs.dusk.network.rs.codec.game.encode.message.WeightMessage
import rs.dusk.utility.inject

val definitions: ItemDefinitions by inject()

PlayerSpawn then {
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
    player.send(WeightMessage(weight.toInt()))
}