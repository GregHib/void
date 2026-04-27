package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.area.wilderness.inMultiCombat
import content.bot.behaviour.utility.TargetScorer
import content.entity.combat.Target
import content.entity.combat.dead
import content.entity.combat.target
import content.entity.effect.frozen
import content.skill.magic.spell.spellBook
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile

data class BotCastSpell(
    val delay: Int = 0,
    val success: Condition? = null,
    val radius: Int = 10,
    val healPercentage: Int = 40,
    val targetScorer: TargetScorer? = null,
    val family: String = "ice",
    val kite: Boolean = true,
    val area: String? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        // Success first so a retreat-by-teleport (bot now outside `area`) can complete the
        // activity even at low HP — otherwise eat() spins forever when food is exhausted.
        if (success?.check(bot.player) == true) return BehaviourState.Success
        if (healPercentage > 0 && bot.levels.get(Skill.Constitution) <= bot.levels.getMax(Skill.Constitution) * healPercentage / 100) {
            return eat(bot, world)
        }
        val target = engagedTarget(bot)
        if (target != null) return handleCombat(bot, world, target)
        if (bot.mode is PlayerOnFloorItemInteract) return BehaviourState.Running
        if (bot.mode is EmptyMode) return search(bot, world)
        return null
    }

    private fun engagedTarget(bot: Bot): Player? {
        val mode = bot.mode
        if (mode is PlayerOnPlayerInteract) return mode.target
        if (mode is CombatMovement) return bot.player.target as? Player
        return null
    }

    private fun handleCombat(bot: Bot, world: BotWorld, currentTarget: Player): BehaviourState {
        if (currentTarget.dead) return BehaviourState.Running
        if (targetScorer != null && shouldRepick(bot, currentTarget)) {
            val context = bot.combatContext
            if (context != null && context.nearbyEnemies.isNotEmpty()) {
                val best = targetScorer.pick(bot.player, context.nearbyEnemies, context)
                if (best != null && best !== currentTarget) {
                    val attackOption = bot.player.options.indexOf("Attack")
                    if (attackOption != -1) {
                        ensureAutocast(bot.player, chooseSpell(bot, best))
                        anchorIfNeeded(bot, best)
                        world.execute(bot.player, InteractPlayer(best.index, attackOption))
                        return BehaviourState.Running
                    }
                }
            }
        }
        if (targetScorer == null && targetGone(bot, currentTarget)) {
            // Target left (teleport-out etc.). Clear the stale interact so search() picks a new
            // target when the activity loops back.
            bot.player.mode = EmptyMode
            return if (success == null) BehaviourState.Success else BehaviourState.Running
        }

        anchorIfNeeded(bot, currentTarget)
        ensureAutocast(bot.player, chooseSpell(bot, currentTarget))
        if (!BotArenaCenter.maybeRecenter(bot, world, area)) {
            maybeKite(bot, world, currentTarget)
        }

        return if (success == null) BehaviourState.Success else BehaviourState.Running
    }

    private fun shouldRepick(bot: Bot, current: Player): Boolean {
        if (current.tile.level != bot.player.tile.level) return true
        if (bot.player.tile.distanceTo(current.tile) > radius) return true
        return !Target.attackable(bot.player, current, message = false)
    }

    /**
     * Cheap, non-throwing "target obviously left" check used when no [targetScorer] is configured.
     * See [BotFightPlayer.targetGone].
     */
    private fun targetGone(bot: Bot, target: Player): Boolean {
        if (target.tile.level != bot.player.tile.level) return true
        return bot.player.tile.distanceTo(target.tile) > radius * 2
    }

    private fun maybeKite(bot: Bot, world: BotWorld, target: Player) {
        if (!kite || !target.frozen) return
        if (bot.tile.distanceTo(target.tile) > 2) return
        val dx = (bot.tile.x - target.tile.x).coerceIn(-1, 1)
        val dy = (bot.tile.y - target.tile.y).coerceIn(-1, 1)
        if (dx == 0 && dy == 0) return
        val kx = bot.tile.x + dx * 2
        val ky = bot.tile.y + dy * 2
        val dest = Tile(kx, ky, bot.tile.level)
        val anchor = bot.player.get<Tile>("bot_kite_anchor")
        if (anchor != null && dest.distanceTo(anchor) > 5) return
        if (area != null && dest !in Areas[area]) return
        world.execute(bot.player, Walk(kx, ky))
    }

    private fun anchorIfNeeded(bot: Bot, target: Player) {
        val current = bot.player.get<Int>("bot_kite_anchor_target")
        if (current == target.index) return
        bot.player["bot_kite_anchor"] = bot.tile
        bot.player["bot_kite_anchor_target"] = target.index
    }

    private fun eat(bot: Bot, world: BotWorld): BehaviourState {
        val inventory = bot.player.inventory
        for (index in inventory.indices) {
            val item = inventory[index]
            val option = item.def.options.indexOf("Eat")
            if (option == -1) continue
            val valid = world.execute(bot.player, InteractInterface(149, 0, item.def.id, index, option))
            if (!valid) return BehaviourState.Failed(Reason.Invalid("Invalid inventory interaction: ${item.def.id} $index $option"))
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Running
    }

    private fun search(bot: Bot, world: BotWorld): BehaviourState {
        val player = bot.player
        val attackOption = player.options.indexOf("Attack")
        if (attackOption == -1) return handleNoTarget()
        val target = pickTarget(bot) ?: run {
            if (BotArenaCenter.maybeRecenter(bot, world, area)) return BehaviourState.Running
            return handleNoTarget()
        }
        ensureAutocast(player, chooseSpell(bot, target))
        anchorIfNeeded(bot, target)
        val valid = world.execute(player, InteractPlayer(target.index, attackOption))
        if (!valid) return BehaviourState.Failed(Reason.Invalid("Invalid player interaction: ${target.index} $attackOption"))
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
        if (context != null) return context.enemiesByTile[tile.id] ?: emptyList()
        val player = bot.player
        return Players.at(tile).filter { it !== player && !it.dead && Target.attackable(player, it, message = false) }
    }

    private fun handleNoTarget(): BehaviourState {
        if (success == null) return BehaviourState.Failed(Reason.NoTarget)
        if (delay > 0) return BehaviourState.Wait(delay, BehaviourState.Running)
        return BehaviourState.Running
    }

    private fun chooseSpell(bot: Bot, target: Player): String? {
        val player = bot.player
        val maxHp = bot.levels.getMax(Skill.Constitution)
        val hpFraction = if (maxHp > 0) bot.levels.get(Skill.Constitution).toDouble() / maxHp else 1.0
        // Re-evaluate only when target identity, frozen state, or HP bucket changes; otherwise reuse last spell.
        val hpBucket = (hpFraction * 4).toInt().coerceIn(0, 4)
        val targetKey = (target.index shl 3) or (hpBucket shl 1) or (if (target.frozen) 1 else 0)
        if (player.get("autocast_choice_key", Int.MIN_VALUE) == targetKey) {
            return player.get<String>("autocast_choice_spell")
        }
        val magic = bot.levels.get(Skill.Magic)
        // Multi-target spells only matter in multi-combat zones; skip the spiral scan otherwise.
        val multi = bot.player.inMultiCombat
        val fam = when {
            hpFraction < 0.50 && magic >= 68 -> "blood"
            !target.frozen && magic >= 58 -> "ice"
            else -> family
        }
        val tier = when {
            multi && magic >= 94 -> "barrage"
            multi && magic >= 70 -> "burst"
            magic >= 84 -> "blitz"
            magic >= 58 -> "rush"
            else -> null
        } ?: return null
        val spell = "${fam}_${tier}"
        player["autocast_choice_key"] = targetKey
        player["autocast_choice_spell"] = spell
        return spell
    }

    private fun ensureAutocast(player: Player, spell: String?) {
        if (spell == null) return
        if (player.spellBook != "ancient_spellbook") {
            player.open("ancient_spellbook")
        }
        val castId: Int = InterfaceDefinitions.getComponent("ancient_spellbook", spell)?.getOrNull("cast_id") ?: return
        if (player.get("autocast", 0) == castId) return
        player.set("autocast_spell", spell)
        player.set("autocast", castId)
    }
}
