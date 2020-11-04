package rs.dusk.world.interact.dialogue.type

import rs.dusk.engine.client.ui.dialogue.DialogueContext
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.entity.item.detail.ItemDefinitions
import rs.dusk.utility.get

private const val DESTROY_INTERFACE_NAME = "confirm_destroy"

suspend fun DialogueContext.destroy(text: String, item: Int): Boolean {
    val itemDecoder: ItemDefinitions = get()
    if (player.open(DESTROY_INTERFACE_NAME)) {
        player.interfaces.sendText(DESTROY_INTERFACE_NAME, "line1", text.trimIndent().replace("\n", "<br>"))
        player.interfaces.sendText(DESTROY_INTERFACE_NAME, "item_name", itemDecoder.get(item).name)
        player.interfaces.sendItem(DESTROY_INTERFACE_NAME, "item_slot", item, 1)
        return await("destroy")
    }
    return false
}