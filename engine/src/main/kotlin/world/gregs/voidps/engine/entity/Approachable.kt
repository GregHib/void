package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.mode.interact.arriveDelay
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Wildcards

/**
 * Target Entity interaction whilst approaching from any distance
 * See `character.approachRange()` for setting the required distance
 */
interface Approachable {

    /**
     * NPC Dialogue helper
     */
    fun talkWithApproach(npc: String, block: suspend Dialogue.() -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("Talk-to:$id") { mutableListOf() }.add { player, target ->
                player.talkWith(target) { block(this) }
            }
        }
    }

    /**
     * Player option
     */
    fun playerApproach(option: String, block: suspend (player: Player, target: Player) -> Unit) {
        for (opt in Wildcards.find(option)) {
            playerPlayerBlocks.getOrPut(opt) { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on Player
     */
    fun interfaceOnPlayerApproach(id: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(id)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    /**
     * Item on Player
     */
    fun itemOnPlayerApproach(item: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(item)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }


    /**
     * Npc option
     */
    fun npcApproach(option: String, npc: String, block: suspend (Player, NPC) -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on NPC
     * Any [npc] is allowed but [id] is required
     */
    fun interfaceOnNpcApproach(id: String, npc: String = "*", block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Item on NPC
     * Any item is allowed, [npc] is required
     */
    fun itemOnNpcApproach(item: String = "*", npc: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    /**
     * GameObject option
     */
    fun objectApproach(option: String, obj: String, arriveDelay: Boolean = true, block: suspend (Player, GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (id in Wildcards.find(obj)) {
            playerObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on GameObject
     * Any [obj] is allowed but [id] is required
     */
    fun interfaceOnObjectApproach(id: String, obj: String = "*", arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(obj)) {
                onObjectBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Item on GameObject
     * Any item is allowed, [obj] is required
     */
    fun itemOnObjectApproach(item: String = "*", obj: String, arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(obj)) {
                onObjectBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    /**
     * FloorItem option
     */
    fun floorItemApproach(option: String, item: String = "*", arriveDelay: Boolean = true, block: suspend (Player, FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (id in Wildcards.find(item)) {
            playerFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on FloorItem
     * Any [item] is allowed but [id] is required
     */
    fun interfaceOnFloorItemApproach(id: String, item: String = "*", arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(item)) {
                onFloorItemBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Item on FloorItem
     * Any item is allowed, [floorItem] is required
     */
    fun itemOnFloorItemApproach(item: String = "*", floorItem: String, arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(floorItem)) {
                onFloorItemBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    /**
     * Npc player option
     */
    suspend fun npcApproachPlayer(option: String, block: suspend (npc: NPC, target: Player) -> Unit) {
        npcPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    /**
     * Npc npc option
     */
    suspend fun npcApproachNpc(option: String, npc: String, block: suspend (npc: NPC, target: NPC) -> Unit) {
        for (id in Wildcards.find(npc)) {
            npcNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Npc game object option
     */
    suspend fun npcApproachObject(option: String, obj: String, arriveDelay: Boolean = true, block: suspend (npc: NPC, target: GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (id in Wildcards.find(obj)) {
            npcObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Npc floor item option
     */
    suspend fun npcApproachFloorItem(option: String, item: String, arriveDelay: Boolean = true, block: suspend (npc: NPC, target: FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (id in Wildcards.find(item)) {
            npcFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    companion object {
        val playerPlayerBlocks = mutableMapOf<String, MutableList<suspend (Player, Player) -> Unit>>()
        val onPlayerBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, Player) -> Unit>>()

        val playerNpcBlocks = mutableMapOf<String, MutableList<suspend (Player, NPC) -> Unit>>()
        val onNpcBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, NPC) -> Unit>>()

        val playerObjectBlocks = mutableMapOf<String, MutableList<suspend (Player, GameObject) -> Unit>>()
        val onObjectBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, GameObject) -> Unit>>()

        val playerFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, FloorItem) -> Unit>>()
        val onFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, FloorItem) -> Unit>>()

        val npcPlayerBlocks = mutableMapOf<String, MutableList<suspend (NPC, Player) -> Unit>>()
        val npcNpcBlocks = mutableMapOf<String, MutableList<suspend (NPC, NPC) -> Unit>>()
        val npcObjectBlocks = mutableMapOf<String, MutableList<suspend (NPC, GameObject) -> Unit>>()
        val npcFloorItemBlocks = mutableMapOf<String, MutableList<suspend (NPC, FloorItem) -> Unit>>()

        // Don't call arriveDelay before an object or floor item interaction
        private val noDelays = mutableSetOf<String>()


        suspend fun approach(player: Player, target: Player, option: String) {
            for (block in playerPlayerBlocks[option] ?: return) {
                block(player, target)
            }
        }

        suspend fun approach(player: Player, target: NPC, option: String) {
            for (block in playerNpcBlocks["$option:${target.def(player).stringId}"] ?: emptyList()) {
                block(player, target)
            }
            for (block in playerNpcBlocks[option] ?: return) {
                block(player, target)
            }
        }

        suspend fun approach(player: Player, target: GameObject, option: String) {
            if (!noDelays.contains(target.id)) {
                player.arriveDelay()
            }
            for (block in playerObjectBlocks["$option:${target.def(player).stringId}"] ?: emptyList()) {
                block(player, target)
            }
            for (block in playerObjectBlocks[option] ?: return) {
                block(player, target)
            }
        }

        suspend fun approach(player: Player, target: FloorItem, option: String) {
            if (!noDelays.contains(target.id)) {
                player.arriveDelay()
            }
            for (block in playerFloorItemBlocks["$option:${target.id}"] ?: emptyList()) {
                block(player, target)
            }
            for (block in playerFloorItemBlocks[option] ?: return) {
                block(player, target)
            }
        }

        suspend fun approach(player: Player, id: String, slot: Int, item: Item, target: Player) {
            for (block in onPlayerBlocks[if (item.isEmpty()) id else item.id] ?: return) {
                block(player, id, slot, item, target)
            }
        }

        suspend fun approach(player: Player, id: String, item: Item, slot: Int, target: NPC) {
            if (item.isEmpty()) {
                for (block in onNpcBlocks["$id:${target.def(player).stringId}"] ?: emptyList()) {
                    block(player, id, slot, item, target)
                }
                for (block in onNpcBlocks["$id:*"] ?: return) {
                    block(player, id, slot, item, target)
                }
            } else {
                for (block in onNpcBlocks["${item.id}:${target.def(player).stringId}"] ?: emptyList()) {
                    block(player, id, slot, item, target)
                }
                for (block in onNpcBlocks["${item.id}:*"] ?: return) {
                    block(player, id, slot, item, target)
                }
            }
        }

        suspend fun approach(player: Player, id: String, item: Item, slot: Int, target: GameObject) {
            if (!noDelays.contains(target.id)) {
                player.arriveDelay()
            }
            if (item.isEmpty()) {
                for (block in onObjectBlocks["$id:${target.def(player).stringId}"] ?: emptyList()) {
                    block(player, id, slot, item, target)
                }
                for (block in onObjectBlocks["$id:*"] ?: return) {
                    block(player, id, slot, item, target)
                }
            } else {
                for (block in onObjectBlocks["${item.id}:${target.def(player).stringId}"] ?: emptyList()) {
                    block(player, id, slot, item, target)
                }
                for (block in onObjectBlocks["${item.id}:*"] ?: return) {
                    block(player, id, slot, item, target)
                }
            }
        }

        suspend fun approach(player: Player, id: String, item: Item, slot: Int, target: FloorItem) {
            if (!noDelays.contains(target.id)) {
                player.arriveDelay()
            }
            if (item.isEmpty()) {
                for (block in onFloorItemBlocks["$id:${target.id}"] ?: emptyList()) {
                    block(player, id, slot, item, target)
                }
                for (block in onFloorItemBlocks["$id:*"] ?: return) {
                    block(player, id, slot, item, target)
                }
            } else {
                for (block in onFloorItemBlocks["${item.id}:${target.id}"] ?: emptyList()) {
                    block(player, id, slot, item, target)
                }
                for (block in onFloorItemBlocks["${item.id}:*"] ?: return) {
                    block(player, id, slot, item, target)
                }
            }
        }

        suspend fun approach(npc: NPC, target: Player, option: String) {
            for (block in npcPlayerBlocks[option] ?: return) {
                block(npc, target)
            }
        }

        suspend fun approach(npc: NPC, target: NPC, option: String) {
            for (block in npcNpcBlocks["$option:${target.id}"] ?: return) {
                block(npc, target)
            }
        }

        suspend fun approach(npc: NPC, target: GameObject, option: String) {
            if (!noDelays.contains(target.id)) {
                npc.arriveDelay()
            }
            for (block in npcObjectBlocks["$option:${target.id}"] ?: return) {
                block(npc, target)
            }
        }

        suspend fun approach(npc: NPC, target: FloorItem, option: String) {
            if (!noDelays.contains(target.id)) {
                npc.arriveDelay()
            }
            for (block in npcFloorItemBlocks["$option:${target.id}"] ?: return) {
                block(npc, target)
            }
        }

        fun clear() {
            playerPlayerBlocks.clear()
            onPlayerBlocks.clear()
            playerNpcBlocks.clear()
            onNpcBlocks.clear()
            playerObjectBlocks.clear()
            onObjectBlocks.clear()
            playerFloorItemBlocks.clear()
            onFloorItemBlocks.clear()
            npcPlayerBlocks.clear()
            npcNpcBlocks.clear()
            npcObjectBlocks.clear()
            npcFloorItemBlocks.clear()
            noDelays.clear()
        }
    }
}