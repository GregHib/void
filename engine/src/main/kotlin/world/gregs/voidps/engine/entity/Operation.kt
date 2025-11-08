package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.mode.interact.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

/**
 * Target Entity interaction within close-proximity
 */
interface Operation {


    /*
        Player operations
     */

    fun playerOperate(option: String, handler: suspend Player.(PlayerOnPlayerInteract) -> Unit) {
        playerPlayer.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcOperate(option: String, npc: String = "*", handler: suspend Player.(PlayerOnNPCInteract) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            playerNpc.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun objectOperate(option: String, obj: String = "*", arrive: Boolean = true, handler: suspend Player.(PlayerOnObjectInteract) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
            if (!arrive) {
                noDelays.add("$option:$id")
            }
            playerObject.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun floorItemOperate(option: String, handler: suspend Player.(PlayerOnFloorItemInteract) -> Unit) {
        playerFloorItem.getOrPut(option) { mutableListOf() }.add(handler)
    }

    /*
        Interface on
     */

    fun onPlayerOperate(id: String = "*", handler: suspend Player.(ItemOnPlayerInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            onPlayer.getOrPut(i) { mutableListOf() }.add(handler)
        }
    }

    fun itemOnPlayerOperate(item: String = "*", handler: suspend Player.(ItemOnPlayerInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { id ->
            onPlayer.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    fun onNPCOperate(id: String = "*", npc: String = "*", handler: suspend Player.(InterfaceOnNPCInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(npc, Wildcard.Npc) { n ->
                onNpc.getOrPut("$i:$n") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnNPCOperate(item: String = "*", npc: String = "*", handler: suspend Player.(ItemOnNPCInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(npc, Wildcard.Npc) { id ->
                itemOnNpc.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    fun onObjectOperate(id: String = "*", obj: String = "*", handler: suspend Player.(InterfaceOnObjectInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(obj, Wildcard.Object) { o ->
                onObject.getOrPut("$i:$o") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnObjectOperate(item: String = "*", obj: String = "*", arrive: Boolean = true, handler: suspend Player.(ItemOnObjectInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(obj, Wildcard.Object) { id ->
                if (!arrive) {
                    noDelays.add("$itm:$id")
                }
                itemOnObject.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    fun onFloorItemOperate(id: String = "*", floorItem: String = "*", handler: suspend Player.(InterfaceOnFloorItemInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(floorItem, Wildcard.Item) { floor ->
                onFloorItem.getOrPut("$i:$floor") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnFloorItemOperate(item: String = "*", floorItem: String = "*", handler: suspend Player.(ItemOnFloorItemInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(floorItem, Wildcard.Item) { id ->
                itemOnFloorItem.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    /*
        NPC operations
     */

    fun npcOperatePlayer(option: String, handler: suspend NPC.(NPCOnPlayerInteract) -> Unit) {
        npcPlayer.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcOperateNPC(option: String, npc: String = "*", handler: suspend NPC.(NPCOnNPCInteract) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcNpc.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun npcOperateObject(option: String, obj: String = "*", arrive: Boolean = true, handler: suspend NPC.(NPCOnObjectInteract) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
            if (!arrive) {
                noDelays.add("$option:$id")
            }
            npcObject.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun npcOperateFloorItem(option: String, handler: suspend NPC.(NPCOnFloorItemInteract) -> Unit) {
        npcFloorItem.getOrPut(option) { mutableListOf() }.add(handler)
    }

    companion object : AutoCloseable {
        val playerPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnPlayerInteract) -> Unit>>(10)
        val onPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnPlayerInteract) -> Unit>>(2)

        val playerNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnNPCInteract) -> Unit>>(1100)
        val onNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOnNPCInteract) -> Unit>>(2)
        val itemOnNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnNPCInteract) -> Unit>>(50)

        val playerObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnObjectInteract) -> Unit>>(750)
        val onObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOnObjectInteract) -> Unit>>(2)
        val itemOnObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnObjectInteract) -> Unit>>(1400)

        val playerFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerOnFloorItemInteract) -> Unit>>(2)
        val onFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceOnFloorItemInteract) -> Unit>>(2)
        val itemOnFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemOnFloorItemInteract) -> Unit>>(2)

        val npcPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnPlayerInteract) -> Unit>>(2)
        val npcNpc = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnNPCInteract) -> Unit>>(2)
        val npcObject = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnObjectInteract) -> Unit>>(2)
        val npcFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCOnFloorItemInteract) -> Unit>>(2)

        // Don't call arriveDelay before an object interaction
        val noDelays = mutableSetOf<String>()

        override fun close() {
            playerPlayer.clear()
            onPlayer.clear()
            playerNpc.clear()
            onNpc.clear()
            itemOnNpc.clear()
            playerObject.clear()
            itemOnObject.clear()
            playerFloorItem.clear()
            onFloorItem.clear()
            itemOnFloorItem.clear()
            npcPlayer.clear()
            npcNpc.clear()
            npcObject.clear()
            npcFloorItem.clear()
            noDelays.clear()
        }
    }
}