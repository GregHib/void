package world.gregs.voidps.world.interact.entity

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.mode.move.npcMove
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.characterDespawn
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.entity.combat.dead
import world.gregs.voidps.world.interact.entity.death.npcDeath

val collisions: Collisions by inject()
val npcs: NPCs by inject()
val players: Players by inject()

playerSpawn { player ->
    if (players.add(player) && Settings["characterCollision", false]) {
        add(player)
    }
}

npcSpawn { npc ->
    if (Settings["characterCollision", false]) {
        add(npc)
    }
}

npcDeath { npc ->
    remove(npc)
}

characterDespawn { character ->
    if (Settings["characterCollision", false]) {
        remove(character)
    }
}

move {
    if (Settings["characterCollision", false]) {
        move(character, from, to)
    }
}

npcMove {
    if (Settings["characterCollision", false] && !character.dead) {
        move(character, from, to)
    }
    npcs.update(from, to, npc)
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
