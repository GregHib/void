import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.awaitInterface
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.container
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.sendScript
import world.gregs.voidps.network.encode.sendVarc
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.inject

IntVariable(118, Variable.Type.VARP, defaultValue = -1).register("main_container")
IntVariable(1496, Variable.Type.VARP, defaultValue = -1).register("free_container")
IntVariable(532, Variable.Type.VARP).register("currency")// also 743?

val definitions: ItemDefinitions by inject()

on<Command>({ prefix == "shop" }) { player: Player ->
    player.action(ActionType.Shopping) {
        try {
            player.interfaces.apply {
                open("shop")
                close("inventory")
                open("shop_side")
                sendText("shop", "title", "Bob's Brilliant Axes")
            }
//            player.open("item_info")
            player.container("shop_side")
            player.setVar("free_container", -1)//554)
            player.setVar("main_container", 1)
            /*
                TODO shops

                bobs_brilliant_axes:
                    title: "Bob's Brilliant Axes"
                    sample:
                      container: bobs_brilliant_axes_sample
                      items:
                        - id: bronze_pickaxe
                          amount: 1
                          price: 0
                          restock: -1
                        - id: bronze_hatchet
                          amount: 1
                          price: 0
                          restock: -1
                    stock:
                      container: bobs_brilliant_axes
                      items:
                        - id: bronze_pickaxe
                          amount: 10
                          price: 1
                          restock: 60
                        - id: bronze_hatchet
                          amount: 10
                          price: 10
                          restock: 60
             */
            player.setVar("currency", 995)
//            val sample = player.container("bobs_brilliant_axes_sample")
//            sample.add("bronze_pickaxe")
//            sample.add("bronze_hatchet")
//            player.sendContainer("bobs_brilliant_axes_sample")
            val main = player.container("bobs_brilliant_axes")
            main.add("bronze_pickaxe")
            main.add("bronze_hatchet")
            main.add("iron_hatchet")
            main.add("steel_hatchet")
            main.add("iron_battleaxe")
            main.add("steel_battleaxe")
            main.add("mithril_battleaxe")
            player.interfaceOptions.apply {
                unlockAll("shop", "stock_options", 0 until 28)
                unlockAll("shop", "free_options", 0 until 28)
            }
            player.sendContainer("bobs_brilliant_axes")
            awaitInterface("shop")
        } finally {
            player.close("shop")
            player.open("inventory")
            player.close("shop_side")
        }
    }
}

fun openShop(currency: String = "coins") {

}


on<Command>({ prefix == "921" }) { player: Player ->
    line(player, "This is just a line.", 0, 0)
}

/**
 * @param alignment 0 = left, 1 = center, 2 = right
 */
fun line(player: Player, text: String, alignment: Int, fontSize: Int) {
    player.sendScript(921, listOf(text, alignment, fontSize))
}

on<Command>({ prefix == "922" }) { player: Player ->
    warning(player, "This is a warning<br>with multiple lines.", 1, 0)
}

/**
 * @param alignment 0 = left, 1 = center, 2 = right
 */
fun warning(player: Player, text: String, alignment: Int, width: Int) {
    player.sendScript(922, listOf(text, alignment, width))
}

on<Command>({ prefix == "923" }) { player: Player ->
    row(player, 0, "One", "Two", "Three")
}

/**
 * @param alignment 0 = left, 1 = center, 2 = right
 */
fun row(player: Player, alignment: Int, left: String, center: String, right: String) {
    player.sendScript(923, listOf(alignment, left, center, right))
}

on<Command>({ prefix == "924" }) { player: Player ->
    script924(player, "This is just a test", 1, 1, 0, 0)
}

/**
 * @param alignment 0 = left, 1 = center, 2 = right
 */
fun script924(player: Player, text: String, height: Int, alignment: Int, incrementVarc742: Int, font: Int) {
    player.sendVarc(742, 0)
    player.sendScript(924, listOf(text, height, alignment, incrementVarc742, font))
}