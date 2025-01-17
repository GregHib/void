package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.ContinueSuspension

private const val ITEM_INTERFACE_ID = "dialogue_obj_box"
private const val DOUBLE_ITEM_INTERFACE_ID = "dialogue_double_obj_box"

suspend fun SuspendableContext<Player>.item(item: String, zoom: Int, text: String, sprite: Int? = null) {
    check(player.open(ITEM_INTERFACE_ID)) { "Unable to open item dialogue for $player" }
    player.sendScript("dialogue_item_zoom", get<ItemDefinitions>().get(item).id, zoom)
    if (sprite != null) {
        player.interfaces.sendSprite(ITEM_INTERFACE_ID, "sprite", sprite)
    }
    val lines = if (text.contains("\n")) text.trimIndent().replace("\n", "<br>") else get<FontDefinitions>().get("q8_full").splitLines(text, 380).joinToString("<br>")
    player.interfaces.sendText(ITEM_INTERFACE_ID, "line1", lines)
    ContinueSuspension.get(player)
    player.close(ITEM_INTERFACE_ID)
}

suspend fun SuspendableContext<Player>.items(item1: String, item2: String, text: String) {
    check(player.open(DOUBLE_ITEM_INTERFACE_ID)) { "Unable to open item dialogue for $player" }
    player.interfaces.sendItem(DOUBLE_ITEM_INTERFACE_ID, "model1", get<ItemDefinitions>().get(item1).id)
    player.interfaces.sendItem(DOUBLE_ITEM_INTERFACE_ID, "model2", get<ItemDefinitions>().get(item2).id)
    val lines = if (text.contains("\n")) text.trimIndent().replace("\n", "<br>") else get<FontDefinitions>().get("q8_full").splitLines(text, 380).joinToString("<br>")
    player.interfaces.sendText(DOUBLE_ITEM_INTERFACE_ID, "line1", lines)
    ContinueSuspension.get(player)
    player.close(DOUBLE_ITEM_INTERFACE_ID)
}