package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelOverride
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit
import kotlin.math.floor

on<HitEffectiveLevelOverride>({ type == "magic" && defence && target is NPC }, priority = Priority.HIGH) { _: Character ->
    level = (target as NPC).levels.get(Skill.Magic)
}

on<HitEffectiveLevelOverride>({ type == "magic" && defence && target is Player }, priority = Priority.LOW) { _: Character ->
    target as Player
    val level = floor(target.levels.get(Skill.Magic) * 0.7)
    this.level = (floor(this.level * 0.3) + level).toInt()
}

on<CombatHit>({ spell.isNotBlank() }) { character: Character ->
    character.setGraphic("${spell}_hit")
}

on<CombatSwing>({ (delay ?: -1) >= 0 && it.spell.isNotBlank() }, Priority.LOWEST) { character: Character ->
    character.clearVar("spell")
    if (character is Player && !character.hasVar("autocast")) {
        character.queue.clearWeak()
    }
}

multiTargetHit({ isMultiTargetSpell(spell) }, { 9 })