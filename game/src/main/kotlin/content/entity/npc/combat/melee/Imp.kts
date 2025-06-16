package content.entity.npc.combat.melee

import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.entity.character.player.Player
import content.entity.sound.areaSound
import content.entity.combat.hit.npcCombatDamage
import world.gregs.voidps.engine.queue.queue
import content.entity.sound.areaSound

private val teleportRadiusMax = 20
private val teleportRadiusMin = 5
private val teleportChance = 0.25
private val teleportChanceHit = 0.10
private val telePoofVfxRadius = 5

fun isBlocked(tile: Tile): Boolean {
    val collisions = get<Collisions>()
    val flags = collisions.get(tile.x, tile.y, tile.level)
    return flags != 0
}

fun randomValidTile(npc: NPC): Tile {
    repeat(10) {
        val dest = npc.tile.toCuboid(teleportRadiusMax).random()
        if (!isBlocked(dest ) && dest.region == npc.tile.region) {
            if (npc.tile.distanceTo(dest) >= teleportRadiusMin) {
                return dest
            }
        }
    }
    return npc.tile
}

fun teleportImp(npc: NPC, chance: Double, target: Player?) {
    if (npc.queue.contains("death")) {
        return
    }
    if (random.nextDouble() > chance) {
        return
    }

    val destination = randomValidTile(npc)

    npc.softTimers.restart("teleport_timer")

    npc.softQueue("imp_teleport") {
        areaSound("imp_puff_teleport", npc.tile, telePoofVfxRadius)
            npc.gfx("imp_puff")
            delay(1)
            npc.tele(destination)
            npc.gfx("imp_puff")
            npc.mode = PauseMode
            delay(1)
            npc.tile = destination
            npc.steps.clear()
        }
    }

npcSpawn("imp") { npc ->
    npc.softTimers.start("teleport_timer")
}

npcTimerStart("teleport_timer") {
    interval = random.nextInt(50, 200)
}

npcTimerTick("teleport_timer") { npc ->
    teleportImp(npc, teleportChance, null)
}

npcCombatDamage("imp") { npc ->
    teleportImp(npc, teleportChanceHit, null)
}