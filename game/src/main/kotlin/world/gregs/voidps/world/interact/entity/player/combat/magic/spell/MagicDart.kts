package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "magic_dart"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("magic_dart")
    player.setGraphic("magic_dart_cast")
    player.shoot(name = player.spell, target = target)
    player["spell_damage"] = -1.0
    player["spell_experience"] = 30.0
    player.hit(target)
    delay = 5
}

on<HitDamageModifier>({ isSpell(spell) }) { player: Player ->
    damage = player.levels.get(Skill.Magic) + 100.0
}