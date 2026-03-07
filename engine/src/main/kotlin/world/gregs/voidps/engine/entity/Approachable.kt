package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.interact.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

/**
 * Target Entity interaction whilst approaching from any distance
 * See `character.approachRange()` for setting the required distance
 */
interface Approachable {


    /*
        Player approaches
     */

    fun playerApproach(option: String, handler: suspend Player.(PlayerOnPlayerInteract) -> Unit) {
        Script.checkLoading()
        playerPlayer.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcApproach(option: String, npc: String = "*", handler: suspend Player.(PlayerOnNPCInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(npc, Wildcard.Npc) { id ->
            playerNpc.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun objectApproach(option: String, obj: String = "*", handler: suspend Player.(PlayerOnObjectInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(obj, Wildcard.Object) { id ->
            playerObject.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun floorItemApproach(option: String, handler: suspend Player.(PlayerOnFloorItemInteract) -> Unit) {
        Script.checkLoading()
        playerFloorItem.getOrPut(option) { mutableListOf() }.add(handler)
    }

    /*
        Interface on
     */

    fun onPlayerApproach(id: String = "*", handler: suspend Player.(ItemOnPlayerInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(id, Wildcard.Component) { i ->
            onPlayer.getOrPut(i) { mutableListOf() }.add(handler)
        }
    }

    fun itemOnPlayerApproach(item: String = "*", block: suspend Player.(ItemOnPlayerInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(item, Wildcard.Item) { id ->
            onPlayer.getOrPut(id) { mutableListOf() }.add(block)
        }
    }

    fun onNPCApproach(id: String = "*", npc: String = "*", handler: suspend Player.(InterfaceOnNPCInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(npc, Wildcard.Npc) { n ->
                onNpc.getOrPut("$i:$n") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnNPCApproach(item: String = "*", npc: String = "*", handler: suspend Player.(ItemOnNPCInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(npc, Wildcard.Npc) { id ->
                itemOnNpc.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    fun onObjectApproach(id: String = "*", obj: String = "*", handler: suspend Player.(InterfaceOnObjectInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(obj, Wildcard.Object) { o ->
                onObject.getOrPut("$i:$o") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnObjectApproach(item: String = "*", obj: String = "*", arrive: Boolean = true, handler: suspend Player.(ItemOnObjectInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(obj, Wildcard.Object) { id ->
                itemOnObject.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    fun onFloorItemApproach(id: String = "*", floorItem: String = "*", handler: suspend Player.(InterfaceOnFloorItemInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(floorItem, Wildcard.Item) { floor ->
                onFloorItem.getOrPut("$i:$floor") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnFloorItemApproach(item: String = "*", floorItem: String = "*", handler: suspend Player.(ItemOnFloorItemInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(floorItem, Wildcard.Item) { id ->
                itemOnFloorItem.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }


    /*
        NPC approaches
     */

    fun npcApproachPlayer(option: String, handler: suspend NPC.(NPCOnPlayerInteract) -> Unit) {
        Script.checkLoading()
        npcPlayer.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcApproachNPC(option: String, handler: suspend NPC.(NPCOnNPCInteract) -> Unit) {
        Script.checkLoading()
        npcNpc.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcApproachObject(option: String, obj: String = "*", block: suspend NPC.(NPCOnObjectInteract) -> Unit) {
        Script.checkLoading()
        Wildcards.find(obj, Wildcard.Object) { id ->
            npcObject.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun npcApproachFloorItem(option: String, block: suspend NPC.(NPCOnFloorItemInteract) -> Unit) {
        Script.checkLoading()
        npcFloorItem.getOrPut(option) { mutableListOf() }.add(block)
    }

    companion object : AutoCloseable {
        val playerPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnPlayerInteract) -> Unit>>(2)
        val onPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnPlayerInteract) -> Unit>>(10)

        val playerNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnNPCInteract) -> Unit>>(150)
        val onNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOnNPCInteract) -> Unit>>(250)
        val itemOnNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnNPCInteract) -> Unit>>(25)

        val playerObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnObjectInteract) -> Unit>>(50)
        val onObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOnObjectInteract) -> Unit>>(2)
        val itemOnObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnObjectInteract) -> Unit>>(2)

        val playerFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnFloorItemInteract) -> Unit>>(2)
        val onFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOnFloorItemInteract) -> Unit>>(2)
        val itemOnFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnFloorItemInteract) -> Unit>>(2)

        val npcPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnPlayerInteract) -> Unit>>(2)
        val npcNpc = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnNPCInteract) -> Unit>>(2)
        val npcObject = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnObjectInteract) -> Unit>>(2)
        val npcFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnFloorItemInteract) -> Unit>>(2)

        override fun close() {
            playerPlayer.clear()
            onPlayer.clear()
            playerNpc.clear()
            onNpc.clear()
            itemOnNpc.clear()
            playerObject.clear()
            onObject.clear()
            itemOnObject.clear()
            playerFloorItem.clear()
            onFloorItem.clear()
            itemOnFloorItem.clear()
            npcPlayer.clear()
            npcNpc.clear()
            npcObject.clear()
            npcFloorItem.clear()
        }
    }
}