package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackHit
import java.util.concurrent.TimeUnit

specialAttackHit("ice_cleave") { player ->
    player.freeze(target, TimeUnit.SECONDS.toTicks(20))
}

characterCombatHit("zamorak_godsword*", special = true) { character ->
    character.setGraphic("ice_cleave_hit")
}