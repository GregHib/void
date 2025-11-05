package content.area.troll_country.god_wars_dungeon.saradomin

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class CommanderZilyana : Script {

    val players: Players by inject()
    val areas: AreaDefinitions by inject()
    val npcs: NPCs by inject()

    var starlight: NPC? = null
    var bree: NPC? = null
    var growler: NPC? = null

    init {
        npcSpawn("commander_zilyana") {
            if (starlight == null) {
                starlight = npcs.add("starlight", Tile(2903, 5260))
            }
            if (bree == null) {
                bree = npcs.add("bree", Tile(2902, 5270))
            }
            if (growler == null) {
                growler = npcs.add("growler", Tile(2898, 5262))
            }
        }

        npcCombatSwing("commander_zilyana") { npc ->
            when (random.nextInt(2)) {
                0 -> { // Magic
                    npc.anim("commander_zilyana_magic")
                    areaSound("commander_zilyana_magic", target.tile, delay = 1)
                    val targets = players.filter { it.tile in areas["saradomin_chamber"] }
                    for (target in targets) {
                        val hit = npc.hit(target, offensiveType = "magic")
                        if (hit > 0) {
                            target.gfx("commander_zilyana_magic_strike")
                        }
                    }
                }
                else -> { // Melee
                    target.sound("commander_zilyana_attack")
                    npc.hit(target, offensiveType = "melee")
                }
            }
        }

        npcDespawn("starlight") {
            starlight = null
        }

        npcDespawn("bree") {
            bree = null
        }

        npcDespawn("growler") {
            growler = null
        }

        npcCombatAttack("commander_zilyana") {
            if (type == "magic") {
                if (damage > 0) {
                    areaSound("commander_zilyana_magic_impact", target.tile)
                    target.gfx("commander_zilyana_magic_impact")
                } else {
                    areaSound("spell_splash", target.tile)
                    areaSound("spell_splash", target.tile, delay = 20)
                    areaGfx("spell_splash", target.tile.addY(1), height = 100)
                    areaGfx("spell_splash", target.tile.addY(-1), delay = 20, height = 100)
                }
            }
        }
    }
}
