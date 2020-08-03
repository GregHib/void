package rs.dusk.world.interact.dialogue

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.client.ui.open
import rs.dusk.utility.get

private const val DESTROY_INTERFACE_NAME = "confirm_destroy"

suspend fun Dialogues.destroy(text: String, item: Int): String {
    val itemDecoder: ItemDecoder = get()
    if (player.open(DESTROY_INTERFACE_NAME)) {
        player.interfaces.sendText(DESTROY_INTERFACE_NAME, "line1", text)
        player.interfaces.sendText(DESTROY_INTERFACE_NAME, "item_name", itemDecoder.getSafe(item).name)
        player.interfaces.sendItem(DESTROY_INTERFACE_NAME, "item_slot", item, 1)
    }
    return await("destroy")
}