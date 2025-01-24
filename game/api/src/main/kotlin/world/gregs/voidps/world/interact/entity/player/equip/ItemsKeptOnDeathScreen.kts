package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.world.interact.entity.player.effect.skulled

val enums: EnumDefinitions by inject()

timerStart("skull") { player ->
    if (player.interfaces.contains("items_kept_on_death")) {
        player.open("items_kept_on_death", close = true)
    }
}

timerStop("skull") { player ->
    if (player.interfaces.contains("items_kept_on_death")) {
        player.open("items_kept_on_death", close = true)
    }
}

interfaceRefresh("items_kept_on_death") { player ->
    val items = ItemsKeptOnDeath.getAllOrdered(player)
    val savedItems = ItemsKeptOnDeath.kept(player, items, enums)
    val carriedWealth = items.sumOf { it.def.cost * it.amount }
    val savedWealth = savedItems.sumOf { it.def.cost * it.amount }
    player.updateItemsOnDeath(
        savedItems,
        carriedWealth = carriedWealth,
        riskedWealth = carriedWealth - savedWealth,
        skull = player.skulled
    )
}

fun Player.updateItemsOnDeath(items: List<Item>, carriedWealth: Int, riskedWealth: Int, familiar: Boolean = false, gravestone: Boolean = false, skull: Boolean = false) {
    sendScript("items_kept_on_death",
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
        if (skull) "You're marked with a <red_orange>skull." else ""
    )
}