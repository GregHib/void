package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.BotInterfaceOpen
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.Spiral

/** Open a nearby supported booth or chest through its actual game option. */
class BotOpenBank : BotAction {

    private val bankOpen = BotInterfaceOpen("bank")
    private var nearest: BotInteractObject? = null

    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState = if (bankOpen.check(bot.player)) BehaviourState.Success else BehaviourState.Running

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        if (bankOpen.check(bot.player)) {
            return BehaviourState.Success
        }
        if (bot.mode != EmptyMode) {
            return BehaviourState.Running
        }
        if (nearest == null) {
            val (obj, option) = nearby(bot.player) ?: return BehaviourState.Running
            nearest = BotInteractObject(option, obj.id, success = bankOpen, radius = 0, x = obj.x, y = obj.y)
        }
        return nearest!!.update(bot, world, frame)
    }

    fun option(player: Player, obj: GameObject): String? = when {
        obj.id.startsWith("bank_booth") -> "Use-quickly"
        obj.id.startsWith("bank_chest") -> when {
            obj.def(player).containsOption("Bank") -> "Bank"
            obj.def(player).containsOption("Use") -> "Use"
            else -> null
        }
        obj.id == "shantay_chest" -> "Open"
        else -> null
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
