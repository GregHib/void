package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell

val definitions: SpellDefinitions by inject()

fun isSpell(spell: String) = spell == "claws_of_guthix" || spell == "flames_of_zamorak" || spell == "saradomin_strike"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("cast_god_spell")
    val def = definitions.getValue(player.spell)
    player["spell_damage"] = def.damage
    player["spell_experience"] = def.experience
    player.hit(target)
    delay = 5
}