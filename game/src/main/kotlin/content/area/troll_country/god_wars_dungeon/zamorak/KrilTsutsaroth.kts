package content.area.troll_country.god_wars_dungeon.zamorak

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.effect.toxin.poison
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.entity.sound.areaSound
import content.entity.sound.sound
import content.skill.prayer.protectMelee
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random

npcCombatSwing("kril_tsutsaroth") { npc ->
    when (random.nextInt(3)) {
        0 -> { // Magic
            target.sound("kril_tsutsaroth_magic", delay = 30)
            npc.anim("kril_tsutsaroth_magic_attack")
            npc.gfx("kril_tsutsaroth_magic_attack")
//            npc.shoot("1211", target)
            npc.hit(target, offensiveType = "magic", delay = 1)
        }
        else -> { // Melee
            npc.anim("kril_tsutsaroth_attack")
            target.sound("kril_tsutsaroth_attack")
            if (random.nextInt(4) == 0) { // TODO check if random is same or dif
                npc.poison(target, 80)
            }
            val slam = target is Player && random.nextInt(3) != 0 && target.protectMelee() && !target.hasClock("gwd_block_counter")
            if (slam) {
                target.start("gwd_block_counter", random.nextInt(5) + 6)
                target.levels.drain(Skill.Prayer, multiplier = 0.5)
                target.message("K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.")
                npc.say("YARRRRRRR!")
//                areaSound("3274", npc.tile, delay = 15, repeat = 2) // TODO check
                npc.hit(target, offensiveType = "damage", damage = 350 + (random.nextInt(15) * 10)) // TODO prayer mod?
            } else {
                npc.hit(target, offensiveType = "melee")
            }
        }
    }
}

npcCombatAttack("kril_tsutsaroth") { npc ->
    if (type == "magic") {
        if (damage > 0) {
            areaSound("kril_tsutsaroth_magic_impact", target.tile)
        } else {
            // TODO splash
        }
    }
}