package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.owned.OwnedBotStatus
import content.bot.owned.owner
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

/**
 * Reactive: a player-owned bot that is losing a fight warns its owner, then gets out.
 * - At [warn] percent hitpoints, or out of food below three quarters: one message to the owner.
 * - At [flee] percent, or out of food under half health: teleport home with [item] (an escape item the
 *   owner gave it: jewellery is discharged, a tab is used up), else any carried escape item, else a free
 *   teleport when `bots.owned.freeEscape` allows one, else it is left to run.
 * Either way the fight ends with [Reason.Escaped], which [content.bot.owned.OwnedBots] turns into a status
 * and a message; a worn Ring of Life remains the engine's last ditch below that.
 * Action instances are shared between bots, so all state is in bot variables and clocks.
 */
data class BotEscape(
    val warn: Int,
    val flee: Int,
    val item: String?,
) : BotAction {

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (player.dead || player.queue.contains("teleport")) {
            return BehaviourState.Success
        }
        val max = player.levels.getMax(Skill.Constitution)
        if (max <= 0) {
            return BehaviourState.Success
        }
        val percent = player.levels.get(Skill.Constitution) * 100 / max
        val food = hasFood(player)
        if (percent <= flee || (!food && percent <= 50)) {
            escape(player, frame)
            return BehaviourState.Success
        }
        if ((percent <= warn || (!food && percent <= NO_FOOD_WARN)) && !player.hasClock(WARNED)) {
            player.start(WARNED, WARN_INTERVAL)
            player["owned_status"] = OwnedBotStatus.Low.ordinal
            owner(player)?.message("[Bot ${player["owned_slot", 0] + 1}] ${player.name} is at $percent% health${if (food) "." else " with no food."}")
        }
        return BehaviourState.Success
    }

    /** Reactive results are discarded by the manager, so the fight is ended by failing the root [frame] itself. */
    private fun escape(player: Player, frame: BehaviourFrame) {
        if (player.hasClock(ESCAPED)) {
            return
        }
        player.start(ESCAPED, ESCAPE_INTERVAL)
        player.mode = EmptyMode
        val how = when {
            useItem(player) -> "teleports home"
            Settings["bots.owned.freeEscape", false] && Teleport.teleport(player, home(), "modern", force = true) -> "teleports home"
            else -> {
                player[RUN] = true
                "is running for it"
            }
        }
        owner(player)?.message("[Bot ${player["owned_slot", 0] + 1}] ${player.name} is nearly dead and $how!")
        frame.fail(Reason.Escaped)
    }

    /**
     * The owner's chosen escape item, else the first escape item worn or carried: jewellery loses a charge, a
     * teleport tab is used up. Lands at home either way.
     */
    private fun useItem(player: Player): Boolean {
        for (inventory in listOf(player.equipment, player.inventory)) {
            for (index in inventory.indices) {
                val found = inventory[index]
                if (found.isEmpty() || !isEscape(found.id)) {
                    continue
                }
                val used = if (found.id.endsWith("_teleport")) inventory.remove(index, found.id, 1) else inventory.discharge(player, index)
                if (!used) {
                    continue
                }
                return Teleport.teleport(player, home(), "jewellery", force = true)
            }
        }
        return false
    }

    private fun isEscape(id: String): Boolean {
        if (item != null) {
            return id == item || (item.endsWith("*") && id.startsWith(item.dropLast(1)))
        }
        return ESCAPE_PREFIXES.any { id.startsWith(it) } || (id.endsWith("_teleport") && !id.endsWith("_tele_teleport"))
    }

    private fun hasFood(player: Player): Boolean {
        val inventory = player.inventory
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isNotEmpty() && item.def.options.contains("Eat")) {
                return true
            }
        }
        return false
    }

    private fun owner(player: Player): Player? = player.owner

    companion object {
        const val WARNED = "owned_escape_warned"
        const val ESCAPED = "owned_escaped"

        /** Set when no teleport was possible; the owner script walks the bot to safety. */
        const val RUN = "owned_escape_run"
        private const val WARN_INTERVAL = 100
        private const val NO_FOOD_WARN = 75
        private const val ESCAPE_INTERVAL = 20

        val ESCAPE_PREFIXES = listOf("games_necklace_", "ring_of_duelling_", "amulet_of_glory_", "combat_bracelet_", "skills_necklace_", "ring_of_life")

        fun home(): Tile = Tile(Settings["world.home.x", 3222], Settings["world.home.y", 3218], Settings["world.home.level", 0])
    }
}
