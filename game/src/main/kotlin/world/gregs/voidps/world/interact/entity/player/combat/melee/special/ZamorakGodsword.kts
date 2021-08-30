package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialDamageMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.range.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack
import java.util.concurrent.TimeUnit

fun isZamorakGodsword(weapon: Item?) = weapon != null && weapon.name.startsWith("zamorak_godsword")

specialDamageMultiplier(1.1, ::isZamorakGodsword)
specialAccuracyMultiplier(2.0, ::isZamorakGodsword)

on<CombatSwing>({ !swung() && it.specialAttack && isZamorakGodsword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    player.setAnimation("ice_cleave")
    player.setGraphic("ice_cleave")
    player.hit(target)
    delay = 6
}

on<CombatHit>({ special && isZamorakGodsword(weapon) }) { character: Character ->
    character.start("freeze", TimeUnit.SECONDS.toTicks(20))
    character.setGraphic("ice_cleave_hit")
}