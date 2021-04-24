import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitInterface
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.StringMapVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

val MAX_SHOP_SIZE = 40

IntVariable(118, Variable.Type.VARP, defaultValue = -1).register("main_container")
IntVariable(1496, Variable.Type.VARP, defaultValue = -1).register("free_container")
repeat(MAX_SHOP_SIZE) {
    IntVariable(946 + it, Variable.Type.VARC).register("amount_$it")
}
val currencies = mapOf(
    "coins" to 995,
    "tokkul" to 6529,
    "trading_sticks" to 6306,
    "archery_ticket" to 1464,
    "pieces_of_eight" to 8951,
    "agility_arena_ticket" to 2996,
    "castle_wars_ticket" to 4067,
    "runecrafting_guild_token" to 13650,
    "fist_of_guthix_token" to 12852
)
StringMapVariable(743, Variable.Type.VARC, values = currencies).register("item_info_currency")
StringMapVariable(532, Variable.Type.VARP, values = currencies).register("shop_currency")

val itemDefs: ItemDefinitions by inject()
val containerDefs: ContainerDefinitions by inject()

on<NPCOption>({ npc.def.has("shop") && option == "Trade" }) { player: Player ->
    npc.turn(player)
    player.events.emit(OpenShop(npc.def["shop"]))
}

on<OpenShop> { player: Player ->
    player.action(ActionType.Shopping) {
        var handler: EventHandler? = null
        try {
            val definition = containerDefs.getOrNull(name) ?: return@action
            val currency: String = definition["currency", "coins"]
            player.setVar("shop_currency", currency)
            player.setVar("item_info_currency", currency)
            player["shop"] = name
            player.interfaces.open("shop")
            player.open("shop_side")
            val containerSample = "${name}_sample"

            player.setVar("free_container", containerDefs.getId(containerSample))
            val sample = openShopContainer(player, containerSample)
            player.interfaceOptions.unlockAll("shop", "sample", 0 until sample.capacity * 5)

            player.setVar("main_container", definition.id)
            val main = openShopContainer(player, name)
            handler = sendAmounts(player, main, name)
            player.interfaceOptions.unlockAll("shop", "stock", 0 until main.capacity * 6)

            player.interfaces.sendText("shop", "title", definition.getOrNull("title") as? String ?: "Shop")

            awaitInterface("shop")
        } finally {
            player.events.remove(handler)
            player.close("shop")
            player.close("item_info")
            player.close("shop_side")
        }
    }
}

on<InterfaceOpened>({ name == "shop_side" }) { player: Player ->
    player.interfaceOptions.send("shop_side", "container")
    player.interfaceOptions.unlockAll("shop_side", "container", 0 until 28)
}

fun openShopContainer(player: Player, name: String): Container {
    val new = !player.containers.contains(name)
    val container = player.container(name)
    container.minimumStack = -1
    container.stackMode = StackMode.Always
    if (new) {
        fillShop(container, name)
    }
    player.sendContainer(name)
    return container
}

fun fillShop(container: Container, name: String) {
    val def = containerDefs.get(name)
    for (i in 0 until def.length) {
        val id = itemDefs.getName(def.ids?.getOrNull(i) ?: -1)
        if (id.isBlank()) {
            continue
        }
        val amount = def.amounts?.getOrNull(i) ?: 0
        container.add(id, amount)
    }
}

fun sendAmounts(player: Player, container: Container, name: String): EventHandler {
    for ((index, amount) in container.getAmounts().withIndex()) {
        player.setVar("amount_$index", amount)
    }
    return player.events.on<Player, ItemChanged>({ this.container == name }) {
        player.setVar("amount_${index}", amount)
    }
}
