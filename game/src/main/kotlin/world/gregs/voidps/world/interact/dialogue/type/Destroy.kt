package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendItem
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.engine.utility.get

private const val DESTROY_INTERFACE_ID = "dialogue_confirm_destroy"

suspend fun PlayerContext.destroy(text: String, item: String): Boolean {
    val itemDecoder: ItemDefinitions = get()
    check(player.open(DESTROY_INTERFACE_ID)) { "Unable to open destroy dialogue for $item $player" }
    player.interfaces.sendText(DESTROY_INTERFACE_ID, "line1", text.trimIndent().replace("\n", "<br>"))
    val def = itemDecoder.get(item)
    player.interfaces.sendText(DESTROY_INTERFACE_ID, "item_name", def.name)
    player.interfaces.sendItem(DESTROY_INTERFACE_ID, "item_slot", def.id, 1)
    val result = StringSuspension(player) == "confirm"
    player.close(DESTROY_INTERFACE_ID)
    return result
}