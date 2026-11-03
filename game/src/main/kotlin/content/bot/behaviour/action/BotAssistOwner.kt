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

/**
 * Reactive: a following owned bot fights beside its owner by its `owned_aggression`.
 * - aggressive: attacks what the owner is attacking; in single-way combat, where that NPC is taken, the
 *   nearest NPC of the same name (so a cow's colour variants, a goblin's armours, all count).
 * - passive: never starts a fight, but in multi-way combat attacks whatever is attacking the owner.
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
            // Let go when the owner is done, unless the NPC is still on the bot
            if (!owner.fighting() && !player.underAttack) {
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
        if (player["owned_loot", true] && loot(bot, world)) {
            return BehaviourState.Success
        }
        val target = pick(player, owner, aggressive) ?: return BehaviourState.Success
        val option = target.def(player).options.indexOf("Attack")
        if (option == -1 || !Target.attackable(player, target, message = false)) {
            return BehaviourState.Success
        }
        world.execute(player, InteractNPC(target.index, option + 1))
        player.start("fight_starting", 5)
        return BehaviourState.Success
    }

    private fun Player.fighting(): Boolean = target != null || underAttack

    private fun pick(player: Player, owner: Player, aggressive: Boolean): NPC? {
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

    /** Picks up the bot's own drops (floor items it owns), as BotFightNpc does. */
    private fun loot(bot: Bot, world: BotWorld): Boolean {
        val player = bot.player
        for (tile in Spiral.spiral(player.tile, LOOT_RADIUS)) {
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
        const val RADIUS = 8
        const val LOOT_RADIUS = 3
    }
}
