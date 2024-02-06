package world.gregs.voidps.world.interact.entity

import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.characterDespawn
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile

val collisions: Collisions by inject()
val npcs: NPCs by inject()
val players: Players by inject()
val active = getProperty("characterCollision") == "true"

playerSpawn { player: Player ->
    if (players.add(player) && active) {
        collisions.add(player)
    }
}

npcSpawn { npc: NPC ->
    if (!active) {
        return@npcSpawn
    }
    val mask = entity(npc)
    for (x in npc.tile.x until npc.tile.x + npc.def.size) {
        for (y in npc.tile.y until npc.tile.y + npc.def.size) {
            collisions.add(x, y, npc.tile.level, mask)
        }
    }
}

characterDespawn { character: Character ->
    if (active) {
        collisions.remove(character)
    }
}

move({ active }) { character: Character ->
    collisions.move(character, from, to)
}

move { npc: NPC ->
    npcs.update(from, to, npc)
}

fun Collisions.add(char: Character) {
    val mask = entity(char)
    for (x in 0 until char.size) {
        for (y in 0 until char.size) {
            add(char.tile.x + x, char.tile.y + y, char.tile.level, mask)
        }
    }
}

fun Collisions.remove(char: Character) {
    val mask = entity(char)
    for (x in 0 until char.size) {
        for (y in 0 until char.size) {
            remove(char.tile.x + x, char.tile.y + y, char.tile.level, mask)
        }
    }
}

fun Collisions.move(character: Character, from: Tile, to: Tile) {
    val mask = entity(character)
    for (x in 0 until character.size) {
        for (y in 0 until character.size) {
            remove(from.x + x, from.y + y, from.level, mask)
        }
    }
    for (x in 0 until character.size) {
        for (y in 0 until character.size) {
            add(to.x + x, to.y + y, to.level, mask)
        }
    }
}

fun entity(character: Character): Int = if (character is Player) CollisionFlag.BLOCK_PLAYERS else (CollisionFlag.BLOCK_NPCS or if (character["solid", false]) CollisionFlag.FLOOR else 0)
