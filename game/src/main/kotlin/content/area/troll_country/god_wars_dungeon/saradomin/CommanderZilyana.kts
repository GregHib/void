package content.area.troll_country.god_wars_dungeon.saradomin

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.gfx.areaGfx
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random

val players: Players by inject()
val areas: AreaDefinitions by inject()

npcCombatSwing("commander_zilyana") { npc ->
    when (random.nextInt(2)) {
        0 -> { // Magic
            npc.anim("6967")
            areaSound("3834", target.tile)
            val targets = players.filter { it.tile in areas["saradomin_chamber"] }
            for (target in targets) {
                val hit = npc.hit(target, offensiveType = "magic")
                if (hit > 0) {
                    target.gfx("1207")
                }
            }
        }
        else -> { // Melee
            target.sound("2503")
        }
    }
}

npcCombatAttack("commander_zilyana") {
    if (type == "magic") {
        if (damage > 0) {
            areaSound("3853", target.tile)
            target.gfx("1194")
        } else {
            areaGfx("85", target.tile.addY(1), height = 100)
            areaSound("227", target.tile)
            areaSound("227", target.tile, 20)
            areaGfx("85", target.tile.addY(-1), 20, 100)
        }
    }
}