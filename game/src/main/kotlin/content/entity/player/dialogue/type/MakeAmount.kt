package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.event.Script

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.IntSuspension

private const val INTERFACE_ID = "dialogue_skill_creation"
private const val INTERFACE_AMOUNT_ID = "skill_creation_amount"
private const val DEFAULT_TEXT = "Choose how many you wish to make, then<br>click on the chosen item to begin."

suspend fun Context<Player>.makeAmount(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true,
    names: List<String>? = null,
): Pair<String, Int> {
    val (index, amount) = makeAmountIndex(items, type, maximum, text, allowAll, names)
    val id = items.getOrNull(index) ?: ""
    return id to amount
}

suspend fun Context<Player>.makeAmountIndex(
    items: List<String>,
    type: String,
    maximum: Int,
    text: String = DEFAULT_TEXT,
    allowAll: Boolean = true,
    names: List<String>? = null,
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
    for (index in 0 until 10) {
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
@Script
class MakeAmount {

    init {
        interfaceOption("1", "create1", "skill_creation_amount") {
            player["skill_creation_amount", false] = 1
        }

        interfaceOption("5", "create5", "skill_creation_amount") {
            player["skill_creation_amount", false] = 5
        }

        interfaceOption("10", "create10", "skill_creation_amount") {
            player["skill_creation_amount", false] = 10
        }

        interfaceOption(component = "all", id = "skill_creation_amount") {
            val max: Int = player["skill_creation_maximum", 1]
            player["skill_creation_amount", false] = max
        }

        interfaceOption("+1", "increment", "skill_creation_amount") {
            var current: Int = player["skill_creation_amount", 1]
            val maximum: Int = player["skill_creation_maximum", 1]
            current++
            if (current > maximum) {
                current = maximum
            }
            player["skill_creation_amount"] = current
        }

        interfaceOption("-1", "decrement", "skill_creation_amount") {
            var current: Int = player["skill_creation_amount", 1]
            current--
            if (current < 0) {
                current = 0
            }
            player["skill_creation_amount"] = current
        }

    }

}
