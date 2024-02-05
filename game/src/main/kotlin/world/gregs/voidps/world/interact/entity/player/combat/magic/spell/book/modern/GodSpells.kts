package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

fun isGodSpell(spell: String) = spell == "claws_of_guthix" || spell == "flames_of_zamorak" || spell == "saradomin_strike"

combatSwing({ player -> !swung() && isGodSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("cast_god_spell")
    player.hit(target, delay = 2)
    delay = 5
}