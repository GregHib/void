package world.gregs.voidps.world.interact.entity

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions

val collisions: Collisions by inject()
val npcs: NPCs by inject()
val players: Players by inject()
val active = getProperty("characterCollision") == "true"

on<Registered> { character: Character ->
    if (active) {
        collisions.add(character)
    }
    if (character is Player) {
        players.add(character)
    }
}

on<Unregistered> { character: Character ->
    if (active) {
        collisions.remove(character)
    }
    if (character is Player) {
        players.remove(character)
    }
}

on<Moved>({ active }) { character: Character ->
    collisions.move(character, from, to)
}

on<Moved> { player: Player ->
    players.update(from, to, player)
}

on<Moved> { npc: NPC ->
    npcs.update(from, to, npc)
}

fun Collisions.add(char: Character) {
    for (x in 0 until char.size) {
        for (y in 0 until char.size) {
            add(char.tile.x + x, char.tile.y + y, char.tile.level, entity(char))
        }
    }
}

fun Collisions.remove(char: Character) {
    for (x in 0 until char.size) {
        for (y in 0 until char.size) {
            remove(char.tile.x + x, char.tile.y + y, char.tile.level, entity(char))
        }
    }
}

fun Collisions.move(character: Character, from: Tile, to: Tile) {
    for (x in 0 until character.size) {
        for (y in 0 until character.size) {
            remove(from.x + x, from.y + y, from.level, entity(character))
        }
    }
    for (x in 0 until character.size) {
        for (y in 0 until character.size) {
            add(to.x + x, to.y + y, to.level, entity(character))
        }
    }
}

fun entity(character: Character): Int = if (character is Player) CollisionFlag.BLOCK_PLAYERS else (CollisionFlag.BLOCK_NPCS or if (character["solid", false]) CollisionFlag.FLOOR else 0)
