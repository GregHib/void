package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.dialogue.StringSuspension

private const val DESTROY_INTERFACE_ID = "dialogue_confirm_destroy"

suspend fun CharacterContext<Player>.destroy(item: String, text: String): Boolean {
    val itemDecoder: ItemDefinitions = get()
    check(player.open(DESTROY_INTERFACE_ID)) { "Unable to open destroy dialogue for $item $player" }
    player.interfaces.sendText(DESTROY_INTERFACE_ID, "line1", text.trimIndent().replace("\n", "<br>"))
    val def = itemDecoder.get(item)
    player.interfaces.sendText(DESTROY_INTERFACE_ID, "item_name", def.name)
    player.interfaces.sendItem(DESTROY_INTERFACE_ID, "item_slot", def.id, 1)
    val result = StringSuspension() == "confirm"
    player.close(DESTROY_INTERFACE_ID)
    return result
}