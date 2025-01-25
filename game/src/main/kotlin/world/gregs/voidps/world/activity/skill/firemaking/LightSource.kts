package world.gregs.voidps.world.activity.skill.firemaking

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItems
import world.gregs.voidps.engine.data.definition.data.LightSources
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import content.entity.player.inv.inventoryItem

val acceptedUnlitSource = arrayOf(
    "oil_lamp_oil",
    "candle_lantern_white",
    "candle_lantern_black",
    "oil_lantern_oil",
    "bullseye_lantern_oil",
    "sapphire_lantern_oil",
    "mining_helmet",
    "emerald_lantern",
    "white_candle",
    "black_candle",
    "unlit_torch"
)

itemOnItems(arrayOf("tinderbox*"), acceptedUnlitSource) {
    val needsFlame: LightSources = toItem.def.getOrNull("light_source") ?: return@itemOnItems

    if (!it.has(Skill.Firemaking, needsFlame.level, true)) {
        return@itemOnItems
    }

    it.inventory.transaction {
        replace(toItem.id, needsFlame.onceLit)
    }

    val litItem = determineLightSource(needsFlame.onceLit)
    it.message("You light the $litItem", ChatType.Game)

}

inventoryItem("Extinguish") {
    val toExtinguish: LightSources = item.def.getOrNull("light_source") ?: return@inventoryItem

    player.inventory.transaction {
        replace(item.id, toExtinguish.onceExtinguish)
    }

    player.message("You extinguish the flame.", ChatType.Game)
}

fun determineLightSource(itemName: String): String {
    return when {
        itemName.contains("lantern", ignoreCase = true) -> "lantern."
        itemName.contains("candle", ignoreCase = true) -> "candle."
        else -> "null"
    }
}