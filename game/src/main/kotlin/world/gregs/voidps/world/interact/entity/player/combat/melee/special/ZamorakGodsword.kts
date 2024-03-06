package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.effect.freeze
import java.util.concurrent.TimeUnit

combatSwing("zamorak_godsword*", "melee", special = true) { player ->
    player.setAnimation("ice_cleave")
    player.setGraphic("ice_cleave")
    if (player.hit(target) != -1) {
        player.freeze(target, TimeUnit.SECONDS.toTicks(20))
    }
}

characterCombatHit("zamorak_godsword*", special = true) { character ->
    character.setGraphic("ice_cleave_hit")
}