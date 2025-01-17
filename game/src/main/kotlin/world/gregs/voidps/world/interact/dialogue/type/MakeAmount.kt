package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.dialogue.IntSuspension

private const val INTERFACE_ID = "dialogue_skill_creation"
private const val INTERFACE_AMOUNT_ID = "skill_creation_amount"
private const val DEFAULT_TEXT = "Choose how many you wish to make, then<br>click on the chosen item to begin."

suspend fun Context<Player>.makeAmount(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true,
    names: List<String>? = null
): Pair<String, Int> {
    val result = makeAmountIndex(items, type, maximum, text, allowAll, names)
    val id = items.getOrNull(result.first) ?: ""
    return id to result.second
}

suspend fun Context<Player>.makeAmountIndex(
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
    player["skill_creation_type"] = type

    setItemOptions(player, items, names)
    setMax(player, maximum.coerceAtLeast(1))
    val choice: Int = IntSuspension.get(player)
    player.close(INTERFACE_ID)
    player.close(INTERFACE_AMOUNT_ID)
    val amount = player["skill_creation_amount", 1]
    return choice to amount
}

private fun setItemOptions(player: Player, items: List<String>, names: List<String>?) {
    val definitions: ItemDefinitions = get()
    repeat(10) { index ->
        val item = definitions.get(items.getOrNull(index) ?: "")
        player["skill_creation_item_$index"] = item.id
        if (names != null && names.indices.contains(index)) {
            player["skill_creation_name_$index"] = names[index]
        } else if (item.id != -1) {
            player["skill_creation_name_$index"] = item.name
        }
    }
}

private fun setMax(player: Player, maximum: Int) {
    player["skill_creation_maximum"] = maximum
    val amount = player["skill_creation_amount", maximum]
    if (amount > maximum) {
        player["skill_creation_amount"] = maximum
    } else {
        player.sendVariable("skill_creation_amount")
    }
}