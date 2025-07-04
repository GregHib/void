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
            npc.anim("kree_arra_attack")
            areaSound("kree_arra_attack", npc.tile)
        }
    }
    else { // Melee
        npc.anim("kree_arra_melee")
        target.sound("kree_arra_melee")
        npc.hit(target, offensiveType = "melee", defensiveType = "magic")
    }
}

npcCombatAttack("kree_arra") {
    if (type != "melee") {
        areaSound("kree_arra_impact", target.tile)
    }
}