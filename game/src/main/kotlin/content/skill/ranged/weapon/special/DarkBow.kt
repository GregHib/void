package content.skill.ranged.weapon.special

import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.proj.shoot
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.distanceTo

class DarkBow : Script, SpecialAttack {

    init {
        specialAttack("descent_of_darkness") { target, _ ->
            val dragon = ammo == "dragon_arrow"
            anim("bow_accurate")
            gfx("${ammo}_double_shot")
            sound("dark_bow_special")
            sound("descent_of_${if (dragon) "dragons" else "darkness"}")

            val time1 = shoot("descent_of_arrow", target, true)
            shoot("arrow_smoke", target, true)
            if (dragon) {
                shoot("descent_of_dragons_head", target, true)
            }

            val time2 = shoot("descent_of_arrow", target, false)
            shoot("arrow_smoke_2", target, false)
            if (dragon) {
                shoot("descent_of_dragons_head", target, false)
            }
            hit(target, delay = time1)
            hit(target, delay = time2)
        }

        characterCombatDamage("dark_bow*", "range") { character ->
            source.sound("descent_of_darkness")
            source.sound("descent_of_darkness", delay = 20)
            character.gfx("descent_of_${if (source.ammo == "dragon_arrow") "dragons" else "darkness"}_impact")
        }

        combatSwing("dark_bow*", "range") { target ->
            anim("bow_accurate")
            val ammo = ammo
            gfx("${ammo}_double_shot")
            val time1 = shoot(ammo, target, true)
            val time2 = shoot(ammo, target, false)
            hit(target, delay = time1)
            hit(target, delay = time2)
        }
    }

    fun Player.shoot(id: String, target: Character, high: Boolean): Int {
        val distance = tile.distanceTo(target)
        return shoot(id = id, delay = 41, target = target, height = if (high) 43 else 40, flightTime = (if (high) 14 else 5) + distance * 10, curve = if (high) 25 else 5)
    }
}
