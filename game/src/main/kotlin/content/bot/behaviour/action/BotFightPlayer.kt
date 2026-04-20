package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.utility.TargetScorer
import content.entity.combat.Target
import content.entity.combat.dead
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.type.Tile

data class BotFightPlayer(
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
    val healPercentage: Int = 20,
    val lootOverValue: Int = 0,
    val targetScorer: TargetScorer? = null,
    val area: String? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        healPercentage > 0 && bot.levels.get(Skill.Constitution) <= bot.levels.getMax(Skill.Constitution) * healPercentage / 100 -> eat(bot, world)
        success?.check(bot.player) == true -> BehaviourState.Success
        bot.mode is PlayerOnPlayerInteract -> handleEngaged(bot, world)
        bot.mode is PlayerOnFloorItemInteract -> BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun handleEngaged(bot: Bot, world: BotWorld): BehaviourState {
        if (targetScorer != null) {
            val context = bot.combatContext
            val mode = bot.mode as PlayerOnPlayerInteract
            if (context != null && context.nearbyEnemies.isNotEmpty()) {
                val best = targetScorer.pick(bot.player, context.nearbyEnemies, context)
                if (best != null && best !== mode.target) {
                    val attackOption = bot.player.options.indexOf("Attack")
                    if (attackOption != -1) {
                        world.execute(bot.player, InteractPlayer(best.index, attackOption))
                        return BehaviourState.Running
                    }
                }
            }
        }
        BotArenaCenter.maybeRecenter(bot, world, area)
        return if (success == null) BehaviourState.Success else BehaviourState.Running
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
        val attackOption = player.options.indexOf("Attack")
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
        }
        if (attackOption == -1) {
            return handleNoTarget()
        }
        val target = pickTarget(bot)
        if (target != null) {
            val valid = world.execute(bot.player, InteractPlayer(target.index, attackOption))
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid player interaction: ${target.index} $attackOption"))
            }
            return BehaviourState.Running
        }
        if (BotArenaCenter.maybeRecenter(bot, world, area)) return BehaviourState.Running
        return handleNoTarget()
    }

    private fun pickTarget(bot: Bot): Player? {
        val context = bot.combatContext
        if (targetScorer != null && context != null && context.nearbyEnemies.isNotEmpty()) {
            return targetScorer.pick(bot.player, context.nearbyEnemies, context)
        }
        for (tile in Spiral.spiral(bot.player.tile, radius)) {
            val first = enemiesAt(bot, tile).firstOrNull()
            if (first != null) return first
        }
        return null
    }

    private fun enemiesAt(bot: Bot, tile: Tile): List<Player> {
        val context = bot.combatContext
        if (context != null) {
            return context.enemiesByTile[tile.id] ?: emptyList()
        }
        val player = bot.player
        return Players.at(tile).filter { it !== player && !it.dead && Target.attackable(player, it) }
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
