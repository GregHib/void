package content.area.troll_country.god_wars_dungeon.armadyl

import content.entity.combat.attackers
import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.sound.areaSound
import content.entity.sound.sound
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.type.random

val players: Players by inject()
val areas: AreaDefinitions by inject()

npcCombatSwing("kree_arra") { npc ->
    if (npc.attackers.isEmpty() && random.nextInt(2) == 0) { // Enrage
        val targets = players.filter { it.tile in areas["armadyl_chamber"] }
        for (target in targets) {
            if (random.nextBoolean()) {
                npc.hit(target, type = "magic", mark = HitSplat.Mark.Range)
            } else {
                npc.hit(target, type = "range")
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
                    target.gfx("981", delay = 100)
                    target.anim("848")
                    target.sound("3201")
                    break
                }
            }
            npc.anim("6976")
            areaSound("3871", npc.tile)
        }
    }
    else { // Melee
        npc.anim("6997")
        target.sound("3837")
        npc.hit(target, type = "melee", mark = HitSplat.Mark.Magic)
    }
}

npcCombatAttack("kree_arra") { npc ->
    if (type != "melee") {
        areaSound("3874", target.tile)
    }
}