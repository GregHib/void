package world.gregs.voidps.engine.client.update.task.viewport

import world.gregs.voidps.engine.client.update.task.ParallelTask
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.inject

class ViewportUpdating : ParallelTask<Player>() {

    override val characters: Players by inject()
    val npcs: NPCs by inject()
    val objects: Objects by inject()
    val items: FloorItems by inject()

    override fun run(character: Player) {
        gatherObjectsAndItems(character.tile, character.viewport)
        update(character.tile, characters, character.viewport.players, LOCAL_PLAYER_CAP, character)
        update(character.tile, npcs, character.viewport.npcs, LOCAL_NPC_CAP, null)
    }

    /**
     * Updates a tracking set quickly, or precisely when local entities exceeds [cap]
     */
    fun <T : Character> update(tile: Tile, list: CharacterList<T>, set: CharacterTrackingSet<T>, cap: Int, self: T?) {
        set.start(self)
        val entityCount = nearbyEntityCount(list, tile)
        if (entityCount >= cap) {
            gatherByTile(tile, list, set, self)
        } else {
            gatherByChunk(tile, list, set, self)
        }
        set.finish()
    }

    /**
     * Updates [set] precisely for when local entities exceeds maximum stopping at [CharacterTrackingSet.maximum]
     */
    fun <T : Character> gatherByTile(tile: Tile, list: CharacterList<T>, set: CharacterTrackingSet<T>, self: T?) {
        for (t in tile.spiral(VIEW_RADIUS)) {
            val entities = list[t]
            if (!set.track(entities, self)) {
                return
            }
        }
    }

    /**
     * Updates [set] quickly by gathering all entities in local chunks stopping at [CharacterTrackingSet.maximum]
     */
    fun <T : Character> gatherByChunk(tile: Tile, list: CharacterList<T>, set: CharacterTrackingSet<T>, self: T?) {
        val x = tile.x
        val y = tile.y
        for (chunk in tile.chunk.spiral(2)) {
            val entities = list[chunk]
            if (!set.track(entities, self, x, y)) {
                return
            }
        }
    }

    /**
     * Total entities within radius of two chunks
     */
    fun nearbyEntityCount(list: CharacterList<*>, tile: Tile): Int {
        return list.count(tile.chunk)
    }

    fun gatherObjectsAndItems(tile: Tile, viewport: Viewport) {
        viewport.objects.clear()
        viewport.items.clear()
        for (chunk in tile.chunk.spiral(2)) {
            viewport.objects.addAll(objects[chunk])
            viewport.items.addAll(items[chunk])
        }
    }

    companion object {
        const val PLAYER_TICK_CAP = 40
        const val NPC_TICK_CAP = 40
        const val LOCAL_PLAYER_CAP = 255
        const val LOCAL_NPC_CAP = 255

        // View radius could be controlled per tracking set to give a nicer linear
        // expanding square when loading areas with more than max entities
        const val VIEW_RADIUS = 15
    }
}