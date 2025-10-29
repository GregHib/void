package content.entity.player.kept

import content.entity.player.effect.skulled
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.Timer

@Script
class ItemsKeptOnDeathScreen : Api {

    val enums: EnumDefinitions by inject()

    init {
        interfaceRefresh("items_kept_on_death") { player ->
            val items = ItemsKeptOnDeath.getAllOrdered(player)
            val savedItems = ItemsKeptOnDeath.kept(player, items, enums)
            val carriedWealth = items.sumOf { it.def.cost * it.amount }
            val savedWealth = savedItems.sumOf { it.def.cost * it.amount }
            player.updateItemsOnDeath(
                savedItems,
                carriedWealth = carriedWealth,
                riskedWealth = carriedWealth - savedWealth,
                skull = player.skulled,
            )
        }

        timerStart("skull") {
            if (interfaces.contains("items_kept_on_death")) {
                open("items_kept_on_death", close = true)
            }
            return@timerStart Timer.CONTINUE
        }

        timerStop("skull") {
            if (interfaces.contains("items_kept_on_death")) {
                open("items_kept_on_death", close = true)
            }
        }
    }

    fun Player.updateItemsOnDeath(items: List<Item>, carriedWealth: Int, riskedWealth: Int, familiar: Boolean = false, gravestone: Boolean = false, skull: Boolean = false) {
        sendScript(
            "items_kept_on_death",
            AreaType.Dangerous.ordinal,
            items.size.coerceAtMost(4),
            items.getOrNull(0)?.def?.id ?: 0,
            items.getOrNull(1)?.def?.id ?: 0,
            items.getOrNull(2)?.def?.id ?: 0,
            items.getOrNull(3)?.def?.id ?: 0,
            (skull).toInt(),
            familiar.toInt(),
            carriedWealth,
            riskedWealth,
            gravestone.toInt(),
            if (skull) "You're marked with a <red_orange>skull." else "",
        )
    }
}
