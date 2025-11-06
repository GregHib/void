package content.area.troll_country.god_wars_dungeon.armadyl

import content.entity.combat.attackers
import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KreeArra : Script {

    val players: Players by inject()
    val areas: AreaDefinitions by inject()
    val npcs: NPCs by inject()

    var kilisa: NPC? = null
    var skree: NPC? = null
    var geerin: NPC? = null

    init {
        npcSpawn("kree_arra") {
            if (kilisa == null) {
                kilisa = npcs.add("flight_kilisa", Tile(2833, 5297, 2))
            }
            if (skree == null) {
                skree = npcs.add("wingman_skree", Tile(2840, 5303, 2))
            }
            if (geerin == null) {
                geerin = npcs.add("flockleader_geerin", Tile(2828, 5299, 2))
            }
        }

        npcCombatSwing("kree_arra") { target ->
            if (attackers.isEmpty() && random.nextInt(2) == 0) { // Enrage
                val targets = players.filter { it.tile in areas["armadyl_chamber"] }
                for (t in targets) {
                    if (random.nextBoolean()) {
                        hit(t, offensiveType = "magic", defensiveType = "range")
                    } else {
                        hit(t, offensiveType = "range")
                    }
                    // Teleport
                    if (random.nextInt(5) == 0) {
                        var attempts = 0
                        while (attempts++ < 20) {
                            val tile = t.tile.toCuboid(2).random(t) ?: continue
                            if (tile in tile.toCuboid(size, size)) {
                                continue
                            }
                            t.tele(tile)
                            t.gfx("kree_arra_stun", delay = 100)
                            t.anim("kree_arra_stun")
                            t.sound("kree_arra_stun")
                            break
                        }
                    }
                    shoot("kree_arra_tornado_blue", t.tile)
                    anim("kree_arra_attack")
                    areaSound("kree_arra_attack", tile, delay = 1)
                }
            } else { // Melee
                anim("kree_arra_melee")
                target.sound("kree_arra_melee")
                shoot("kree_arra_tornado_white", target.tile)
                hit(target, offensiveType = "melee", defensiveType = "magic")
            }
        }

        npcDespawn("flight_kilisa") {
            kilisa = null
        }

        npcDespawn("wingman_skree") {
            skree = null
        }

        npcDespawn("flockleader_geerin") {
            geerin = null
        }

        npcCombatAttack("kree_arra") {
            if (type != "melee") {
                areaSound("kree_arra_impact", target.tile)
            }
        }
    }
}
