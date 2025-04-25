package content.entity

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.mode.move.npcMove
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile
import content.entity.combat.dead
import content.entity.death.npcDeath
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.network.client.instruction.Walk

val collisions: Collisions by inject()
val npcs: NPCs by inject()
val players: Players by inject()

instruction<Walk> { player ->
    if (player.contains("delay")) {
        return@instruction
    }
    player.closeInterfaces()
    player.clearWatch()
    player.queue.clearWeak()
    player.suspension = null
    if (minimap && !player["a_world_in_microcosm_task", false]) {
        player["a_world_in_microcosm_task"] = true
    }
    player.walkTo(player.tile.copy(x, y))
}

playerSpawn { player ->
    if (players.add(player) && Settings["world.players.collision", false]) {
        add(player)
    }
}

move {
    if (Settings["world.players.collision", false]) {
        move(character, from, to)
    }
}

playerDespawn { player ->
    if (Settings["world.players.collision", false]) {
        remove(player)
    }
}

npcSpawn { npc ->
    if (Settings["world.npcs.collision", false]) {
        add(npc)
    }
}

npcMove {
    if (Settings["world.npcs.collision", false] && !character.dead) {
        move(character, from, to)
    }
    npcs.update(from, to, npc)
}

npcDeath { npc ->
    remove(npc)
}

npcDespawn { npc ->
    if (Settings["world.npcs.collision", false]) {
        remove(npc)
    }
}

fun add(char: Character) {
    val mask = entity(char)
    val size = char.size
    for (x in char.tile.x until char.tile.x + size) {
        for (y in char.tile.y until char.tile.y + size) {
            collisions.add(x, y, char.tile.level, mask)
        }
    }
}

fun remove(char: Character) {
    val mask = entity(char)
    val size = char.size
    for (x in 0 until size) {
        for (y in 0 until size) {
            collisions.remove(char.tile.x + x, char.tile.y + y, char.tile.level, mask)
        }
    }
}

fun move(character: Character, from: Tile, to: Tile) {
    val mask = entity(character)
    val size = character.size
    for (x in 0 until size) {
        for (y in 0 until size) {
            collisions.remove(from.x + x, from.y + y, from.level, mask)
        }
    }
    for (x in 0 until size) {
        for (y in 0 until size) {
            collisions.add(to.x + x, to.y + y, to.level, mask)
        }
    }
}

fun entity(character: Character): Int = if (character is Player) CollisionFlag.BLOCK_PLAYERS else (CollisionFlag.BLOCK_NPCS or if (character["solid", false]) CollisionFlag.FLOOR else 0)
