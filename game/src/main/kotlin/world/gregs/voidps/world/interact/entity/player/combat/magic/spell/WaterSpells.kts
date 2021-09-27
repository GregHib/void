package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell.startsWith("water")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    val staff = if (player.weapon.def["category", ""] == "staff") "_staff" else ""
    player.setAnimation("water_spell${staff}")
    player.setGraphic("water_spell${staff}_cast")
    player.shoot(name = spell, target = target)
    player.hit(target)
    delay = 5
}