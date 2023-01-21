package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendSprite
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.suspend.ContinueSuspension
import world.gregs.voidps.engine.utility.get

private const val ITEM_INTERFACE_ID = "dialogue_obj_box"
private const val ITEM_SCRIPT_ID = 3449

suspend fun DialogueContext.item(text: String, item: String, zoom: Int, sprite: Int? = null) {
    if (!player.open(ITEM_INTERFACE_ID)) {
        return
    }
    player.sendScript(ITEM_SCRIPT_ID, get<ItemDefinitions>().get(item).id, zoom)
    if (sprite != null) {
        player.interfaces.sendSprite(ITEM_INTERFACE_ID, "sprite", sprite)
    }
    player.interfaces.sendText(ITEM_INTERFACE_ID, "line1", text.trimIndent().replace("\n", "<br>"))
    return await("item")
}

context(PlayerContext) suspend fun item(text: String, item: String, zoom: Int, sprite: Int? = null) {
    check(player.open(ITEM_INTERFACE_ID)) { "Unable to open item dialogue for $player" }
    player.sendScript(ITEM_SCRIPT_ID, get<ItemDefinitions>().get(item).id, zoom)
    if (sprite != null) {
        player.interfaces.sendSprite(ITEM_INTERFACE_ID, "sprite", sprite)
    }
    player.interfaces.sendText(ITEM_INTERFACE_ID, "line1", text.trimIndent().replace("\n", "<br>"))
    ContinueSuspension(player)
    player.close(ITEM_INTERFACE_ID)
}