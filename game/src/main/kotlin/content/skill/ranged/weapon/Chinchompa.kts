package content.skill.ranged.weapon

import content.area.wilderness.inMultiCombat
import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.directHit
import content.entity.sound.sound
import content.skill.melee.weapon.multiTargets
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random
import kotlin.random.nextInt

characterCombatDamage("*chinchompa", "range") { character ->
    source as Player
    source.sound("chinchompa_explode", delay = 40)
    character.gfx("chinchompa_impact")
}

combatAttack(type = "range") { source ->
    if (weapon.id.endsWith("chinchompa") && target.inMultiCombat) {
        val targets = multiTargets(target, if (target is Player) 9 else 11)
        for (target in targets) {
            target.directHit(source, random.nextInt(0..damage), type, weapon, spell)
        }
    }
}
