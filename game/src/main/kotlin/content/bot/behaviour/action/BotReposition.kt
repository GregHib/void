package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.entity.combat.dead
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

data class BotReposition(
    val radius: Int = 1,
    val condition: Condition? = null,
) : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (condition != null && !condition.check(bot.player)) {
            return BehaviourState.Success
        }
        val target = pickTile(bot) ?: return BehaviourState.Success
        world.execute(bot.player, Walk(target.x, target.y))
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (bot.player.dead) {
            return BehaviourState.Failed(Reason.Cancelled)
        }
        if (allyCount(bot, bot.tile) == 0) {
            return BehaviourState.Success
        }
        if (bot.mode is EmptyMode && GameLoop.tick - bot.steps.last > 3) {
            return BehaviourState.Success
        }
        return BehaviourState.Running
    }

    private fun pickTile(bot: Bot): Tile? {
        val origin = bot.tile
        val currentCount = allyCount(bot, origin)
        if (currentCount == 0) return null
        val best = mutableListOf<Tile>()
        var bestCount = currentCount
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                if (dx == 0 && dy == 0) continue
                val candidate = origin.add(dx, dy)
                val count = allyCount(bot, candidate)
                if (count < bestCount) {
                    bestCount = count
                    best.clear()
                    best.add(candidate)
                } else if (count == bestCount) {
                    best.add(candidate)
                }
            }
        }
        if (best.isEmpty()) return null
        return best[random.nextInt(best.size)]
    }

    private fun allyCount(bot: Bot, tile: Tile): Int {
        val context = bot.combatContext ?: return 0
        val id = tile.id
        return context.nearbyAllies.count { !it.dead && it.tile.id == id }
    }
}
