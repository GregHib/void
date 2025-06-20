package content.area.troll_country.god_wars_dungeon.zamorak

import content.entity.combat.hit.combatDamage
import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.hit.npcCombatDamage
import content.entity.combat.npcCombatSwing
import content.entity.effect.toxin.poison
import content.entity.gfx.areaGfx
import content.entity.sound.areaSound
import content.entity.sound.sound
import content.skill.prayer.protectMelee
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random

val skills = Skill.entries.toMutableSet().apply {
    remove(Skill.Constitution)
}

npcCombatAttack("*gorak") {
    if (target is Player && damage > 0) {
        target.levels.drain(skills.random(random), random.nextInt(1, 4))
    }
}

combatDamage { player ->
    if (source is NPC && source.id.endsWith("gorak") && player.protectMelee()) {
        player.message("Your protective prayer doesn't seem to work!")
    }
}