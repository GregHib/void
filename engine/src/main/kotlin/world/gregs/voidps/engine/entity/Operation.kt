package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.mode.interact.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Wildcards

/**
 * Target Entity interaction within close-proximity
 */
interface Operation {


    /*
        Player operations
     */

    fun playerOperate(option: String, block: suspend Player.(PlayerPlayerInteract) -> Unit) {
        playerPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    fun npcOperate(option: String, npc: String = "*", block: suspend Player.(PlayerNPCInteract) -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun objectOperate(option: String, obj: String = "*", arrive: Boolean = true, block: suspend Player.(PlayerObjectInteract) -> Unit) {
        for (id in Wildcards.find(obj)) {
            if (!arrive) {
                noDelays.add("$option:$id")
            }
            playerObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun floorItemOperate(option: String, arrive: Boolean = true, block: suspend Player.(PlayerFloorItemInteract) -> Unit) {
        if (!arrive) {
            noDelays.add(option)
        }
        playerFloorItemBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    /*
        Interface on
     */

    fun onPlayerOperate(id: String = "*", block: suspend Player.(ItemPlayerInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    fun itemOnPlayerOperate(item: String = "*", block: suspend Player.(ItemPlayerInteract) -> Unit) {
        for (id in Wildcards.find(item)) {
            onPlayerBlocks.getOrPut(id) { mutableListOf() }.add(block)
        }
    }

    fun onNPCOperate(id: String = "*", npc: String = "*", block: suspend Player.(InterfaceNPCInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            for (n in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$i:$n") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnNPCOperate(item: String = "*", npc: String = "*", block: suspend Player.(ItemNPCInteract) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (id in Wildcards.find(npc)) {
                itemOnNpcBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
        }
    }

    fun onObjectOperate(id: String = "*", obj: String = "*", block: suspend Player.(InterfaceObjectInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            for (o in Wildcards.find(obj)) {
                onObjectBlocks.getOrPut("$i:$o") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnObjectOperate(item: String = "*", obj: String = "*", arrive: Boolean = true, block: suspend Player.(ItemObjectInteract) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (id in Wildcards.find(obj)) {
                if (!arrive) {
                    noDelays.add("$itm:$id")
                }
                itemOnObjectBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
        }
    }

    /*
        NPC operations
     */

    fun npcOperatePlayer(option: String, block: suspend NPC.(NPCPlayerInteract) -> Unit) {
        npcPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    fun npcOperateNPC(option: String, npc: String = "*", block: suspend NPC.(NPCNPCInteract) -> Unit) {
        for (id in Wildcards.find(npc)) {
            npcNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun npcOperateObject(option: String, obj: String = "*", arrive: Boolean = true, block: suspend NPC.(NPCObjectInteract) -> Unit) {
        for (id in Wildcards.find(obj)) {
            if (!arrive) {
                noDelays.add("$option:$id")
            }
            npcObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun npcOperateFloorItem(option: String, arrive: Boolean = true, block: suspend NPC.(NPCFloorItemInteract) -> Unit) {
        if (!arrive) {
            noDelays.add(option)
        }
        npcFloorItemBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    /*
        */
    /**
     * NPC Dialogue helper
     *//*
    fun talkWith(npc: String, block: suspend Dialogue.() -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("Talk-to:$id") { mutableListOf() }.add { player, target ->
                player.talkWith(target) { block(this) }
            }
        }
    }

    */
    /**
     * Interface on Player
     *//*
    fun interfaceOnPlayerOperate(id: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(id)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    */
    /**
     * Item on Player
     *//*
    fun itemOnPlayerOperate(item: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(item)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }


    */
    /**
     * Npc option
     *//*
    fun npcOperate(option: String, npc: String, block: suspend (player: Player, target: NPC) -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    */
    /**
     * Interface on NPC
     * Any [npc] is allowed but [id] is required
     *//*
    fun interfaceOnNpcOperate(id: String, npc: String = "*", block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    */
    /**
     * Item on NPC
     * Any item is allowed, [npc] is required
     *//*
    fun itemOnNpcOperate(item: String = "*", npc: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    */
    /**
     * GameObject option
     *//*
    fun objectOperate(option: String, obj: String, arriveDelay: Boolean = true, block: suspend (Player, GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (id in Wildcards.find(obj)) {
            playerObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    */
    /**
     * Interface on GameObject
     * Any [obj] is allowed but [id] is required
     *//*
    fun interfaceOnObjectOperate(id: String, obj: String = "*", arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(obj)) {
                onObjectBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    */
    /**
     * Item on GameObject
     * Any item is allowed, [obj] is required
     *//*
    fun itemOnObjectOperate(item: String = "*", obj: String, arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(obj)) {
                onObjectBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    */
    /**
     * FloorItem option
     *//*
    fun floorItemOperate(option: String, item: String, arriveDelay: Boolean = true, block: suspend (Player, FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (id in Wildcards.find(item)) {
            playerFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    */
    /**
     * Interface on FloorItem
     * Any [item] is allowed but [id] is required
     *//*
    fun interfaceOnFloorItemOperate(id: String, item: String = "*", arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(item)) {
                onFloorItemBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    */
    /**
     * Item on FloorItem
     * Any item is allowed, [floorItem] is required
     *//*
    fun itemOnFloorItemOperate(item: String = "*", floorItem: String, arriveDelay: Boolean = true, block: suspend (player: Player, id: String, slot: Int, item: Item, target: FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(floorItem)) {
                onFloorItemBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }

*/

    companion object {
        val playerPlayerBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerPlayerInteract) -> Unit>>()
        val onPlayerBlocks = mutableMapOf<String, MutableList<suspend Player.(ItemPlayerInteract) -> Unit>>()

        val playerNpcBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerNPCInteract) -> Unit>>()
        val onNpcBlocks = mutableMapOf<String, MutableList<suspend Player.(InterfaceNPCInteract) -> Unit>>()
        val itemOnNpcBlocks = mutableMapOf<String, MutableList<suspend Player.(ItemNPCInteract) -> Unit>>()

        val playerObjectBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerObjectInteract) -> Unit>>()
        val onObjectBlocks = mutableMapOf<String, MutableList<suspend Player.(InterfaceObjectInteract) -> Unit>>()
        val itemOnObjectBlocks = mutableMapOf<String, MutableList<suspend Player.(ItemObjectInteract) -> Unit>>()

        val playerFloorItemBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerFloorItemInteract) -> Unit>>()
        val onFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, FloorItem) -> Unit>>()

        val npcPlayerBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCPlayerInteract) -> Unit>>()
        val npcNpcBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCNPCInteract) -> Unit>>()
        val npcObjectBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCObjectInteract) -> Unit>>()
        val npcFloorItemBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCFloorItemInteract) -> Unit>>()

        // Don't call arriveDelay before an object or floor item interaction
        val noDelays = mutableSetOf<String>()

        fun clear() {
            playerPlayerBlocks.clear()
            onPlayerBlocks.clear()
            playerNpcBlocks.clear()
            onNpcBlocks.clear()
            itemOnNpcBlocks.clear()
            playerObjectBlocks.clear()
            itemOnObjectBlocks.clear()
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