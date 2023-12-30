package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.dialogue.ContinueSuspension

private const val ITEM_INTERFACE_ID = "dialogue_obj_box"
private const val ITEM_SCRIPT_ID = 3449

suspend fun CharacterContext.item(text: String, item: String, zoom: Int, sprite: Int? = null) {
    check(player.open(ITEM_INTERFACE_ID)) { "Unable to open item dialogue for $player" }
    player.sendScript(ITEM_SCRIPT_ID, get<ItemDefinitions>().get(item).id, zoom)
    if (sprite != null) {
        player.interfaces.sendSprite(ITEM_INTERFACE_ID, "sprite", sprite)
    }
    val lines = if (text.contains("\n")) text.trimIndent().replace("\n", "<br>") else get<FontDefinitions>().get("497").splitLines(text, 380).joinToString("<br>")
    player.interfaces.sendText(ITEM_INTERFACE_ID, "line1", lines)
    ContinueSuspension()
    player.close(ITEM_INTERFACE_ID)
}