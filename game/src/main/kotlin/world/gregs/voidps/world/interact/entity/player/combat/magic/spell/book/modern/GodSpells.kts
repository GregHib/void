package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.spellSwing

val godSpells = setOf("claws_of_guthix", "flames_of_zamorak", "saradomin_strike")

spellSwing(godSpells, Priority.LOW) { player ->
    player.setAnimation("cast_god_spell")
    player.hit(target, delay = 2)
    delay = 5
}