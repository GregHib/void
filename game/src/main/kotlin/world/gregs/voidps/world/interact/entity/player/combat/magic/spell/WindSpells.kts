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
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.getElementalSpellDamage
import world.gregs.voidps.world.interact.entity.player.combat.magic.getElementalSpellExperience
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell.startsWith("wind")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("wind_spell${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("wind_spell_cast")
    player.shoot(name = spell, target = target)
    player["spell_damage"] = getElementalSpellDamage(spell)
    player["spell_experience"] = getElementalSpellExperience(spell)
    player.hit(target)
    delay = 5
}