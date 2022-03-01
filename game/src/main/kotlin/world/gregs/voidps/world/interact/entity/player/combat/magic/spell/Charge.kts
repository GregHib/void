package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.TICKS
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.network.visual.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()

fun wearingMatchingArenaGear(player: Player): Boolean = isMatchingArenaSpell(player.spell, player.equipped(EquipSlot.Cape).id)
fun isMatchingArenaSpell(spell: String, cape: String): Boolean = isSaradomin(spell, cape) || isGuthix(spell, cape) || isZamorak(spell, cape)
fun isSaradomin(spell: String, cape: String): Boolean = spell == "saradomin_strike" && cape == "saradomin_cape"
fun isGuthix(spell: String, cape: String): Boolean = spell == "claws_of_guthix" && cape == "guthix_cape"
fun isZamorak(spell: String, cape: String): Boolean = spell == "flames_of_zamorak" && cape == "zamorak_cape"

on<HitDamageModifier>({ player -> type == "magic" && player.hasEffect("charge") && wearingMatchingArenaGear(player) }, Priority.HIGHEST) { _: Player ->
    damage += 100.0
}

on<InterfaceOption>({ id.endsWith("_spellbook") && component == "charge" }) { player: Player ->
    if (player.hasEffect("charge_delay")) {
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