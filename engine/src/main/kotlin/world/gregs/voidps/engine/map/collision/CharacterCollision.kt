package world.gregs.voidps.engine.map.collision

import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.NPCMoved
import world.gregs.voidps.engine.entity.character.move.PlayerMoved
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCRegistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.event.priority
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.map.Tile

class CharacterCollision(val collisions: Collisions) {

    init {
        PlayerRegistered priority 9 then {
            add(player)
        }

        NPCRegistered priority 9 then {
            add(npc)
        }

        Unregistered priority 9 where { entity is Character } then {
            remove(entity as Character)
        }

        PlayerMoved priority 9 then {
            move(player, from, to)
        }

        NPCMoved priority 9 then {
            move(npc, from, to)
        }
    }

    fun add(player: Player) {
        collisions.add(player.tile.x, player.tile.y, player.tile.plane, CollisionFlag.ENTITY)
    }
    fun add(npc: NPC) {
        for (x in 0 until npc.size.width) {
            for (y in 0 until npc.size.height) {
                collisions.add(npc.tile.x + x, npc.tile.y + y, npc.tile.plane, CollisionFlag.ENTITY)
            }
        }
    }
    fun remove(character: Character) {
        collisions.remove(character.tile.x, character.tile.y, character.tile.plane, CollisionFlag.ENTITY)
    }

    fun move(character: Character, from: Tile, to: Tile) {
        // No simple way of looking up if an npc is over a tile (incl size)
        // This means players can remove npcs collisions.
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                collisions.remove(from.x + x, from.y + y, from.plane, CollisionFlag.ENTITY)
            }
        }
        for (x in 0 until character.size.width) {
            for (y in 0 until character.size.height) {
                collisions.add(to.x + x, to.y + y, to.plane, CollisionFlag.ENTITY)
            }
        }
    }
}