package rs.dusk.world.interact.dialogue

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage

private const val ITEM_INTERFACE_NAME = "obj_box"
private const val ITEM_SCRIPT_ID = 3449

suspend fun Dialogues.itemBox(text: String, model: Int, zoom: Int, sprite: Int? = null) {
    if (player.open(ITEM_INTERFACE_NAME)) {
        player.send(ScriptMessage(ITEM_SCRIPT_ID, model, zoom))
        if (sprite != null) {
            player.interfaces.sendSprite(ITEM_INTERFACE_NAME, "sprite", sprite)
        }
        player.interfaces.sendText(ITEM_INTERFACE_NAME, "line1", text)
        return await("item")
    }
}