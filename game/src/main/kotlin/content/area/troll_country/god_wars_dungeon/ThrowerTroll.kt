package content.area.troll_country.god_wars_dungeon

import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.proj.shoot
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.event.Script
@Script
class ThrowerTroll {

    init {
        characterCombatDamage("troll_rock", "range") { character ->
            // TODO need range gfx field
            //  Could potentially rename `type` and have type as the spell/ammo?
        //      TODO Combat vs attack style
            character.sound("troll_rock_defend")
        }

        npcCombatSwing("thrower_troll_trollheim*") { npc ->
            if (random.nextInt(10) == 0) {
                npc.say("Urg!")
            }
            areaSound("thrower_troll_attack", npc.tile, radius = 10)
            npc.anim("thrower_troll_attack")
            npc.shoot("troll_rock", target)
            npc.hit(target, offensiveType = "range")
        }

    }

}
