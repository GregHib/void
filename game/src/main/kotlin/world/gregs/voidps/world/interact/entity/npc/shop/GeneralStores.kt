package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ContainerData
import world.gregs.voidps.engine.entity.character.contain.remove.ItemIndexRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.inject

object GeneralStores {

    private val containerDefs: ContainerDefinitions by inject()
    private val itemDefs: ItemDefinitions by inject()

    val stores: MutableMap<String, Container> = mutableMapOf()

    fun get(key: String) = stores.getOrPut(key) {
        val def = containerDefs.get(key)
        val minimumQuantities = IntArray(def.length) { if (def.ids?.getOrNull(it) != null) -1 else 0 }
        val checker = ItemIndexRemovalChecker(minimumQuantities)
        Container(
            data = ContainerData(Array(def.length) {
                Item(
                    id = itemDefs.get(def.ids?.getOrNull(it) ?: -1).stringId,
                    amount = def.amounts?.getOrNull(it) ?: 0
                )
            }),
            id = key,
            stackRule = AlwaysStack,
            removalCheck = checker
        ).apply {
            definitions = itemDefs
        }
    }

    fun bind(player: Player, key: String): Container = get(key).apply {
        this.events.add(player.events)
        player.sendContainer(this, false)
    }

    fun unbind(player: Player, key: String): Container = get(key).apply {
        this.events.remove(player.events)
    }

}