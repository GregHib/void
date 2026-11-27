package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractInterface

/**
 * The first step of an ordered recovery: the bot casts Lumbridge Home Teleport, the real spell with its long
 * cast, from wherever it is, so the rest of the trip (bank, food, the church's altar) is in Lumbridge. The
 * half-hour wait between casts is waived for a companion, and a bot on another spellbook switches to the
 * modern one for the cast and back after. One instance per recovery, as it remembers what it cast.
 */
class BotOwnedHomeTeleport : BotAction {
    private var cast = false
    private var previous: String? = null
    private var waited = 0

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (player.tile in Areas["lumbridge_teleport"]) {
            restore(player)
            return BehaviourState.Success
        }
        if (!cast) {
            player.clear("home_teleport_timeout")
            previous = BOOKS.firstOrNull { player.hasOpen("${it}_spellbook") }
            if (previous != "modern") {
                player.open("modern_spellbook")
            }
            val component = InterfaceDefinitions.getComponent("modern_spellbook", "lumbridge_home_teleport")
                ?: return BehaviourState.Failed(Reason.Invalid("No home teleport to cast."))
            val option = component.options?.indexOf("Cast")?.takeIf { it != -1 } ?: 0
            if (!world.execute(player, InteractInterface(InterfaceDefinition.id(component.id), InterfaceDefinition.componentId(component.id), -1, -1, option))) {
                restore(player)
                return BehaviourState.Failed(Reason.Invalid("It couldn't cast its home teleport."))
            }
            cast = true
            return BehaviourState.Running
        }
        if (++waited > TIMEOUT) {
            restore(player)
            return BehaviourState.Failed(Reason.Invalid("Its home teleport didn't go through."))
        }
        return BehaviourState.Running
    }

    private fun restore(player: Player) {
        val book = previous ?: return
        previous = null
        if (book != "modern") {
            player.open("${book}_spellbook")
        }
    }

    companion object {
        private val BOOKS = listOf("modern", "ancient", "lunar", "dungeoneering")

        /** Ticks allowed for the cast, which takes about sixteen. */
        private const val TIMEOUT = 40
    }
}
