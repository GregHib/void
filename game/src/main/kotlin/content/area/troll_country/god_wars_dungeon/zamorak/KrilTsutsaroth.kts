package content.area.troll_country.god_wars_dungeon.zamorak

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.effect.toxin.poison
import content.entity.gfx.areaGfx
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
            target.sound("3888", delay = 30)
            npc.anim("6947")
            npc.gfx("1210")
            npc.hit(target, offensiveType = "magic", delay = 1)
        }
        else -> { // Melee
            npc.anim("6945")
            target.sound("3833")
            if (random.nextInt(4) == 0) { // TODO check if random is same or dif
                npc.poison(target, 80)
            }
            val slam = target is Player && random.nextInt(3) != 0 && target.protectMelee() && !target.hasClock("gwd_block_counter")
            if (slam) {
                target.start("gwd_block_counter", random.nextInt(5) + 6)
                target.levels.drain(Skill.Prayer, multiplier = 0.5)
                target.message("K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.")
                npc.say("YARRRRRRR!")
                areaSound("3274", npc.tile, delay = 15, repeat = 2) // TODO check
                npc.hit(target, offensiveType = "damage", damage = 350 + (random.nextInt(15) * 10)) // TODO prayer mod?
            } else {
                // Normal
            }
        }
    }
}

npcCombatAttack("kril_tsutsaroth") { npc ->
    if (type == "magic") {
        if (damage > 0) {
            areaSound("3844", target.tile)
        } else {
            // TODO splash
        }
    }
}