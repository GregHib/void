package content.skill.summoning

import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player

npcOperate("Swap", "bogrog") {
    openTradeInInterface(player, true)
}

interfaceOption("Trade Scrolls", id = "summoning_trade_in") {
    openTradeInInterface(player, false)
}

interfaceOption("Trade Pouches", id = "summoning_trade_in") {
    openTradeInInterface(player, true)
}

/**
 * Opens the interface used for trading in summoning pouches and scrolls for shards.
 *
 * @param player: The [Player] to open the interface for
 * @param isPouchTradeIn: If the interface to be opened is for trading in pouches. Otherwise, the scroll
 * trade in interface is opened
 */
fun openTradeInInterface(player: Player, isPouchTradeIn: Boolean) {
    player.open("summoning_trade_in")

    // Pouch trade in set up
    player.interfaces.sendVisibility("summoning_trade_in", "pouch_trade_in_text", isPouchTradeIn)
    player.interfaces.sendVisibility("summoning_trade_in", "pouch_trade_in", isPouchTradeIn)
    player.interfaces.sendVisibility("summoning_trade_in", "pouch_tab_scroll_bar", isPouchTradeIn)

    // Scroll trade in set up
    player.interfaces.sendVisibility("summoning_trade_in", "scroll_trade_in_text", !isPouchTradeIn)
    player.interfaces.sendVisibility("summoning_trade_in", "scroll_trade_in", !isPouchTradeIn)
    player.interfaces.sendVisibility("summoning_trade_in", "scroll_tab_scroll_bar", !isPouchTradeIn)

    val scrollTabActiveSprite = 1190
    val pouchTabActiveSprite = 1191
    val scrollTabInactiveSprite = 1192
    val pouchTabInactiveSprite = 1193
    val interfaceId = 78

    val componentId: Int
    val componentString: String
    val script: String


    if (isPouchTradeIn) {
        script = "populate_summoning_pouch_trade_in"
        componentId = 15
        componentString = "pouch_trade_in"
        player.interfaces.sendSprite("summoning_trade_in", "pouch_tab_sprite", pouchTabActiveSprite)
        player.interfaces.sendSprite("summoning_trade_in", "scroll_tab_sprite", scrollTabInactiveSprite)
    }
    else {
        script = "populate_summoning_scroll_trade_in"
        componentId = 14
        componentString = "scroll_trade_in"
        player.interfaces.sendSprite("summoning_trade_in", "pouch_tab_sprite", pouchTabInactiveSprite)
        player.interfaces.sendSprite("summoning_trade_in", "scroll_tab_sprite", scrollTabActiveSprite)
    }

    val width = 8
    val height = 10
    val startingIndex = 1
    val endingIndex = 78

    player.sendScript(
        script,
        InterfaceDefinition.pack(interfaceId, componentId),
        width,
        height,
        startingIndex,
        endingIndex,
        "Value<col=FF9040>",
        "Trade<col=FF9040>",
        "Trade-5<col=FF9040>",
        "Trade-10<col=FF9040>",
        "Trade-X<col=FF9040>",
        "Trade-All<col=FF9040>",
    )

    player.interfaceOptions.unlockAll("summoning_trade_in", componentString, 0..400)
}