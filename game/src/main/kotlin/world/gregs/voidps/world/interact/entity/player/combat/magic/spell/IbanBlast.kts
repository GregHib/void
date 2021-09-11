package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "iban_blast"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOWER) { player: Player ->
    player.setAnimation("iban_blast")
    player.setGraphic("iban_blast_cast", height = 100)
    player.shoot(name = player.spell, target = target, delay = 43, height = player.height + 14, endHeight = target.height + 2, offset = 1)
    player["spell_damage"] = 250.0
    player["spell_experience"] = 30.0
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isSpell(spell) }) { character: Character ->
    character.setGraphic("${spell}_hit", height = 100)
}