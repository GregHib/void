package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotObjectExists
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.type.Tile
import kotlin.collections.indexOf
import kotlin.collections.iterator

data class BotInteractObject(
    val option: String,
    val id: String,
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
    val x: Int? = null,
    val y: Int? = null,
    val condition: Condition? = null,
) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (condition != null && !condition.check(bot.player)) {
            return BehaviourState.Success
        }
        if (success != null && success.check(bot.player)) {
            return BehaviourState.Success
        }
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        condition != null && !condition.check(bot.player) -> BehaviourState.Success
        success?.check(bot.player) == true -> BehaviourState.Success
        bot.mode is PlayerOnObjectInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun search(bot: Bot, world: BotWorld): BehaviourState {
        val player = bot.player
        val start = if (x != null && y != null) player.tile.copy(x = x, y = y) else player.tile
        for (tile in Spiral.spiral(start, radius)) {
            for (obj in GameObjects.at(tile)) {
                return interact(player, obj, world) ?: continue
            }
        }
        return handleNoTarget()
    }

    /** Whether [success]'s object stands within [OPENED_SLACK] tiles of where it is looked for. */
    private fun opened(success: BotObjectExists): Boolean {
        val expected = Tile(success.x, success.y, 0)
        for (tile in Spiral.spiral(expected, OPENED_SLACK)) {
            for (level in 0..3) {
                if (GameObjects.at(tile.copy(level = level)).any { wildcardEquals(success.id, it.id) }) {
                    return true
                }
            }
        }
        return false
    }

    private fun interact(player: Player, obj: GameObject, world: BotWorld): BehaviourState? {
        if (!wildcardEquals(id, obj.id)) {
            return null
        }
        val index = obj.def(player).options?.indexOf(option)
        if (index == null || index == -1) {
            return null
        }
        val valid = world.execute(player, InteractObject(obj.intId, obj.x, obj.y, index + 1))
        if (!valid) {
            return BehaviourState.Failed(Reason.Invalid("Invalid object interaction: $obj ${index + 1}"))
        }
        return BehaviourState.Running
    }

    private fun handleNoTarget(): BehaviourState {
        if (success == null) {
            return BehaviourState.Failed(Reason.NoTarget)
        }
        if (success is BotObjectExists && opened(success)) {
            // nothing to open and its opened form stands by the tile given: a gate someone left open, or one
            // whose opened half lands a tile from where the edge says; either way the way is clear
            return BehaviourState.Success
        }
        if (delay > 0) {
            return BehaviourState.Wait(delay, BehaviourState.Running)
        }
        return BehaviourState.Running
    }

    companion object {
        /** Tiles either way an opened door or gate may stand from where its edge expects it. */
        const val OPENED_SLACK = 2
    }
}
