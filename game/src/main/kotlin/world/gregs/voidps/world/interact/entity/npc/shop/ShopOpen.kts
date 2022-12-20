import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.turn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.GeneralStores
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

val itemDefs: ItemDefinitions by inject()
val containerDefs: ContainerDefinitions by inject()
val logger = InlineLogger()

on<NPCOption>({ def.has("shop") && option == "Trade" }) { player: Player ->
    npc.turn(player)
    player.events.emit(OpenShop(def["shop"]))
}

on<OpenShop> { player: Player ->
    player.action(ActionType.Shopping) {
        try {
            val definition = containerDefs.getOrNull(id) ?: return@action
            val currency: String = definition["currency", "coins"]
            player.setVar("shop_currency", currency)
            player.setVar("item_info_currency", currency)
            player["shop"] = id
            player.interfaces.open("shop")
            player.open("shop_side")
            val containerSample = "${id}_sample"

            player.setVar("free_container", containerDefs.get(containerSample).id)
            val sample = openShopContainer(player, containerSample)
            player.interfaceOptions.unlockAll("shop", "sample", 0 until sample.capacity * 5)

            player.setVar("main_container", definition.id)
            val main = openShopContainer(player, id)
            sendAmounts(player, main)
            player.interfaceOptions.unlockAll("shop", "stock", 0 until main.capacity * 6)

            player.interfaces.sendVisibility("shop", "store", id.endsWith("general_store"))
            player.interfaces.sendText("shop", "title", definition["title", "Shop"])

            awaitInterface("shop")
        } finally {
            if (id.endsWith("general_store")) {
                GeneralStores.unbind(player, id)
            }
            player.close("shop")
            player.close("item_info")
            player.close("shop_side")
        }
    }
}

on<InterfaceRefreshed>({ id == "shop_side" }) { player: Player ->
    player.interfaceOptions.send("shop_side", "container")
    player.interfaceOptions.unlockAll("shop_side", "container", 0 until 28)
}

fun openShopContainer(player: Player, id: String): Container {
    return if (id.endsWith("general_store")) {
        GeneralStores.bind(player, id)
    } else {
        val new = !player.containers.containsKey(id)
        val container = player.containers.container(id)
        if (new) {
            fillShop(container, id)
        }
        player.sendContainer(id)
        container
    }
}

fun fillShop(container: Container, shopId: String) {
    val def = containerDefs.get(shopId)
    if (!def.has("shop")) {
        logger.warn { "Invalid shop definition $shopId" }
    }
    val ids = def.ids ?: return
    val amounts = def.amounts ?: return
    for (index in 0 until def.length) {
        val intId = ids.getOrNull(index) ?: continue
        val id = itemDefs.getOrNull(intId)?.stringId ?: continue
        val amount = amounts.getOrNull(index) ?: 0
        container.transaction { set(index, Item(id, amount)) }
    }
}

on<ItemChanged>({ it.contains("shop") && container == it["shop"] }) { player: Player ->
    player.setVar("amount_${index}", item.amount)
}

fun sendAmounts(player: Player, container: Container) {
    for ((index, item) in container.getItems().withIndex()) {
        player.setVar("amount_$index", item.amount)
    }
}
