package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "iban_blast"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("iban_blast")
    player.setGraphic("iban_blast_cast")
    player.shoot(name = player.spell, target = target)
    player["spell_damage"] = 250.0
    player["spell_experience"] = 30.0
    player.hit(target)
    delay = 5
}