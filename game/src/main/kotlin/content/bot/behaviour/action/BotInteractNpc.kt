package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractNPC
import kotlin.collections.indexOf
import kotlin.collections.iterator

data class BotInteractNpc(
    val option: String,
    val id: String,
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
) : BotAction {

    override fun start(bot: Bot, world: BotWorld, frame: BehaviourFrame) = BehaviourState.Running

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        success?.check(bot.player) == true -> BehaviourState.Success
        bot.mode is PlayerOnNPCInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun search(bot: Bot, world: BotWorld): BehaviourState {
        val player = bot.player
        val ids = if (id.contains(",")) id.split(",") else listOf(id)
        for (tile in Spiral.spiral(player.tile, radius)) {
            for (npc in NPCs.at(tile)) {
                if (ids.none { wildcardEquals(it, npc.id) }) {
                    continue
                }
                val index = npc.def(player).options.indexOf(option)
                if (index == -1) {
                    continue
                }
                val valid = world.execute(bot.player, InteractNPC(npc.index, index + 1))
                if (!valid) {
                    return BehaviourState.Failed(Reason.Invalid("Invalid npc interaction: ${npc.index} ${index + 1}"))
                }
                return BehaviourState.Running
            }
        }
        return handleNoTarget()
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
