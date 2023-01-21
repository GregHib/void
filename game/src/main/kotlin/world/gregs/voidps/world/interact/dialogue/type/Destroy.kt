package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendItem
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.suspend.BooleanSuspension
import world.gregs.voidps.engine.utility.get

private const val DESTROY_INTERFACE_ID = "dialogue_confirm_destroy"

suspend fun DialogueContext.destroy(text: String, item: String): Boolean {
    val itemDecoder: ItemDefinitions = get()
    if (player.open(DESTROY_INTERFACE_ID)) {
        player.interfaces.sendText(DESTROY_INTERFACE_ID, "line1", text.trimIndent().replace("\n", "<br>"))
        val def = itemDecoder.get(item)
        player.interfaces.sendText(DESTROY_INTERFACE_ID, "item_name", def.name)
        player.interfaces.sendItem(DESTROY_INTERFACE_ID, "item_slot", def.id, 1)
        return await("destroy")
    }
    return false
}

context(Interaction) suspend fun destroy(text: String, item: String): Boolean {
    val itemDecoder: ItemDefinitions = get()
    check(player.open(DESTROY_INTERFACE_ID)) { "Unable to open destroy dialogue for $item $player" }
    player.interfaces.sendText(DESTROY_INTERFACE_ID, "line1", text.trimIndent().replace("\n", "<br>"))
    val def = itemDecoder.get(item)
    player.interfaces.sendText(DESTROY_INTERFACE_ID, "item_name", def.name)
    player.interfaces.sendItem(DESTROY_INTERFACE_ID, "item_slot", def.id, 1)
    val result = BooleanSuspension()
    player.close(DESTROY_INTERFACE_ID)
    return result
}