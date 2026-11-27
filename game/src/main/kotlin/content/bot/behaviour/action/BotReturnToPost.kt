package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.entity.combat.attacking
import content.entity.combat.underAttack
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile

/**
 * A bot holding a post (`owned_post`, the tile's id) walks back to it when a fight or a pickup has drawn it off.
 * Done at once when it is there, has no post, or is busy (fighting, mid-order); done too when it cannot get back,
 * so the hold goes on from where it stands rather than failing.
 */
class BotReturnToPost : BotAction {
    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val post = bot.player["owned_post", -1]
        if (post == -1 || bot.tile.id == post || bot.player.attacking || bot.player.underAttack || bot.mode !is EmptyMode) {
            return BehaviourState.Success
        }
        val tile = Tile(post)
        world.execute(bot.player, Walk(tile.x, tile.y))
        return BehaviourState.Running
    }

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val post = bot.player["owned_post", -1]
        return when {
            post == -1 || bot.tile.id == post -> BehaviourState.Success
            bot.player.attacking || bot.player.underAttack -> BehaviourState.Success
            // stopped short (the post taken, or no way): hold from here
            bot.mode is EmptyMode && GameLoop.tick - bot.steps.last > 3 -> BehaviourState.Success
            else -> BehaviourState.Running
        }
    }
}
