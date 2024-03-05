package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit

val handler: suspend CombatSwing.(Player) -> Unit = { player ->
    player.setAnimation("cast_god_spell")
    player.hit(target, delay = 2)
    delay = 5
}
combatSwing(spell = "claws_of_guthix", style = "magic", block = handler)
combatSwing(spell = "flames_of_zamorak", style = "magic", block = handler)
combatSwing(spell = "saradomin_strike", style = "magic", block = handler)