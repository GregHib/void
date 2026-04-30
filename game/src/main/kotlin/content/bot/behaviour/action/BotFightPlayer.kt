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
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItem
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
    val lootStrategy: BotLootStrategy = BotLootStrategy.DEFAULT,
    val targetScorer: TargetScorer? = null,
    val area: String? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame) = when {
        // Success first so a retreat-by-teleport (bot now outside `area`) can complete the
        // activity even at low HP — otherwise eat() spins forever when food is exhausted.
        success?.check(bot.player) == true -> BehaviourState.Success
        healPercentage > 0 && bot.levels.get(Skill.Constitution) <= bot.levels.getMax(Skill.Constitution) * healPercentage / 100 -> eat(bot, world)
        bot.mode is PlayerOnPlayerInteract -> handleEngaged(bot, world)
        bot.mode is PlayerOnFloorItemInteract -> BehaviourState.Running
        bot.mode is EmptyMode -> search(bot, world)
        else -> null
    }

    private fun handleEngaged(bot: Bot, world: BotWorld): BehaviourState {
        val mode = bot.mode as PlayerOnPlayerInteract
        if (targetScorer != null && shouldRepick(bot, mode.target)) {
            val context = bot.combatContext
            if (context != null && context.nearbyEnemies.isNotEmpty()) {
                val best = targetScorer.pick(bot.player, context.nearbyEnemies, context)
                if (best != null && best !== mode.target) {
                    val attackOption = bot.player.options.indexOf("Attack")
                    if (attackOption != -1) {
                        world.execute(bot.player, InteractPlayer(best.index, attackOption))
                        bot.player.start("fight_starting", 5)
                        return BehaviourState.Running
                    }
                }
            }
        }
        if (targetScorer == null && targetGone(bot, mode.target)) {
            // Target has clearly left (different level or far outside our scan radius — typical
            // sign of a teleport-out). Clear the stale interact so search() picks a new target
            // when the activity loops back via restart.
            bot.player.mode = EmptyMode
        }
        BotArenaCenter.maybeRecenter(bot, world, area)
        return if (success == null) BehaviourState.Success else BehaviourState.Running
    }

    private fun shouldRepick(bot: Bot, current: Player): Boolean {
        if (current.dead) return true
        if (current.tile.level != bot.player.tile.level) return true
        if (bot.player.tile.distanceTo(current.tile) > radius) return true
        return !Target.attackable(bot.player, current, message = false)
    }

    /**
     * Cheap, non-throwing "target obviously left" check used when no [targetScorer] is configured.
     * Avoids the heavier attackable/dead checks in [shouldRepick] to stay compatible with relaxed
     * mocks in tests where those properties aren't stubbed.
     */
    private fun targetGone(bot: Bot, target: Player): Boolean {
        if (target.tile.level != bot.player.tile.level) return true
        return bot.player.tile.distanceTo(target.tile) > radius * 2
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
            // Window for a follow-up brew reactive to chain on top of the food (overheal stack).
            bot.player.start("just_ate_food", 2)
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Running
    }

    private fun search(bot: Bot, world: BotWorld): BehaviourState {
        val player = bot.player
        val attackOption = player.options.indexOf("Attack")
        if (player.hasClock("loot_pending")) {
            val lootResult = takeLoot(bot, world)
            if (lootResult != null) return lootResult
            // Nothing eligible found this tick; stop scanning until the next kill.
            player.stop("loot_pending")
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
            // Open a brief window for fight-start reactives (e.g. boost potions) to fire once
            // per new engagement; gated on this clock so they don't re-drink on every decay tick.
            bot.player.start("fight_starting", 5)
            return BehaviourState.Running
        }
        if (BotArenaCenter.maybeRecenter(bot, world, area)) return BehaviourState.Running
        return handleNoTarget()
    }

    private fun takeLoot(bot: Bot, world: BotWorld): BehaviourState? {
        val player = bot.player
        var bestItem: FloorItem? = null
        var bestScore = Long.MIN_VALUE
        for (tile in Spiral.spiral(player.tile, radius)) {
            for (item in FloorItems.at(tile)) {
                if (!isLootable(player, item)) continue
                if (!lootStrategy.ranks()) {
                    return executeTake(bot, world, item)
                }
                val score = lootStrategy.score(item)
                if (score > bestScore) {
                    bestScore = score
                    bestItem = item
                }
            }
        }
        return bestItem?.let { executeTake(bot, world, it) }
    }

    private fun isLootable(player: Player, item: FloorItem): Boolean {
        if (item.owner != player.accountName) return false
        if (item.def.cost <= lootOverValue) return false
        if (!lootStrategy.accepts(item)) return false
        // Reserve one inventory slot (e.g. for weapon switches) unless the item can stack onto an existing pile.
        val inv = player.inventory
        val canStack = item.def.stackable == 1 && inv.indexOf(item.id) >= 0
        if (inv.spaces <= 1 && !canStack) return false
        return true
    }

    private fun executeTake(bot: Bot, world: BotWorld, item: FloorItem): BehaviourState {
        val index = item.def.floorOptions.indexOf("Take")
        val valid = world.execute(bot.player, InteractFloorItem(item.def.id, item.tile.x, item.tile.y, index))
        if (!valid) {
            return BehaviourState.Failed(Reason.Invalid("Invalid floor item interaction: $item $index"))
        }
        return BehaviourState.Running
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
        return Players.at(tile).filter { it !== player && !it.dead && Target.attackable(player, it, message = false) }
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
