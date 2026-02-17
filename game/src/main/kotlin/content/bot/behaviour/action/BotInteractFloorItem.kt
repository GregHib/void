package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Condition
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import kotlin.collections.indexOf
import kotlin.collections.iterator

data class BotInteractFloorItem(
    val option: String,
    val id: String,
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
    val x: Int? = null,
    val y: Int? = null,
) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame) = BehaviourState.Running

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        success?.check(bot.player) == true -> BehaviourState.Success
        bot.mode is PlayerOnFloorItemInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun search(bot: Bot, world: BotWorld): BehaviourState {
        val player = bot.player
        val start = if (x != null && y != null) player.tile.copy(x = x, y = y) else player.tile
        for (tile in Spiral.spiral(start, radius)) {
            for (obj in FloorItems.at(tile)) {
                return interact(player, obj, world) ?: continue
            }
        }
        return handleNoTarget()
    }

    private fun interact(player: Player, item: FloorItem, world: BotWorld): BehaviourState? {
        if (!wildcardEquals(id, item.id)) {
            return null
        }
        val index = item.def.floorOptions.indexOf(option)
        if (index == -1) {
            return null
        }
        val valid = world.execute(player, InteractFloorItem(item.def.id, item.tile.x, item.tile.y, index + 1))
        if (!valid) {
            return BehaviourState.Failed(Reason.Invalid("Invalid floor item interaction: $item ${index + 1}"))
        }
        return BehaviourState.Running
    }

    private fun handleNoTarget(): BehaviourState {
        if (success == null) {
            return BehaviourState.Failed(Reason.NoTarget)
        }
        if (delay > 0) {
            return BehaviourState.Wait(delay, BehaviourState.Running)
        }
        return BehaviourState.Running
    }
}
