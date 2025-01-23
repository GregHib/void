package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargets
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.nextInt

characterCombatHit("*chinchompa", "range") { character ->
    source as Player
    source.playSound("chinchompa_explode", delay = 40)
    character.setGraphic("chinchompa_hit")
}

combatAttack(type = "range") { source ->
    if (weapon.id.endsWith("chinchompa") && target.inMultiCombat) {
        val targets = multiTargets(target, if (target is Player) 9 else 11)
        for (target in targets) {
            target.directHit(source, random.nextInt(0..damage), type, weapon, spell)
        }
    }
}