package rs.dusk.engine.model.entity.index.contain

import rs.dusk.cache.config.decoder.ItemContainerDecoder
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.utility.get

sealed class Containers(val id: Int) {
    object PriceChecker : Containers(90)
    object Inventory : Containers(93)
    object Equipment : Containers(94)
    object Bank : Containers(95)
    object DuelStake : Containers(134)
    object BeastOfBurden : Containers(530)

    sealed class Shop(id: Int, val title: String = "") : Containers(id) {
        object LletyaArcheryShop : Shop(303, "Lletya Archery Shop")
    }
}

fun Player.container(container: Containers): Container {
    val id = container.id
    if (containers.containsKey(id)) {
        containers[id] = Container(
            decoder = get(),
            capacity = get<ItemContainerDecoder>().getSafe(id).length,
            stackMode = if(container == Containers.Bank) StackMode.Always else StackMode.Normal// TODO stack mode
        )
    }
    return containers[id]!!
}