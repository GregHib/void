package content.skill.firemaking

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.LightSources
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class LightSource : Script {

    init {
        val unlitSources = buildString {
            append("oil_lamp_oil,")
            append("candle_lantern_white,")
            append("candle_lantern_black,")
            append("oil_lantern_oil,")
            append("bullseye_lantern_oil,")
            append("sapphire_lantern_oil,")
            append("mining_helmet,")
            append("emerald_lantern,")
            append("white_candle,")
            append("black_candle,")
            append("unlit_torch")
        }
        itemOnItem("tinderbox*", unlitSources) { _, toItem ->
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

        itemOption("Extinguish") { (item) ->
            val source: LightSources = item.def.getOrNull("light_source") ?: return@itemOption

            inventory.transaction {
                replace(item.id, source.onceExtinguish)
            }

            message("You extinguish the flame.", ChatType.Game)
        }
    }

    fun determineLightSource(itemName: String): String = when {
        itemName.contains("lantern", ignoreCase = true) -> "lantern."
        itemName.contains("candle", ignoreCase = true) -> "candle."
        else -> "null"
    }
}
