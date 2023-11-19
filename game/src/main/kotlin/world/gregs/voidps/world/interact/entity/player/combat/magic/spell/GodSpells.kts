package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell

fun isGodSpell(spell: String) = spell == "claws_of_guthix" || spell == "flames_of_zamorak" || spell == "saradomin_strike"

on<CombatSwing>({ player -> !swung() && isGodSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("cast_god_spell")
    player.hit(target, delay = 2)
    delay = 5
}