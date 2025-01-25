package content.skill.ranged.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random
import content.entity.combat.hit.characterCombatHit
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.directHit
import content.area.wilderness.inMultiCombat
import content.skill.melee.weapon.multiTargets
import content.entity.sound.playSound
import kotlin.random.nextInt

characterCombatHit("*chinchompa", "range") { character ->
    source as Player
    source.playSound("chinchompa_explode", delay = 40)
    character.gfx("chinchompa_hit")
}

combatAttack(type = "range") { source ->
    if (weapon.id.endsWith("chinchompa") && target.inMultiCombat) {
        val targets = multiTargets(target, if (target is Player) 9 else 11)
        for (target in targets) {
            target.directHit(source, random.nextInt(0..damage), type, weapon, spell)
        }
    }
}