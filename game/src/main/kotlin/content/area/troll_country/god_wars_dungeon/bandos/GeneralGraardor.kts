package content.area.troll_country.god_wars_dungeon.bandos

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.proj.shoot
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random

val players: Players by inject()
val areas: AreaDefinitions by inject()

npcCombatSwing("general_graardor") { npc ->
    when (random.nextInt(2)) {
        0 -> { // Range
            npc.anim("7063")
            npc.gfx("1219")// height = 30
            areaSound("3834", target.tile)
            val targets = players.filter { it.tile in areas["bandos_chamber"] } // TODO possible targets
            for (target in targets) {
                val delay = npc.shoot("1200", target) // TODO params & does shoot if splash?
                npc.hit(target, type = "range", delay = delay)
            }
        }
        else -> { // Melee
            target.sound("3860")
            target.sound("3860", delay = 20)
        }
    }
}

npcCombatAttack("general_graardor") {
    if (type == "range") {
        if (damage > 0) {
            target.gfx("359") // height 96
        } else {
            // TODO splash
        }
    }
}