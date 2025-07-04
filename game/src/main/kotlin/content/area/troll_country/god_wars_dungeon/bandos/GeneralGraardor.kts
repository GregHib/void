package content.area.troll_country.god_wars_dungeon.bandos

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.proj.shoot
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random

val players: Players by inject()
val areas: AreaDefinitions by inject()

npcCombatSwing("general_graardor") { npc ->
    when (random.nextInt(2)) {
        0 -> { // Range
            npc.anim("general_graardor_slam")
            npc.gfx("general_graardor_slam")
            areaSound("general_graardor_slam", target.tile, delay = 20, radius = 7)
            val targets = players.filter { it.tile in areas["bandos_chamber"] } // TODO possible targets
            for (target in targets) {
                val delay = npc.shoot("general_graardor_projectile", target) // TODO params & does shoot if splash?
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
    // TODO check for minions and respawn if necessary
}

npcCombatAttack("general_graardor") {
    if (type == "range") {
        if (damage > 0) {
            target.gfx("general_graardor_smash_impact")
        } else {
            // TODO splash
        }
    }
}