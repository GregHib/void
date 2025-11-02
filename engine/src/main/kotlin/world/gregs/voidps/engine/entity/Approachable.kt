package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.Operation.Companion
import world.gregs.voidps.engine.entity.Operation.Companion.noDelays
import world.gregs.voidps.engine.entity.character.mode.interact.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
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
        Interface on
     */

    fun onPlayerApproach(id: String = "*", block: suspend Player.(ItemPlayerInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            onPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    fun itemOnPlayerApproach(item: String = "*", block: suspend Player.(ItemPlayerInteract) -> Unit) {
        for (id in Wildcards.find(item)) {
            onPlayerBlocks.getOrPut(id) { mutableListOf() }.add(block)
        }
    }

    fun onNPCApproach(id: String = "*", npc: String = "*", block: suspend Player.(InterfaceNPCInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            for (n in Wildcards.find(npc)) {
                onNpcBlocks.getOrPut("$i:$n") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnNPCApproach(item: String = "*", npc: String = "*", block: suspend Player.(ItemNPCInteract) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (id in Wildcards.find(npc)) {
                itemOnNpcBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
        }
    }

    fun onObjectApproach(id: String = "*", obj: String = "*", block: suspend Player.(InterfaceObjectInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            for (o in Wildcards.find(obj)) {
                onObjectBlocks.getOrPut("$i:$o") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnObjectApproach(item: String = "*", obj: String = "*", arrive: Boolean = true, block: suspend Player.(ItemObjectInteract) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (id in Wildcards.find(obj)) {
                itemOnObjectBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
        }
    }

    fun onFloorItemApproach(id: String = "*", floorItem: String = "*", block: suspend Player.(InterfaceFloorItemInteract) -> Unit) {
        for (i in Wildcards.find(id)) {
            for (floor in Wildcards.find(floorItem)) {
                onFloorItemBlocks.getOrPut("$i:$floor") { mutableListOf() }.add(block)
            }
        }
    }

    fun itemOnFloorItemApproach(item: String = "*", floorItem: String = "*", arrive: Boolean = true, block: suspend Player.(ItemFloorItemInteract) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (id in Wildcards.find(floorItem)) {
                if (!arrive) {
                    noDelays.add("$itm:$id")
                }
                itemOnFloorItemBlocks.getOrPut("$itm:$id") { mutableListOf() }.add(block)
            }
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
        val onFloorItemBlocks = mutableMapOf<String, MutableList<suspend Player.(InterfaceFloorItemInteract) -> Unit>>()
        val itemOnFloorItemBlocks = mutableMapOf<String, MutableList<suspend Player.(ItemFloorItemInteract) -> Unit>>()

        val npcPlayerBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCPlayerInteract) -> Unit>>()
        val npcNpcBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCNPCInteract) -> Unit>>()
        val npcObjectBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCObjectInteract) -> Unit>>()
        val npcFloorItemBlocks = mutableMapOf<String, MutableList<suspend NPC.(NPCFloorItemInteract) -> Unit>>()

        fun clear() {
            playerPlayerBlocks.clear()
            onPlayerBlocks.clear()
            playerNpcBlocks.clear()
            onNpcBlocks.clear()
            itemOnNpcBlocks.clear()
            playerObjectBlocks.clear()
            onObjectBlocks.clear()
            itemOnObjectBlocks.clear()
            playerFloorItemBlocks.clear()
            onFloorItemBlocks.clear()
            itemOnFloorItemBlocks.clear()
            npcPlayerBlocks.clear()
            npcNpcBlocks.clear()
            npcObjectBlocks.clear()
            npcFloorItemBlocks.clear()
        }
    }
}