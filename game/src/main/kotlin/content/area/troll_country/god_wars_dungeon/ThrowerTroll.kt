package content.area.troll_country.god_wars_dungeon

import content.entity.combat.hit.hit
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.random

class ThrowerTroll : Script {

    init {
        combatDamage("range", ::attack)
        npcCombatDamage("troll_rock", "range", ::attack)

        npcCombatSwing("thrower_troll_trollheim*") { target ->
            if (random.nextInt(10) == 0) {
                say("Urg!")
            }
            areaSound("thrower_troll_attack", tile, radius = 10)
            anim("thrower_troll_attack")
            shoot("troll_rock", target)
            hit(target, offensiveType = "range")
        }
    }

    fun attack(target: Character, damage: CombatDamage) {
        // TODO need range gfx field
        //  Could potentially rename `type` and have type as the spell/ammo?
        //      TODO Combat vs attack style
        damage.source.sound("troll_rock_defend")
    }
}
