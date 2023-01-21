package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.awaitDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.suspend.IntSuspension
import world.gregs.voidps.engine.utility.get

private const val INTERFACE_ID = "dialogue_skill_creation"
private const val INTERFACE_AMOUNT_ID = "skill_creation_amount"
private const val DEFAULT_TEXT = "Choose how many you wish to make, then<br>click on the chosen item to begin."

suspend fun Player.makeAmount(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true
): Pair<String, Int> {
    awaitDialogue {
        val pair = makeAmount(items, type, maximum, text, allowAll)
        action.resume(pair)
    }
    return action.await(Suspension.External)
}

suspend fun DialogueContext.makeAmount(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true
): Pair<String, Int> {
    val result = makeAmountIndex(items, type, maximum, text, allowAll)
    if (result.first != -1) {
        val id = items.getOrNull(result.first) ?: ""
        return id to result.second
    }
    return "" to 0
}

suspend fun DialogueContext.makeAmountIndex(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true,
    names: List<String>? = null
): Pair<Int, Int> {
    return if (player.open(INTERFACE_ID) && player.open(INTERFACE_AMOUNT_ID)) {
        if (allowAll) {
            player.interfaceOptions.unlockAll(INTERFACE_AMOUNT_ID, "all")
        }
        player.interfaces.sendVisibility(INTERFACE_ID, "all", allowAll)
        player.interfaces.sendVisibility(INTERFACE_ID, "custom", false)
        player.interfaces.sendText(INTERFACE_AMOUNT_ID, "line1", text)
        player.setVar("skill_creation_type", type)

        setItemOptions(player, items, names)
        setMax(player, maximum)
        val choice: Int = await("make")
        val amount = player.getVar("skill_creation_amount", 0)
        choice to amount
    } else {
        -1 to 0
    }
}

context(PlayerContext) suspend fun makeAmount(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true
): Pair<String, Int> {
    val result = makeAmountIndex(items, type, maximum, text, allowAll)
    val id = items.getOrNull(result.first) ?: ""
    return id to result.second
}

context(PlayerContext) suspend fun makeAmountIndex(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true,
    names: List<String>? = null
): Pair<Int, Int> {
    check(player.open(INTERFACE_ID) && player.open(INTERFACE_AMOUNT_ID)) { "Unable to open make amount dialogue for $player" }
    if (allowAll) {
        player.interfaceOptions.unlockAll(INTERFACE_AMOUNT_ID, "all")
    }
    player.interfaces.sendVisibility(INTERFACE_ID, "all", allowAll)
    player.interfaces.sendVisibility(INTERFACE_ID, "custom", false)
    player.interfaces.sendText(INTERFACE_AMOUNT_ID, "line1", text)
    player.setVar("skill_creation_type", type)

    setItemOptions(player, items, names)
    setMax(player, maximum)
    val choice: Int = IntSuspension(player)
    player.close(INTERFACE_ID)
    player.close(INTERFACE_AMOUNT_ID)
    val amount = player.getVar("skill_creation_amount", 0)
    return choice to amount
}

private fun setItemOptions(player: Player, items: List<String>, names: List<String>?) {
    val definitions: ItemDefinitions = get()
    repeat(10) { index ->
        val item = definitions.get(items.getOrNull(index) ?: "")
        player.setVar("skill_creation_item_$index", item.id)
        if (names != null && names.indices.contains(index)) {
            player.setVar("skill_creation_name_$index", names[index])
        } else if (item.id != -1) {
            player.setVar("skill_creation_name_$index", item.name)
        }
    }
}

private fun setMax(player: Player, maximum: Int) {
    player.setVar("skill_creation_maximum", maximum)
    val amount = player.getVar("skill_creation_amount", maximum)
    if (amount > maximum) {
        player.setVar("skill_creation_amount", maximum)
    } else {
        player.sendVar("skill_creation_amount")
    }
}