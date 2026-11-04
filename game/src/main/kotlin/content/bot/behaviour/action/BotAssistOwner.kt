package content.bot.behaviour.action

import content.area.wilderness.inMultiCombat
import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.owned.owner
import content.entity.combat.Target
import content.entity.combat.attacker
import content.entity.combat.attacking
import content.entity.combat.target
import content.entity.combat.underAttack
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.type.Tile

/**
 * Reactive: a following owned bot fights beside its owner by its `owned_aggression`.
 * - aggressive: attacks what the owner is attacking; in single-way combat, where that NPC is taken, the
 *   nearest NPC of the same name (so a cow's colour variants, a goblin's armours, all count).
 * - passive: never starts a fight, but in multi-way combat attacks whatever is attacking the owner.
 * Either way, with the owner's auto-retaliate on (mirrored to it), it fights back at an npc attacking it: the
 * engine's own retaliate is for a player standing still, and a follower never is. Any fight is on a leash of
 * [ENGAGE_RANGE] tiles to the owner: a bot left that far behind lets go and follows, whatever is on it, so it is
 * never stranded in a fight its owner walked away from.
 * A bot holding a post (`owned_post`) only takes a fight, or a drop, within [RADIUS] of the post.
 *   Defending itself is the engine's auto-retaliate, mirrored from the owner.
 * Either way it lets go when the owner stops fighting, and loots its own kills when `owned_loot` is on.
 */
class BotAssistOwner : BotAction {

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val owner = player.owner ?: return BehaviourState.Success
        if (player.dead || owner.dead) {
            return BehaviourState.Success
        }
        val aggressive = player["owned_aggression", "passive"] == "aggressive"
        val mode = player.mode
        if (player.attacking || mode is PlayerOnNPCInteract) {
            // the attack took: its starts are forgiven
            player.clear(FAILED_STARTS)
            // Let go when the owner is done, unless the NPC is still on the bot; or when the owner has gone too far
            val left = !owner.tile.within(player.tile, ENGAGE_RANGE)
            if (left || (!owner.fighting() && !player.underAttack)) {
                player.target = null
                player.mode = EmptyMode
            }
            return BehaviourState.Success
        }
        if (mode is PlayerOnFloorItemInteract) {
            return BehaviourState.Success
        }
        // Following is interruptible; anything else (walking to a bank, a dialogue) is not
        if (mode !is EmptyMode && mode !is Follow) {
            return BehaviourState.Success
        }
        // an attack just issued is given its ticks to take; one that did not (the target out of reach: across a
        // wall, on another floor) counts against it, and after a few the bot leaves that target be for a while
        // rather than lunging at it every tick
        if (player.hasClock("fight_starting") || player.hasClock(GIVE_UP)) {
            return BehaviourState.Success
        }
        val post = player["owned_post", -1]
        if (player["owned_loot", true] && loot(bot, world, post)) {
            return BehaviourState.Success
        }
        val target = pick(player, owner, aggressive) ?: return BehaviourState.Success
        if (!target.tile.within(player.tile, ENGAGE_RANGE)) {
            // too far to be a fight the bot is in: it follows its owner there first rather than lunging across the map
            return BehaviourState.Success
        }
        if (post != -1 && !target.tile.within(Tile(post), RADIUS)) {
            // beyond the post's reach: a holding bot lets it be
            return BehaviourState.Success
        }
        val option = target.def(player).options.indexOf("Attack")
        if (option == -1 || !Target.attackable(player, target, message = false)) {
            return BehaviourState.Success
        }
        val tries = if (player[LAST_TARGET, -1] == target.index) player[FAILED_STARTS, 0] + 1 else 1
        player[LAST_TARGET] = target.index
        player[FAILED_STARTS] = tries
        if (tries > MAX_STARTS) {
            player.clear(FAILED_STARTS)
            player.start(GIVE_UP, GIVE_UP_TICKS)
            return BehaviourState.Success
        }
        world.execute(player, InteractNPC(target.index, option + 1))
        player.start("fight_starting", 5)
        return BehaviourState.Success
    }

    private fun Player.fighting(): Boolean = target != null || underAttack

    private fun pick(player: Player, owner: Player, aggressive: Boolean): NPC? {
        val onIt = player.attacker as? NPC
        if (onIt != null && player.underAttack && !onIt.dead && player["auto_retaliate", false] && owner.tile.within(player.tile, ENGAGE_RANGE)) {
            return onIt
        }
        if (aggressive) {
            val wanted = owner.target as? NPC
            if (wanted != null && !wanted.dead) {
                if (canHit(player, wanted)) {
                    return wanted
                }
                return nearest(player, wanted.def(player).name, wanted)
            }
        }
        val attacker = owner.attacker as? NPC
        if (attacker != null && owner.underAttack && !attacker.dead && player.inMultiCombat && canHit(player, attacker)) {
            return attacker
        }
        return null
    }

    /** Single-way combat: an NPC someone else is fighting is off limits. */
    private fun canHit(player: Player, npc: NPC): Boolean {
        if (npc.dead) {
            return false
        }
        val attacker = npc.attacker
        return attacker == null || attacker === player || !npc.underAttack || (player.inMultiCombat && npc.inMultiCombat)
    }

    /** The nearest attackable NPC called [name] (the same kind, whatever its variant's id) that is free to fight. */
    private fun nearest(player: Player, name: String, exclude: NPC): NPC? {
        for (tile in Spiral.spiral(player.tile, RADIUS)) {
            for (npc in NPCs.at(tile)) {
                if (npc === exclude || npc.def(player).name != name || !canHit(player, npc)) {
                    continue
                }
                if (npc.def(player).options.indexOf("Attack") == -1) {
                    continue
                }
                return npc
            }
        }
        return null
    }

    /** Picks up the bot's own drops (floor items it owns), as BotFightNpc does; from a [post], only those within its reach. */
    private fun loot(bot: Bot, world: BotWorld, post: Int): Boolean {
        val player = bot.player
        for (tile in Spiral.spiral(player.tile, LOOT_RADIUS)) {
            if (post != -1 && !tile.within(Tile(post), RADIUS)) {
                continue
            }
            for (item in FloorItems.at(tile)) {
                if (item.owner != player.accountName) {
                    continue
                }
                val index = item.def.floorOptions.indexOf("Take")
                if (index == -1) {
                    continue
                }
                return world.execute(player, InteractFloorItem(item.def.id, item.tile.x, item.tile.y, index))
            }
        }
        return false
    }

    companion object {
        /** The target last lunged at, how many starts at it came to nothing, and the clock that then leaves it be. */
        const val LAST_TARGET = "owned_assist_target"
        const val FAILED_STARTS = "owned_assist_starts"
        const val GIVE_UP = "owned_assist_give_up"
        const val MAX_STARTS = 3
        const val GIVE_UP_TICKS = 25

        /** How far (tiles) from the bot a target may be for it to join the fight. */
        const val ENGAGE_RANGE = 12
        const val RADIUS = 8
        const val LOOT_RADIUS = 3
    }
}
