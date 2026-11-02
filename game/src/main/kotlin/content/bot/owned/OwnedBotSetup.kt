package content.bot.owned

import content.bot.Bot
import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart

/**
 * Makes a freshly connected bot a copy of its owner: persistent variables (minus the owner's own bookkeeping),
 * skills, looks, and a bank that *is* the owner's bank.
 */
object OwnedBotSetup {

    /**
     * Owner variables an owned bot must not inherit: its own roster, account identity, bank tab counters,
     * social state and anything that points at another player or a place the owner is in.
     */
    private val SKIP_PREFIXES = listOf(
        "owned_", "bot_ui", "display_name", "name_history", "previous_name", "creation", "playtime", "rights",
        "logged_out", "bank_tab_", "open_bank_tab", "last_bank_amount", "bank_", "random_event", "house", "exchange",
        "friends", "ignores", "muted", "clan", "trade", "duel", "party", "in_wilderness", "skull", "teleblock",
        "instance", "minigame", "last_login", "email", "password",
    )

    fun apply(owner: Player, bot: Bot, slot: Int) {
        val player = bot.player
        player["owner"] = owner.accountName
        player["owner_index"] = owner.index
        player["owned_slot"] = slot
        player["owned"] = true
        // Nothing a fresh account was given may reach the owner's bank
        player.inventory.transaction { clear() }
        player.equipment.transaction { clear() }
        copyVariables(owner, player)
        syncStats(owner, player)
        copyLooks(owner, player)
        // The bot's bank is the owner's: existing bank checks and resolvers read the owner's items, and every
        // change the bot makes goes to the owner's client because the owner is the bound listener. The bank
        // interface is never opened by an owned bot (tab counters are owner variables).
        player.inventories.instances["bank"] = owner.bank
    }

    fun copyVariables(owner: Player, player: Player) {
        for ((key, value) in owner.variables.data) {
            if (SKIP_PREFIXES.any { key.startsWith(it) }) {
                continue
            }
            // Collections would be shared by reference; primitives only.
            if (value !is Int && value !is Boolean && value !is String && value !is Long && value !is Double) {
                continue
            }
            player.variables.set(key, value, false)
        }
    }

    fun syncStats(owner: Player, player: Player) {
        for (skill in Skill.all) {
            player.experience.set(skill, owner.experience.get(skill))
            player.levels.set(skill, owner.levels.getMax(skill))
        }
    }

    fun copyLooks(owner: Player, player: Player) {
        val from = owner.body
        val body = player.body
        body.male = from.male
        for (part in BodyPart.entries) {
            body.setLook(part, from.getLook(part))
        }
        for (colour in BodyColour.entries) {
            body.setColour(colour, from.getColour(colour))
        }
        player.appearance.emote = owner.appearance.emote
        player.flagAppearance()
    }
}
