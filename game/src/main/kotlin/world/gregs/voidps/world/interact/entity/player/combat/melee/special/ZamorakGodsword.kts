package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import java.util.concurrent.TimeUnit

fun isZamorakGodsword(weapon: Item) = weapon.id.startsWith("zamorak_godsword")

on<CombatSwing>({ !swung() && it.specialAttack && isZamorakGodsword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("ice_cleave")
    player.setGraphic("ice_cleave")
    if (player.hit(target) != -1) {
        player.freeze(target, TimeUnit.SECONDS.toTicks(20))
    }
    delay = 6
}

on<CombatHit>({ special && isZamorakGodsword(weapon) }) { character: Character ->
    character.setGraphic("ice_cleave_hit")
}