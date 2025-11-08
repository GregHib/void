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

    fun playerOperate(option: String, handler: suspend Player.(PlayerPlayerInteract) -> Unit) {
        playerPlayer.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcOperate(option: String, npc: String = "*", handler: suspend Player.(PlayerNPCInteract) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            playerNpc.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun objectOperate(option: String, obj: String = "*", arrive: Boolean = true, handler: suspend Player.(PlayerObjectInteract) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
            if (!arrive) {
                noDelays.add("$option:$id")
            }
            playerObject.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun floorItemOperate(option: String, handler: suspend Player.(PlayerFloorItemInteract) -> Unit) {
        playerFloorItem.getOrPut(option) { mutableListOf() }.add(handler)
    }

    /*
        Interface on
     */

    fun onPlayerOperate(id: String = "*", handler: suspend Player.(ItemPlayerInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            onPlayer.getOrPut(i) { mutableListOf() }.add(handler)
        }
    }

    fun itemOnPlayerOperate(item: String = "*", handler: suspend Player.(ItemPlayerInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { id ->
            onPlayer.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    fun onNPCOperate(id: String = "*", npc: String = "*", handler: suspend Player.(InterfaceNPCInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(npc, Wildcard.Npc) { n ->
                onNpc.getOrPut("$i:$n") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnNPCOperate(item: String = "*", npc: String = "*", handler: suspend Player.(ItemNPCInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(npc, Wildcard.Npc) { id ->
                itemOnNpc.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    fun onObjectOperate(id: String = "*", obj: String = "*", handler: suspend Player.(InterfaceObjectInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(obj, Wildcard.Object) { o ->
                onObject.getOrPut("$i:$o") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnObjectOperate(item: String = "*", obj: String = "*", arrive: Boolean = true, handler: suspend Player.(ItemObjectInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(obj, Wildcard.Object) { id ->
                if (!arrive) {
                    noDelays.add("$itm:$id")
                }
                itemOnObject.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    fun onFloorItemOperate(id: String = "*", floorItem: String = "*", handler: suspend Player.(InterfaceFloorItemInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(floorItem, Wildcard.Item) { floor ->
                onFloorItem.getOrPut("$i:$floor") { mutableListOf() }.add(handler)
            }
        }
    }

    fun itemOnFloorItemOperate(item: String = "*", floorItem: String = "*", handler: suspend Player.(ItemFloorItemInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(floorItem, Wildcard.Item) { id ->
                itemOnFloorItem.getOrPut("$itm:$id") { mutableListOf() }.add(handler)
            }
        }
    }

    /*
        NPC operations
     */

    fun npcOperatePlayer(option: String, handler: suspend NPC.(NPCPlayerInteract) -> Unit) {
        npcPlayer.getOrPut(option) { mutableListOf() }.add(handler)
    }

    fun npcOperateNPC(option: String, npc: String = "*", handler: suspend NPC.(NPCNPCInteract) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcNpc.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun npcOperateObject(option: String, obj: String = "*", arrive: Boolean = true, handler: suspend NPC.(NPCObjectInteract) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
            if (!arrive) {
                noDelays.add("$option:$id")
            }
            npcObject.getOrPut("$option:$id") { mutableListOf() }.add(handler)
        }
    }

    fun npcOperateFloorItem(option: String, handler: suspend NPC.(NPCFloorItemInteract) -> Unit) {
        npcFloorItem.getOrPut(option) { mutableListOf() }.add(handler)
    }

    companion object : AutoCloseable {
        val playerPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerPlayerInteract) -> Unit>>(10)
        val onPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemPlayerInteract) -> Unit>>(2)

        val playerNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerNPCInteract) -> Unit>>(1100)
        val onNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceNPCInteract) -> Unit>>(2)
        val itemOnNpc = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemNPCInteract) -> Unit>>(50)

        val playerObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerObjectInteract) -> Unit>>(750)
        val onObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceObjectInteract) -> Unit>>(2)
        val itemOnObject = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemObjectInteract) -> Unit>>(1400)

        val playerFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerFloorItemInteract) -> Unit>>(2)
        val onFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceFloorItemInteract) -> Unit>>(2)
        val itemOnFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemFloorItemInteract) -> Unit>>(2)

        val npcPlayer = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCPlayerInteract) -> Unit>>(2)
        val npcNpc = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCNPCInteract) -> Unit>>(2)
        val npcObject = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCObjectInteract) -> Unit>>(2)
        val npcFloorItem = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCFloorItemInteract) -> Unit>>(2)

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