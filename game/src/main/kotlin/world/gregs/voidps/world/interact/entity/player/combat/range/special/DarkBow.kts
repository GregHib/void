package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

specialAttack("descent_of_darkness") { player ->
    val dragon = player.ammo == "dragon_arrow"
    player.setAnimation("bow_accurate")
    player.setGraphic("${player.ammo}_double_shot")
    player.playSound("dark_bow_special")
    player.playSound("descent_of_${if (dragon) "dragons" else "darkness"}")

    val time1 = player.shoot("descent_of_arrow", target, true)
    player.shoot("arrow_smoke", target, true)
    if (dragon) {
        player.shoot("descent_of_dragons_head", target, true)
    }

    val time2 = player.shoot("descent_of_arrow", target, false)
    player.shoot("arrow_smoke_2", target, false)
    if (dragon) {
        player.shoot("descent_of_dragons_head", target, false)
    }
    player.hit(target, delay = time1)
    player.hit(target, delay = time2)
}

characterCombatHit("dark_bow*", "range") { character ->
    source.playSound("descent_of_darkness")
    source.playSound("descent_of_darkness", delay = 20)
    character.setGraphic("descent_of_${if (source.ammo == "dragon_arrow") "dragons" else "darkness"}_hit")
}

combatSwing("dark_bow*", "range") { player ->
    player.setAnimation("bow_accurate")
    val ammo = player.ammo
    player.setGraphic("${ammo}_double_shot")
    val time1 = player.shoot(ammo, target, true)
    val time2 = player.shoot(ammo, target, false)
    player.hit(target, delay = time1)
    player.hit(target, delay = time2)
}

fun Player.shoot(id: String, target: Character, high: Boolean): Int {
    val distance = tile.distanceTo(target)
    return shoot(id = id, delay = 41, target = target, height = if (high) 43 else 40, flightTime = (if (high) 14 else 5) + distance * 10, curve = if (high) 25 else 5)
}