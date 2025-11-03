package content.skill.firemaking

import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.LightSources
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class LightSource : Script {

    init {
        Wildcards.register(
            Wildcard.Item, "@unlit_sources",
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
            "unlit_torch",
        )
        itemOnItem("tinderbox*", "@unlit_sources") { _, toItem ->
            val needsFlame: LightSources = toItem.def.getOrNull("light_source") ?: return@itemOnItem

            if (!has(Skill.Firemaking, needsFlame.level, true)) {
                return@itemOnItem
            }

            inventory.transaction {
                replace(toItem.id, needsFlame.onceLit)
            }

            val litItem = determineLightSource(needsFlame.onceLit)
            message("You light the $litItem", ChatType.Game)
        }

        inventoryItem("Extinguish") {
            val source: LightSources = item.def.getOrNull("light_source") ?: return@inventoryItem

            player.inventory.transaction {
                replace(item.id, source.onceExtinguish)
            }

            player.message("You extinguish the flame.", ChatType.Game)
        }
    }

    fun determineLightSource(itemName: String): String = when {
        itemName.contains("lantern", ignoreCase = true) -> "lantern."
        itemName.contains("candle", ignoreCase = true) -> "candle."
        else -> "null"
    }
}
