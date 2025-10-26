package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.dispatch.Dispatcher
import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.mode.interact.arriveDelay
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Wildcards

/**
 * Target Entity interaction within close-proximity
 */
interface Operation {

    /**
     * NPC Dialogue helper
     */
    fun talkWith(npc: String, block: suspend Dialogue.() -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("Talk-to:$id") { mutableListOf() }.add { player, target ->
                player.talkWith(target) { block(this) }
            }
        }
    }

    /**
     * Player option
     */
    fun playerOperate(option: String, block: suspend (player: Player, target: Player) -> Unit) {
        for (opt in Wildcards.find(option)) {
            playerPlayerBlocks.getOrPut(opt) { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on Player
     */
    fun interfaceOnPlayerOperate(id: String, block: suspend (player: Player, id: String, slot: Int, target: Player) -> Unit) {
        for (i in Wildcards.find(id)) {
            interfaceOnPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }

    /**
     * Item on Player
     */
    fun itemOnPlayerOperate(item: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: Player) -> Unit) {
        for (i in Wildcards.find(item)) {
            itemOnPlayerBlocks.getOrPut(i) { mutableListOf() }.add(block)
        }
    }


    /**
     * Npc option
     */
    fun npcOperate(option: String, npc: String, block: suspend (Player, NPC) -> Unit) {
        for (id in Wildcards.find(npc)) {
            playerNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on NPC
     */
    fun interfaceOnNpcOperate(id: String, npc: String, block: suspend (player: Player, id: String, slot: Int, target: NPC) -> Unit) {
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(npc)) {
                interfaceOnNpcBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Item on NPC
     */
    fun itemOnNpcOperate(item: String, npc: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: NPC) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(npc)) {
                itemOnNpcBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    /**
     * GameObject option
     */
    fun objectOperate(option: String, obj: String, block: suspend (Player, GameObject) -> Unit) {
        for (id in Wildcards.find(obj)) {
            playerObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on GameObject
     */
    fun interfaceOnObjectOperate(id: String, obj: String, block: suspend (player: Player, id: String, slot: Int, target: GameObject) -> Unit) {
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(obj)) {
                interfaceOnObjectBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Item on GameObject
     */
    fun itemOnObjectOperate(item: String, obj: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: GameObject) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(obj)) {
                itemOnObjectBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    /**
     * FloorItem option
     */
    fun floorItemOperate(option: String, item: String, block: suspend (Player, FloorItem) -> Unit) {
        for (id in Wildcards.find(item)) {
            playerFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Interface on FloorItem
     */
    fun interfaceOnFloorItemOperate(id: String, item: String, block: suspend (player: Player, id: String, slot: Int, target: FloorItem) -> Unit) {
        for (itf in Wildcards.find(id)) {
            for (i in Wildcards.find(item)) {
                interfaceOnFloorItemBlocks.getOrPut("$itf:$i") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Item on FloorItem
     */
    fun itemOnFloorItemOperate(item: String, floorItem: String, block: suspend (player: Player, id: String, slot: Int, item: Item, target: FloorItem) -> Unit) {
        for (itm in Wildcards.find(item)) {
            for (i in Wildcards.find(floorItem)) {
                itemOnFloorItemBlocks.getOrPut("$itm:$i") { mutableListOf() }.add(block)
            }
        }
    }


    /**
     * Npc player option
     */
    suspend fun npcOperatePlayer(option: String, block: suspend (npc: NPC, target: Player) -> Unit) {
        npcPlayerBlocks.getOrPut(option) { mutableListOf() }.add(block)
    }

    /**
     * Npc npc option
     */
    suspend fun npcOperateNpc(option: String, npc: String, block: suspend (npc: NPC, target: NPC) -> Unit) {
        for (id in Wildcards.find(npc)) {
            npcNpcBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Npc game object option
     */
    suspend fun npcOperateObject(option: String, obj: String, block: suspend (npc: NPC, target: GameObject) -> Unit) {
        for (id in Wildcards.find(obj)) {
            npcObjectBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    /**
     * Npc floor item option
     */
    suspend fun npcOperateFloorItem(option: String, item: String, block: suspend (npc: NPC, target: FloorItem) -> Unit) {
        for (id in Wildcards.find(item)) {
            npcFloorItemBlocks.getOrPut("$option:$id") { mutableListOf() }.add(block)
        }
    }

    companion object {
        val playerPlayerBlocks = mutableMapOf<String, MutableList<suspend (Player, Player) -> Unit>>()
        val interfaceOnPlayerBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Player) -> Unit>>()
        val itemOnPlayerBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, Player) -> Unit>>()

        val playerNpcBlocks = mutableMapOf<String, MutableList<suspend (Player, NPC) -> Unit>>()
        val interfaceOnNpcBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, NPC) -> Unit>>()
        val itemOnNpcBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, NPC) -> Unit>>()

        val playerObjectBlocks = mutableMapOf<String, MutableList<suspend (Player, GameObject) -> Unit>>()
        val interfaceOnObjectBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, GameObject) -> Unit>>()
        val itemOnObjectBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, GameObject) -> Unit>>()

        val playerFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, FloorItem) -> Unit>>()
        val interfaceOnFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, FloorItem) -> Unit>>()
        val itemOnFloorItemBlocks = mutableMapOf<String, MutableList<suspend (Player, String, Int, Item, FloorItem) -> Unit>>()

        val npcPlayerBlocks = mutableMapOf<String, MutableList<suspend (NPC, Player) -> Unit>>()
        val npcNpcBlocks = mutableMapOf<String, MutableList<suspend (NPC, NPC) -> Unit>>()
        val npcObjectBlocks = mutableMapOf<String, MutableList<suspend (NPC, GameObject) -> Unit>>()
        val npcFloorItemBlocks = mutableMapOf<String, MutableList<suspend (NPC, FloorItem) -> Unit>>()


        var playerPlayerDispatcher = MapDispatcher<Operation>("@Operate")
        var playerNpcDispatcher = MapDispatcher<Operation>("@Operate")
        var talkDispatcher = object : Dispatcher<Operation> {
            override fun process(instance: Operation, annotation: String, arguments: String) {
                if (annotation == "@Id") {
                    playerNpcDispatcher.process(instance, "@Operate", "Talk-to:$arguments")
                }
            }

            override fun clear() {}
        }
        private val noDelays = mutableSetOf<Operation>()
        var playerObjectDispatcher = NoDelayDispatcher(noDelays)
        var playerFloorItemDispatcher = NoDelayDispatcher(noDelays)
        var npcPlayerDispatcher = MapDispatcher<Operation>("@Operate")
        var npcNpcDispatcher = MapDispatcher<Operation>("@Operate")
        var npcObjectDispatcher = MapDispatcher<Operation>("@Operate")
        var npcFloorItemDispatcher = MapDispatcher<Operation>("@Operate")


        suspend fun operate(player: Player, target: Player, option: String) {
            for (block in playerPlayerBlocks[option] ?: return) {
                block(player, target)
            }
        }

        suspend fun operate(player: Player, target: NPC, option: String) {
            if (option == "Talk-to") {
                for (block in playerNpcBlocks["$option:${target.def(player).id}"] ?: return) {
                    block(player, target)
                }
            }
            playerNpcDispatcher.onFirst("$option:${target.id}", option) { instance ->
                if (option != "Talk-to") {
                    instance.operate(player, target, option)
                    return@onFirst
                }
                with(instance) {
                    player.talkWith(target) {
                        talk(player, target)
                    }
                }
            }
        }

        suspend fun operate(player: Player, target: GameObject, option: String) = playerObjectDispatcher.onFirst("$option:${target.id}", option) { instance ->
            if (!noDelays.contains(instance)) {
                player.arriveDelay()
            }
            instance.operate(player, target, option)
        }

        suspend fun operate(player: Player, target: FloorItem, option: String) = playerFloorItemDispatcher.onFirst("$option:${target.id}", option) { instance ->
            if (!noDelays.contains(instance)) {
                player.arriveDelay()
            }
            instance.operate(player, target, option)
        }

        suspend fun operate(npc: NPC, target: Player, option: String) = npcPlayerDispatcher.onFirst(option) { instance ->
            instance.operate(npc, target, option)
        }

        suspend fun operate(npc: NPC, target: NPC, option: String) = npcNpcDispatcher.onFirst("$option:${target.id}") { instance ->
            instance.operate(npc, target, option)
        }

        suspend fun operate(npc: NPC, target: GameObject, option: String) = npcObjectDispatcher.onFirst("$option:${target.id}") { instance ->
            instance.operate(npc, target, option)
        }

        suspend fun operate(npc: NPC, target: FloorItem, option: String) = npcFloorItemDispatcher.onFirst("$option:${target.id}") { instance ->
            instance.operate(npc, target, option)
        }
    }
}