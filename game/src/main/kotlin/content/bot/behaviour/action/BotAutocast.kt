package content.bot.behaviour.action

import content.skill.magic.spell.spellBook
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Set [player]'s autocast to [spell] (an ancient_spellbook component id, e.g. "ice_barrage").
 * No-op when [spell] is null. Idempotent — re-call with the same spell short-circuits.
 *
 * Shared between [BotCastSpell] (per-attack autocast selection) and [BotSwitchLoadout] (autocast
 * binding when entering a magic loadout).
 */
internal fun ensureAutocast(player: Player, spell: String?) {
    if (spell == null) return
    if (player.spellBook != "ancient_spellbook") {
        player.open("ancient_spellbook")
    }
    val castId: Int = InterfaceDefinitions.getComponent("ancient_spellbook", spell)?.getOrNull("cast_id") ?: return
    if (player.get("autocast", 0) == castId) return
    player.set("autocast_spell", spell)
    player.set("autocast", castId)
}
