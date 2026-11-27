package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.entity.combat.attacking
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.network.client.instruction.InteractNPC

/**
 * An owned bot attacks the npc its owner pointed at (by [index]) until it is dead or gone, then is done; a
 * fight it can't start (unreachable, the npc already taken in single-way combat) fails the order.
 */
data class BotOwnedAttack(val index: Int) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val npc = NPCs.indexed(index)
        if (npc == null || npc.dead) {
            return BehaviourState.Success
        }
        val player = bot.player
        if (bot.mode is PlayerOnNPCInteract || player.attacking) {
            return BehaviourState.Running
        }
        if (bot.mode !is EmptyMode) {
            return BehaviourState.Running
        }
        val option = npc.def(player).options.indexOf("Attack")
        if (option == -1) {
            return BehaviourState.Failed(Reason.Invalid("${npc.id} can't be attacked."))
        }
        if (!world.execute(player, InteractNPC(npc.index, option + 1))) {
            return BehaviourState.Failed(Reason.Invalid("Invalid npc interaction: ${npc.index} ${option + 1}"))
        }
        player.start("fight_starting", 5)
        return BehaviourState.Running
    }
}
