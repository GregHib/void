package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.entity.combat.attacker
import content.entity.combat.dead
import content.entity.combat.underAttack
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.client.instruction.InteractNPC

data class BotFightNpc(
    val id: String,
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
    val lootOverValue: Int = 0,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        success?.check(bot.player) == true -> BehaviourState.Success
        bot.mode is PlayerOnNPCInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
        bot.mode is PlayerOnFloorItemInteract -> BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun search(bot: Bot, world: BotWorld): BehaviourState {
        val player = bot.player
        for (tile in Spiral.spiral(player.tile, radius)) {
            for (item in FloorItems.at(tile)) {
                if (item.owner != player.accountName) {
                    continue
                }
                if (item.def.cost <= lootOverValue) {
                    continue
                }
                val index = item.def.floorOptions.indexOf("Take")
                val valid = world.execute(bot.player, InteractFloorItem(item.def.id, item.tile.x, item.tile.y, index))
                if (!valid) {
                    return BehaviourState.Failed(Reason.Invalid("Invalid floor item interaction: $item $index"))
                }
                return BehaviourState.Running
            }
            for (npc in NPCs.at(tile)) {
                if (!wildcardEquals(id, npc.id)) {
                    continue
                }
                val index = npc.def(player).options.indexOf("Attack")
                if (index == -1) {
                    continue
                }
                if (npc.dead || (npc.underAttack && npc.attacker != player)) {
                    continue
                }
                val valid = world.execute(bot.player, InteractNPC(npc.index, index + 1))
                if (!valid) {
                    return BehaviourState.Failed(Reason.Invalid("Invalid npc interaction: ${npc.index} ${index + 1}"))
                }
                bot.player.start("fight_starting", 5)
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
