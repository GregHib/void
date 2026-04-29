package content.bot.behaviour.action

import com.github.michaelbull.logging.InlineLogger
import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.skill.magic.spell.hasSpellItems
import content.skill.magic.spell.spellBook
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.network.client.instruction.InteractInterface

private val logger = InlineLogger("PvpBots")

object BotCastVengeance : BotAction {
    private const val MIN_MAGIC = 94
    private const val SPELLBOOK = "lunar_spellbook"
    private const val SPELL = "vengeance"
    private const val RECHECK_SECONDS = 5

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (player.levels.get(Skill.Magic) < MIN_MAGIC) return BehaviourState.Success
        val now = epochSeconds()
        val nextCheck = player.get("bot_vengeance_next_check", 0)
        if (now < nextCheck) return BehaviourState.Success
        player["bot_vengeance_next_check"] = now + RECHECK_SECONDS
        if (player.contains("vengeance")) return BehaviourState.Success
        if (player.remaining("vengeance_delay", now) > 0) return BehaviourState.Success
        // Don't disrupt other modal flows by opening the spellbook over them — e.g. the
        // clan-wars portal "Go in" warning, which the bot's resolver depends on staying open
        // long enough to click. Skip vengeance until the menu is clear.
        val openMenu = player.menu
        if (openMenu != null && openMenu != SPELLBOOK) return BehaviourState.Success
        if (player.spellBook != SPELLBOOK && !player.open(SPELLBOOK)) {
            logger.debug { "Vengeance: '${player.accountName}' failed to open lunar_spellbook (current=${player.spellBook})" }
            return BehaviourState.Success
        }
        if (!player.hasSpellItems(SPELL, message = false)) {
            logger.debug { "Vengeance: '${player.accountName}' missing runes" }
            return BehaviourState.Success
        }
        val interfaceDef = InterfaceDefinitions.getOrNull(SPELLBOOK) ?: return BehaviourState.Success
        val componentId = InterfaceDefinitions.getComponentId(SPELLBOOK, SPELL) ?: return BehaviourState.Success
        val componentDef = InterfaceDefinitions.getComponent(SPELLBOOK, SPELL) ?: return BehaviourState.Success
        val options = componentDef.options ?: componentDef.getOrNull("options") ?: run {
            logger.debug { "Vengeance: no options on $SPELLBOOK:$SPELL component" }
            return BehaviourState.Success
        }
        val option = options.indexOf("Cast")
        if (option == -1) {
            logger.debug { "Vengeance: 'Cast' not in options=${options.toList()}" }
            return BehaviourState.Success
        }
        val valid = world.execute(player, InteractInterface(interfaceDef.id, componentId, -1, -1, option))
        if (!valid) {
            logger.debug { "Vengeance: '${player.accountName}' InteractInterface rejected (interface=${interfaceDef.id} comp=$componentId opt=$option)" }
            return BehaviourState.Failed(Reason.Invalid("Invalid vengeance cast"))
        }
        logger.info { "Vengeance: '${player.accountName}' cast" }
        return BehaviourState.Success
    }
}
