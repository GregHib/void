package content.entity.npc.combat.melee

import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.event.Events
import content.entity.combat.hit.CombatAttack
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import content.entity.sound.sound

private val TELEPORT_RADIUS_MAX = 20
private val TELEPORT_RADIUS_MIN = 1
private val TELEPORT_CHANCE = 0.25
private val TELEPORT_CHANCE_HIT = 0.10
private val TELE_POOF_VFX_RADIUS = 5

fun isBlocked(tile: Tile): Boolean {
    val collisions = get<Collisions>()
    val flags = collisions.get(tile.x, tile.y, tile.level)
    return flags != 0
}

fun randomValidTile(npc: NPC): Tile {
    repeat(5) {
        val dest = npc.tile.toCuboid(TELEPORT_RADIUS_MAX).random()
        if (!isBlocked(dest ) && dest.region == npc.tile.region) {
            if (npc.tile.distanceTo(dest) >= TELEPORT_RADIUS_MIN) {
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
        npc.mode = PauseMode
        npc.steps.clear()
        val players : Players = get()
        players.forEach { player ->
            if (npc.tile.region == player.tile.region) {
                val distance = npc.tile.distanceTo(player.tile)
                if (distance < TELE_POOF_VFX_RADIUS) {
                    player.sound("imp_puff_teleport")
                }
            }
        }
        if (target == null) {
            npc.gfx("imp_puff")
            delay(1)
            npc.tele(destination)
            npc.gfx("imp_puff")
            npc.mode = PauseMode
            delay(1)
            npc.tile = destination
            npc.steps.clear()
        } else {
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
}

npcSpawn("imp") { npc ->
    npc.mode = EmptyMode
    npc.softTimers.start("teleport_timer")
}

npcTimerStart("teleport_timer") {
    interval = random.nextInt(50, 200)
}

npcTimerTick("teleport_timer") { npc ->
    teleportImp(npc, TELEPORT_CHANCE, null)
}

Events.handle<Player, CombatAttack>("player_combat_attack","*","*","*","*") { dispatch ->
    if (this.target is NPC) {
        val npc = this.target as NPC
        if (npc != null) {
            if (npc.id == "imp") {
                teleportImp(npc, TELEPORT_CHANCE_HIT, dispatch)
            }
        }
    }
}