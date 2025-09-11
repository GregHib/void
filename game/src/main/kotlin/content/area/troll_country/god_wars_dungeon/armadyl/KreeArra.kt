package content.area.troll_country.god_wars_dungeon.armadyl

import content.entity.combat.attackers
import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.proj.shoot
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.npcDespawn
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

@Script
class KreeArra {

    val players: Players by inject()
    val areas: AreaDefinitions by inject()
    val npcs: NPCs by inject()

    var kilisa: NPC? = null
    var skree: NPC? = null
    var geerin: NPC? = null

    init {
        npcCombatSwing("kree_arra") { npc ->
            if (npc.attackers.isEmpty() && random.nextInt(2) == 0) { // Enrage
                val targets = players.filter { it.tile in areas["armadyl_chamber"] }
                for (target in targets) {
                    if (random.nextBoolean()) {
                        npc.hit(target, offensiveType = "magic", defensiveType = "range")
                    } else {
                        npc.hit(target, offensiveType = "range")
                    }
                    // Teleport
                    if (random.nextInt(5) == 0) {
                        var attempts = 0
                        while (attempts++ < 20) {
                            val tile = target.tile.toCuboid(2).random(target) ?: continue
                            if (tile in npc.tile.toCuboid(npc.size, npc.size)) {
                                continue
                            }
                            target.tele(tile)
                            target.gfx("kree_arra_stun", delay = 100)
                            target.anim("kree_arra_stun")
                            target.sound("kree_arra_stun")
                            break
                        }
                    }
                    npc.shoot("kree_arra_tornado_blue", target.tile)
                    npc.anim("kree_arra_attack")
                    areaSound("kree_arra_attack", npc.tile, delay = 1)
                }
            } else { // Melee
                npc.anim("kree_arra_melee")
                target.sound("kree_arra_melee")
                npc.shoot("kree_arra_tornado_white", target.tile)
                npc.hit(target, offensiveType = "melee", defensiveType = "magic")
            }
        }

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
