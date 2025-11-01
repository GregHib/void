package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.Operation.Companion
import world.gregs.voidps.engine.entity.character.mode.interact.*
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


    /*
        Player approaches
     */

    fun playerApproach(option: String, block: suspend Player.(PlayerPlayerInteract) -> Unit) {
        playerPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    fun npcApproach(option: String, npc: String = "*", block: suspend Player.(PlayerNPCInteract) -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun objectApproach(option: String, obj: String = "*", block: suspend Player.(PlayerObjectInteract) -> Unit) {
        for (id in Wildcards.find(obj)) {
            playerObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun floorItemApproach(option: String, item: String, block: suspend Player.(PlayerFloorItemInteract) -> Unit) {
        for (id in Wildcards.find(item)) {
            playerFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }


    /*
        NPC approaches
     */

    fun npcApproachPlayer(option: String, block: suspend NPC.(NPCPlayerInteract) -> Unit) {
        npcPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    fun npcApproachNPC(option: String, npc: String = "*", block: suspend NPC.(NPCNPCInteract) -> Unit) {
        for (id in Wildcards.find(npc)) {
            npcNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun npcApproachObject(option: String, obj: String = "*", block: suspend NPC.(NPCObjectInteract) -> Unit) {
        for (id in Wildcards.find(obj)) {
            npcObjectBlocks.getOrPut(option) { mutableListOf() }.add(block)
        }
    }

    fun npcApproachFloorItem(option: String, item: String, block: suspend NPC.(NPCFloorItemInteract) -> Unit) {
        for (id in Wildcards.find(item)) {
            npcFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

/*
    *//**
     * NPC Dialogue helper
     *//*
    fun talkWithApproach(npc: String, block: suspend Dialogue.() -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("Talk-to:$id") { mutableListOf() }.add { player, target ->
                player.talkWith(target) { block(this) }
            }
        }
    }

    *//**
     * Interface on Player
     *//*
    fun interfaceOnPlayerApproach(id: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(id)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    *//**
     * Item on Player
     *//*
    fun itemOnPlayerApproach(item: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(item)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }


    *//**
     * Npc option
     *//*
    fun npcApproach(option: String, npc: String, block: suspend (Player, NPC) -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    *//**
     * Interface on NPC
     * Any [npc] is allowed but [id] is required
     *//*
    fun interfaceOnNpcApproach(id: String, npc: String = "*", block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    *//**
     * Item on NPC
     * Any item is allowed, [npc] is required
     *//*
    fun itemOnNpcApproach(item: String = "*", npc: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    *//**
     * GameObject option
     *//*
    fun objectApproach(option: String, obj: String, arriveDelay: Boolean = true, block: suspend (Player, GameObject) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(obj))
        }
        for (id in Wildcards.find(obj)) {
            playerObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    *//**
     * Interface on GameObject
     * Any [obj] is allowed but [id] is required
     *//*
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

    *//**
     * Item on GameObject
     * Any item is allowed, [obj] is required
     *//*
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


    *//**
     * FloorItem option
     *//*
    fun floorItemApproach(option: String, item: String = "*", arriveDelay: Boolean = true, block: suspend (Player, FloorItem) -> Unit) {
        if (!arriveDelay) {
            noDelays.addAll(Wildcards.find(item))
        }
        for (id in Wildcards.find(item)) {
            playerFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    *//**
     * Interface on FloorItem
     * Any [item] is allowed but [id] is required
     *//*
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

    *//**
     * Item on FloorItem
     * Any item is allowed, [floorItem] is required
     *//*
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

    */

    companion object {
        val playerPlayerBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerPlayerInteract) -> Unit>>()
        val onPlayerBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, Player) -> Unit>>()

        val playerNpcBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerNPCInteract) -> Unit>>()
        val onNpcBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, NPC) -> Unit>>()

        val playerObjectBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerObjectInteract) -> Unit>>()
        val onObjectBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, GameObject) -> Unit>>()

        val playerFloorItemBlocks = mutableMapOf<String, MutableList<suspend Player.(PlayerFloorItemInteract) -> Unit>>()
        val onFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, FloorItem) -> Unit>>()

        val npcPlayerBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCPlayerInteract) -> Unit>>()
        val npcNpcBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCNPCInteract) -> Unit>>()
        val npcObjectBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCObjectInteract) -> Unit>>()
        val npcFloorItemBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCFloorItemInteract) -> Unit>>()

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
        }
    }
}