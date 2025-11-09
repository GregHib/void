package world.gregs.voidps.engine.entity.character.npc.hunt

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject

interface Hunt {

    fun huntFloorItem(mode: String, handler: NPC.(FloorItem) -> Unit) {
        floorItems[mode] = handler
    }

    fun huntNPC(mode: String, handler: NPC.(NPC) -> Unit) {
        npcs[mode] = handler
    }

    fun huntPlayer(npc: String = "*", mode: String, handler: NPC.(Player) -> Unit) {
        players.getOrPut("$mode:$npc") { mutableListOf() }.add(handler)
    }

    fun huntObject(mode: String, handler: NPC.(GameObject) -> Unit) {
        objects[mode] = handler
    }

    companion object : AutoCloseable {
        private val floorItems = Object2ObjectOpenHashMap<String, (NPC, FloorItem) -> Unit>()
        private val players = Object2ObjectOpenHashMap<String, MutableList<(NPC, Player) -> Unit>>()
        private val objects = Object2ObjectOpenHashMap<String, (NPC, GameObject) -> Unit>()
        private val npcs = Object2ObjectOpenHashMap<String, (NPC, NPC) -> Unit>()

        fun hunt(npc: NPC, target: NPC, mode: String) {
            npcs[mode]?.invoke(npc, target)
        }

        fun hunt(npc: NPC, target: GameObject, mode: String) {
            objects[mode]?.invoke(npc, target)
        }

        fun hunt(npc: NPC, target: Player, mode: String) {
            for (handler in players["$mode:${npc.id}"] ?: emptyList()) {
                handler(npc, target)
            }
            for (handler in players["$mode:*"] ?: return) {
                handler(npc, target)
            }
        }

        fun hunt(npc: NPC, target: FloorItem, mode: String) {
            floorItems[mode]?.invoke(npc, target)
        }

        override fun close() {
            floorItems.clear()
            players.clear()
            objects.clear()
            npcs.clear()
        }
    }
}