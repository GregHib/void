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

    fun playerOperate(option: String, block: suspend Player.(PlayerPlayerInteract) -> Unit) {
        playerPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    fun npcOperate(option: String, npc: String = "*", block: suspend Player.(PlayerNPCInteract) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun objectOperate(option: String, obj: String = "*", arrive: Boolean = true, block: suspend Player.(PlayerObjectInteract) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
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
        Wildcards.find(id, Wildcard.Component) { i ->
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    fun itemOnPlayerOperate(item: String = "*", block: suspend Player.(ItemPlayerInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { id ->
            onPlayerBlocks.getOrPut(id) { mutableListOf() }.add(block)
        }
    }

    fun onNPCOperate(id: String = "*", npc: String = "*", block: suspend Player.(InterfaceNPCInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(npc, Wildcard.Npc) { n ->
                onNpcBlocks.getOrPut("$i:$n") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnNPCOperate(item: String = "*", npc: String = "*", block: suspend Player.(ItemNPCInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(npc, Wildcard.Npc) { id ->
                itemOnNpcBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
        }
    }

    fun onObjectOperate(id: String = "*", obj: String = "*", block: suspend Player.(InterfaceObjectInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(obj, Wildcard.Object) { o ->
                onObjectBlocks.getOrPut("$i:$o") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnObjectOperate(item: String = "*", obj: String = "*", arrive: Boolean = true, block: suspend Player.(ItemObjectInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(obj, Wildcard.Object) { id ->
                if (!arrive) {
                    noDelays.add("$itm:$id")
                }
                itemOnObjectBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
        }
    }

    fun onFloorItemOperate(id: String = "*", floorItem: String = "*", block: suspend Player.(InterfaceFloorItemInteract) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            Wildcards.find(floorItem, Wildcard.Item) { floor ->
                onFloorItemBlocks.getOrPut("$i:$floor") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnFloorItemOperate(item: String = "*", floorItem: String = "*", arrive: Boolean = true, block: suspend Player.(ItemFloorItemInteract) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { itm ->
            Wildcards.find(floorItem, Wildcard.Item) { id ->
                if (!arrive) {
                    noDelays.add("$itm:$id")
                }
                itemOnFloorItemBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
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
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    fun npcOperateObject(option: String, obj: String = "*", arrive: Boolean = true, block: suspend NPC.(NPCObjectInteract) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
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

    companion object {
        val playerPlayerBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerPlayerInteract) -> Unit>>(10)
        val onPlayerBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemPlayerInteract) -> Unit>>(2)

        val playerNpcBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerNPCInteract) -> Unit>>(1100)
        val onNpcBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceNPCInteract) -> Unit>>(2)
        val itemOnNpcBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemNPCInteract) -> Unit>>(50)

        val playerObjectBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerObjectInteract) -> Unit>>(750)
        val onObjectBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceObjectInteract) -> Unit>>(2)
        val itemOnObjectBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemObjectInteract) -> Unit>>(1400)

        val playerFloorItemBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(PlayerFloorItemInteract) -> Unit>>(2)
        val onFloorItemBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(InterfaceFloorItemInteract) -> Unit>>(2)
        val itemOnFloorItemBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend Player.(ItemFloorItemInteract) -> Unit>>(2)

        val npcPlayerBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCPlayerInteract) -> Unit>>(2)
        val npcNpcBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCNPCInteract) -> Unit>>(2)
        val npcObjectBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCObjectInteract) -> Unit>>(2)
        val npcFloorItemBlocks = Object2ObjectOpenHashMap<String, MutableList<suspend NPC.(NPCFloorItemInteract) -> Unit>>(2)

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
            itemOnFloorItemBlocks.clear()
            npcPlayerBlocks.clear()
            npcNpcBlocks.clear()
            npcObjectBlocks.clear()
            npcFloorItemBlocks.clear()
            noDelays.clear()
        }
    }
}