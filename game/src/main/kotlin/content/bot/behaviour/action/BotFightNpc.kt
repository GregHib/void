package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Condition
import content.bot.behaviour.Reason
import content.entity.combat.attackers
import content.entity.combat.dead
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractNPC
import kotlin.collections.indexOf
import kotlin.collections.iterator

data class BotFightNpc(
    val id: String,
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
    val healPercentage: Int = 20,
    val lootOverValue: Int = 0,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        healPercentage > 0 && bot.levels.get(Skill.Constitution) <= bot.levels.getMax(Skill.Constitution) / healPercentage -> eat(bot, world)
        success?.check(bot.player) == true -> BehaviourState.Success
        bot.mode is PlayerOnNPCInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
        bot.mode is PlayerOnFloorItemInteract -> BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun eat(bot: Bot, world: BotWorld): BehaviourState {
        val inventory = bot.player.inventory
        for (index in inventory.indices) {
            val item = inventory[index]
            val option = item.def.options.indexOf("Eat")
            if (option == -1) {
                continue
            }
            val valid = world.execute(bot.player, InteractInterface(149, 0, item.def.id, index, option))
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid inventory interaction: ${item.def.id} $index $option"))
            }
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Running
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
                if (npc.dead || npc.attackers.isNotEmpty() && !npc.attackers.contains(player)) {
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
