package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "magic_dart"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOWER) { player: Player ->
    player.setAnimation("magic_dart")
    player.setGraphic("magic_dart_cast", height = 100)
    player.shoot(name = player.spell, target = target, delay = 43, height = player.height + 4, endHeight = target.height - 4, curve = 14, offset = 1)
    player["spell_damage"] = -1.0
    player["spell_experience"] = 30.0
    player.hit(target)
    delay = 5
}

on<HitDamageModifier>({ isSpell(spell) }) { player: Player ->
    damage = player.levels.get(Skill.Magic) + 100.0
}