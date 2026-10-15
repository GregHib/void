package content.bot.behaviour.setup

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.map.Spiral

/** Supported objects and options for opening a nearby bank. */
object BankAccess {
    data class Rule(val id: String, val options: List<String>)

    val bankAccess = listOf(
        Rule("bank_booth*", listOf("Use-quickly")),
        Rule("bank_chest*", listOf("Bank", "Use")),
        Rule("shantay_chest", listOf("Open")),
    )

    fun option(player: Player, obj: GameObject): String? {
        val rule = bankAccess.firstOrNull { wildcardEquals(it.id, obj.id) } ?: return null
        val options = obj.def(player).options ?: return null
        return rule.options.firstOrNull { it in options }
    }

    fun nearby(player: Player): Pair<GameObject, String>? {
        for (tile in Spiral.spiral(player.tile, 10)) {
            for (obj in GameObjects.at(tile)) {
                val option = option(player, obj) ?: continue
                return obj to option
            }
        }
        return null
    }

}
