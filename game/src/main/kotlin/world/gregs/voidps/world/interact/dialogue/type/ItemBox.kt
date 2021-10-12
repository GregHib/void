package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendSprite
import world.gregs.voidps.engine.client.ui.sendText

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