package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.magic.cast
import world.gregs.voidps.world.interact.entity.player.combat.magic.getElementalSpellDamage
import world.gregs.voidps.world.interact.entity.player.combat.magic.getElementalSpellExperience

fun isSpell(spell: String) = spell.startsWith("water")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOWER) { player: Player ->
    val spell = player.spell
    val staff = if (player.weapon.name.endsWith("staff")) "_staff" else ""
    player.setAnimation("water_spell${staff}")
    player.setGraphic("water_spell${staff}_cast")
    player.cast(name = spell, target = target)
    player["spell_damage"] = getElementalSpellDamage(spell)
    player["spell_experience"] = getElementalSpellExperience(spell)
    player.hit(target)
    delay = 5
}

on<CombatHit>({ isSpell(spell) }) { character: Character ->
    character.setGraphic("${spell}_hit", height = 100)
}