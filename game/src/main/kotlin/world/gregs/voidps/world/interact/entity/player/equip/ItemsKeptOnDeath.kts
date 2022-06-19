import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.Colour
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.ui.isOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toInt
import world.gregs.voidps.world.interact.entity.player.equip.AreaType

on<EffectStart>({ effect == "skull" && it.isOpen("items_kept_on_death") }) { player: Player ->
    player.open("items_kept_on_death", close = false)
}

on<EffectStop>({ effect == "skull" && it.isOpen("items_kept_on_death") }) { player: Player ->
    player.open("items_kept_on_death", close = false)
}

on<EffectStart>({ effect == "prayer_protect_item" && it.isOpen("items_kept_on_death") }) { player: Player ->
    player.open("items_kept_on_death", close = false)
}

on<EffectStop>({ effect == "prayer_protect_item" && it.isOpen("items_kept_on_death") }) { player: Player ->
    player.open("items_kept_on_death", close = false)
}

on<InterfaceRefreshed>({ id == "items_kept_on_death" }) { player: Player ->
    val skull = player.hasEffect("skull")
    var saved = if (skull) 0 else 3
    if (player.hasEffect("prayer_protect_item")) {
        saved++
    }
    val items = player.inventory.getItems()
        .union(player.equipment.getItems().toList())
        .filter { it.isNotEmpty() }
        .sortedByDescending { it.def.cost }
    val savedItems = items.take(saved)
    val carriedWealth = items.sumOf { it.def.cost }
    val savedWealth = savedItems.sumOf { it.def.cost }
    player.updateItemsOnDeath(
        savedItems,
        carriedWealth = carriedWealth,
        riskedWealth = carriedWealth - savedWealth,
        skull = skull
    )
}

fun Player.updateItemsOnDeath(items: List<Item>, carriedWealth: Int, riskedWealth: Int, familiar: Boolean = false, gravestone: Boolean = false, skull: Boolean = false) {
    sendScript(118,
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
        if (skull) "You're marked with a ${Colour.AltRed("skull")}." else ""
    )
}