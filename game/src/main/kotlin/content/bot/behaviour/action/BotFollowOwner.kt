package content.bot.behaviour.action

import content.area.wilderness.inPvp
import content.area.wilderness.inWilderness
import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.owned.owner
import content.entity.combat.inCombat
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

/**
 * An owned bot follows its owner like a familiar: the client's Follow option, and a recall (teleport to the
 * owner) when it falls too far behind, changes level, or hasn't moved for `bots.owned.recallTicks` while
 * still away from the owner. Fighting is left to the reactive assist action; while the bot is in any other
 * mode this action waits. Action instances are shared between bots, so the stuck counter lives in bot variables.
 */
class BotFollowOwner : BotAction {

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val owner = player.owner ?: return BehaviourState.Failed(Reason.Invalid("Owner is offline."))
        if (player.dead) {
            return BehaviourState.Running
        }
        val distance = player.tile.distanceTo(owner.tile)
        if (distance in 0..1 || player.inCombat) {
            player.clear(STUCK)
            player[LAST] = player.tile.id
        } else {
            val stuck = if (player[LAST, -1] == player.tile.id) player[STUCK, 0] + 1 else 0
            player[STUCK] = stuck
            player[LAST] = player.tile.id
            val recallTicks = Settings["bots.owned.recallTicks", 20]
            if (distance < 0 || distance > RECALL_DISTANCE || stuck >= recallTicks) {
                if (recall(bot, owner)) {
                    player.clear(STUCK)
                    player[LAST] = player.tile.id
                }
                return BehaviourState.Running
            }
        }
        val mode = player.mode
        if (mode is Follow && mode.target === owner) {
            return BehaviourState.Running
        }
        if (mode !is EmptyMode) {
            return BehaviourState.Running
        }
        val option = player.options.indexOf("Follow")
        if (option == -1) {
            return BehaviourState.Failed(Reason.Invalid("No follow option."))
        }
        world.execute(player, InteractPlayer(owner.index, option))
        return BehaviourState.Running
    }

    companion object {
        private const val STUCK = "owned_follow_stuck"
        private const val LAST = "owned_follow_last_tile"

        /** Further than this from the owner and the bot is teleported to them, as a called familiar. */
        const val RECALL_DISTANCE = 14

        /**
         * Teleports [bot] to its [owner] when neither is somewhere that forbids it: beside them, or onto their
         * very tile after a change of level (stairs, a ladder), where any other tile may be the far side of a
         * wall or no floor at all. Returns false when the recall had to wait (bot in combat, owner dead or in
         * a PvP area).
         */
        fun recall(bot: Bot, owner: Player): Boolean {
            val player = bot.player
            // Never into PvP: the wilderness sets in_wilderness, the arenas in_pvp
            if (player.inCombat || owner.inPvp || owner.inWilderness || owner.dead) {
                return false
            }
            player.mode = EmptyMode
            val tile = if (player.tile.level != owner.tile.level) owner.tile else beside(owner.tile)
            player.tele(tile.x, tile.y, tile.level)
            return true
        }

        /**
         * A tile next to [tile] that can be stepped to from it (the eight neighbours, by a step the pathfinder
         * allows, so never across a wall, into an object or onto a tile with no way back), or [tile] itself when
         * there is none: where a summoned or called bot appears.
         */
        fun beside(tile: Tile): Tile {
            val steps: StepValidator = get()
            for (direction in Direction.all) {
                if (steps.canTravel(level = tile.level, x = tile.x, z = tile.y, offsetX = direction.delta.x, offsetZ = direction.delta.y, size = 1, extraFlag = 0, collision = CollisionStrategies.Normal)) {
                    return tile.add(direction)
                }
            }
            return tile
        }
    }
}
