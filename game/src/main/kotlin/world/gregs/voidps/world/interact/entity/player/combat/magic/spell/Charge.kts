package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.extra.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()

fun wearingMatchingArenaGear(player: Player): Boolean = isMatchingArenaSpell(player.spell, player.equipped(EquipSlot.Cape).id)
fun isMatchingArenaSpell(spell: String, cape: String): Boolean = isSaradomin(spell, cape) || isGuthix(spell, cape) || isZamorak(spell, cape)
fun isSaradomin(spell: String, cape: String): Boolean = spell == "saradomin_strike" && cape == "saradomin_cape"
fun isGuthix(spell: String, cape: String): Boolean = spell == "claws_of_guthix" && cape == "guthix_cape"
fun isZamorak(spell: String, cape: String): Boolean = spell == "flames_of_zamorak" && cape == "zamorak_cape"

on<HitDamageModifier>({ player -> type == "magic" && player.hasClock("charge") && wearingMatchingArenaGear(player) }, Priority.HIGHEST) { _: Player ->
    damage += 100.0
}

on<InterfaceOption>({ id.endsWith("_spellbook") && component == "charge" }) { player: Player ->
    if (player.hasClock("charge_delay")) {
        val remaining = TICKS.toSeconds(player.remaining("charge_delay"))
        player.message("You must wait another $remaining ${"second".plural(remaining)} before casting this spell again.")
        return@on
    }
    val spell = component
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }

    val definition = definitions.get(spell)
    player.setAnimation(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.start("charge", definition["effect_ticks"])
    player.start("charge_delay", definition["delay_ticks"])
}