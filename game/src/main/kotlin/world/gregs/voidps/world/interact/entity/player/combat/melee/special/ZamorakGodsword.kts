package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import java.util.concurrent.TimeUnit

specialAttack("ice_cleave") { player ->
    player.setAnimation(id)
    player.setGraphic(id)
    if (player.hit(target) != -1) {
        player.freeze(target, TimeUnit.SECONDS.toTicks(20))
    }
}

characterCombatHit("zamorak_godsword*", special = true) { character ->
    character.setGraphic("ice_cleave_hit")
}