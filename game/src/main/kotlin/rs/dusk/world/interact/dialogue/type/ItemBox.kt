package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.open
import rs.dusk.network.codec.game.encode.sendScript

private const val ITEM_INTERFACE_NAME = "obj_box"
private const val ITEM_SCRIPT_ID = 3449

suspend fun DialogueContext.item(text: String, item: Int, zoom: Int, sprite: Int? = null) {
    if (player.open(ITEM_INTERFACE_NAME)) {
        player.sendScript(ITEM_SCRIPT_ID, item, zoom)
        if (sprite != null) {
            player.interfaces.sendSprite(ITEM_INTERFACE_NAME, "sprite", sprite)
        }
        player.interfaces.sendText(ITEM_INTERFACE_NAME, "line1", text.trimIndent().replace("\n", "<br>"))
        return await("item")
    }
}