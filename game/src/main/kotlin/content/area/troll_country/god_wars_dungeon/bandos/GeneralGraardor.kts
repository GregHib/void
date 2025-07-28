package content.area.troll_country.god_wars_dungeon.bandos

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.npcDespawn
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

val players: Players by inject()
val areas: AreaDefinitions by inject()
val npcs: NPCs by inject()

var strongstack: NPC? = null
var steelwill: NPC? = null
var grimspike: NPC? = null

npcCombatSwing("general_graardor") { npc ->
    when (random.nextInt(2)) {
        0 -> { // Range
            npc.anim("general_graardor_slam")
            npc.gfx("general_graardor_slam")
            areaSound("general_graardor_slam", target.tile, delay = 20, radius = 7)
            val targets = players.filter { it.tile in areas["bandos_chamber"] }
            for (target in targets) {
                val delay = npc.shoot("general_graardor_projectile", target, curve = random.nextInt(9, 24))
                npc.hit(target, offensiveType = "range", delay = delay)
            }
        }
        else -> { // Melee
            target.sound("general_graardor_attack")
            target.sound("general_graardor_attack", delay = 20)
            npc.anim("general_graardor_attack")
            npc.hit(target, offensiveType = "melee")
        }
    }
}

npcSpawn("general_graardor") {
    if (strongstack == null) {
        strongstack = npcs.add("sergeant_strongstack", Tile(2866, 5358, 2))
    }
    if (steelwill == null) {
        steelwill = npcs.add("sergeant_steelwill", Tile(2872, 5352, 2))
    }
    if (grimspike == null) {
        grimspike = npcs.add("sergeant_grimspike", Tile(2868, 5362, 2))
    }
}

npcDespawn("sergeant_*") { npc ->
    when (npc.id) {
        "sergeant_strongstack" -> strongstack = null
        "sergeant_steelwill" -> steelwill = null
        "sergeant_grimspike" -> grimspike = null
    }
}

npcCombatAttack("general_graardor") {
    if (type == "range") {
        if (damage > 0) {
            target.gfx("general_graardor_smash_impact")
        } else {
            target.gfx("giant_splash")
            target.sound("spell_splash")
        }
    }
}

npcCombatAttack("sergeant_steelwill") {
    if (type == "magic" && damage > 0) {
        areaGfx("sergeant_steelwill_impact", target.tile)
        target.sound("sergeant_steelwill_impact")
    }
}
