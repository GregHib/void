package content.area.troll_country.god_wars_dungeon.zamorak

import content.entity.combat.hit.hit
import content.entity.effect.toxin.poison
import content.skill.prayer.protectMelee
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KrilTsutsaroth : Script {

    val npcs: NPCs by inject()

    var kreeyath: NPC? = null
    var karlak: NPC? = null
    var gritch: NPC? = null

    init {
        npcSpawn("kril_tsutsaroth") {
            if (kreeyath == null) {
                kreeyath = npcs.add("balfrug_kreeyath", Tile(2921, 5319, 2))
            }
            if (karlak == null) {
                karlak = npcs.add("tstanon_karlak", Tile(2932, 5328, 2))
            }
            if (gritch == null) {
                gritch = npcs.add("zakln_gritch", Tile(2919, 5327, 2))
            }
        }

        npcCombatSwing("kril_tsutsaroth") { target ->
            when (random.nextInt(3)) {
                0 -> { // Magic
                    target.sound("kril_tsutsaroth_magic", delay = 30)
                    anim("kril_tsutsaroth_magic_attack")
                    gfx("kril_tsutsaroth_magic_attack")
                    //            shoot("1211", target)
                    hit(target, offensiveType = "magic", delay = 1)
                }
                else -> { // Melee
                    anim("kril_tsutsaroth_attack")
                    target.sound("kril_tsutsaroth_attack")
                    if (random.nextInt(4) == 0) {
                        poison(target, 80)
                    }
                    val slam = target is Player && random.nextInt(3) != 0 && target.protectMelee() && !target.hasClock("gwd_block_counter")
                    if (slam) {
                        target.start("gwd_block_counter", random.nextInt(5) + 6)
                        target.levels.drain(Skill.Prayer, multiplier = 0.5)
                        target.message("K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.")
                        say("YARRRRRRR!")
                        //                areaSound("3274", tile, radius = 15)
                        hit(target, offensiveType = "damage", damage = 350 + (random.nextInt(15) * 10)) // TODO prayer mod?
                    } else {
                        hit(target, offensiveType = "melee")
                    }
                }
            }
        }

        npcDespawn("balfrug_kreeyath") {
            kreeyath = null
        }

        npcDespawn("tstanon_karlak") {
            karlak = null
        }

        npcDespawn("zakln_gritch") {
            gritch = null
        }

        npcCombatAttack("kril_tsutsaroth") { (target, damage, type) ->
            if (type == "magic") {
                if (damage > 0) {
                    areaSound("kril_tsutsaroth_magic_impact", target.tile, radius = 15)
                } else {
                    target.gfx("giant_splash")
                    target.sound("spell_splash")
                }
            }
        }
    }
}
