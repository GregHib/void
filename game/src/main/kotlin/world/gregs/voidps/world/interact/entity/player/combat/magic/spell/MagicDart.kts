package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isMagicDart(spell: String) = spell == "magic_dart"

on<CombatSwing>({ player -> !swung() && isMagicDart(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("magic_dart")
    player.setGraphic("magic_dart_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = magicHitDelay(distance))
    delay = 5
}

on<HitDamageModifier>({ isMagicDart(spell) }) { player: Player ->
    damage = player.levels.get(Skill.Magic) + 100.0
}