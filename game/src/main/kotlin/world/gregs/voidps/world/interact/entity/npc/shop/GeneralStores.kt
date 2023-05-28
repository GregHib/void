package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.contain.ContainerData
import world.gregs.voidps.engine.contain.remove.ItemIndexRemovalChecker
import world.gregs.voidps.engine.contain.sendContainer
import world.gregs.voidps.engine.contain.stack.AlwaysStack
import world.gregs.voidps.engine.data.definition.extra.ContainerDefinitions
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject

object GeneralStores {

    private val containerDefs: ContainerDefinitions by inject()
    private val itemDefs: ItemDefinitions by inject()

    val stores: MutableMap<String, Container> = mutableMapOf()

    fun get(key: String) = stores.getOrPut(key) {
        val def = containerDefs.get(key)
        val minimumQuantities = IntArray(def.length) { if (def.getOrNull<List<Map<String, Int>>>("defaults")?.getOrNull(it) != null) -1 else 0 }
        val checker = ItemIndexRemovalChecker(minimumQuantities)
        Container(
            data = ContainerData(Array(def.length) {
                val map = def.getOrNull<List<Map<String, Int>>>("defaults")?.getOrNull(it)
                Item(
                    id = map?.keys?.firstOrNull() ?: "",
                    amount = map?.values?.firstOrNull() ?: 0
                )
            }),
            id = key,
            itemRule = GeneralStoreRestrictions(itemDefs),
            stackRule = AlwaysStack,
            removalCheck = checker
        )
    }

    fun bind(player: Player, key: String): Container = get(key).apply {
        this.transaction.changes.bind(player.events)
        player.sendContainer(this, false)
    }

    fun unbind(player: Player, key: String): Container = get(key).apply {
        this.transaction.changes.unbind(player.events)
    }

}