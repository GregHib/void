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

interface Operation {

    /**
     * NPC Dialogue helper
     */
    suspend fun Dialogue.talk(player: Player, target: NPC) {}

    /**
     * Player option
     */
    suspend fun operate(player: Player, target: Player, option: String) {}

    /**
     * Interface on Player
     */
    suspend fun operate(player: Player, id: String, target: Player) {}

    /**
     * Item on Player
     */
    suspend fun operate(player: Player, id: String, item: Item, target: Player) {}


    /**
     * Npc option
     */
    suspend fun operate(player: Player, target: NPC, option: String) {}

    /**
     * Interface on NPC
     */
    suspend fun operate(player: Player, id: String, target: NPC) {}

    /**
     * Item on NPC
     */
    suspend fun operate(player: Player, id: String, item: Item, target: NPC) {}

    /**
     * GameObject option
     */
    suspend fun operate(player: Player, target: GameObject, option: String) {}

    /**
     * Interface on GameObject
     */
    suspend fun operate(player: Player, id: String, target: GameObject) {}

    /**
     * Item on GameObject
     */
    suspend fun operate(player: Player, id: String, item: Item, target: GameObject) {}


    /**
     * FloorItem option
     */
    suspend fun operate(player: Player, target: FloorItem, option: String) {}

    /**
     * Interface on FloorItem
     */
    suspend fun operate(player: Player, id: String, target: FloorItem) {}

    /**
     * Item on FloorItem
     */
    suspend fun operate(player: Player, id: String, item: Item, target: FloorItem) {}


    /**
     * Npc player option
     */
    suspend fun operate(npc: NPC, target: Player, option: String) {}

    /**
     * Npc npc option
     */
    suspend fun operate(npc: NPC, target: NPC, option: String) {}

    /**
     * Npc game object option
     */
    suspend fun operate(npc: NPC, target: GameObject, option: String) {}

    /**
     * Player option
     */
    suspend fun operate(npc: NPC, target: FloorItem, option: String) {}

    companion object : Operation {
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


        override suspend fun operate(player: Player, target: Player, option: String) = playerPlayerDispatcher.onFirst(option) { instance ->
            instance.operate(player, target, option)
        }

        override suspend fun operate(player: Player, target: NPC, option: String) {
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

        override suspend fun operate(player: Player, target: GameObject, option: String) = playerObjectDispatcher.onFirst("$option:${target.id}", option) { instance ->
            if (!noDelays.contains(instance)) {
                player.arriveDelay()
            }
            instance.operate(player, target, option)
        }

        override suspend fun operate(player: Player, target: FloorItem, option: String) = playerFloorItemDispatcher.onFirst("$option:${target.id}", option) { instance ->
            if (!noDelays.contains(instance)) {
                player.arriveDelay()
            }
            instance.operate(player, target, option)
        }

        override suspend fun operate(npc: NPC, target: Player, option: String) = npcPlayerDispatcher.onFirst(option) { instance ->
            instance.operate(npc, target, option)
        }

        override suspend fun operate(npc: NPC, target: NPC, option: String) = npcNpcDispatcher.onFirst("$option:${target.id}") { instance ->
            instance.operate(npc, target, option)
        }

        override suspend fun operate(npc: NPC, target: GameObject, option: String) = npcObjectDispatcher.onFirst("$option:${target.id}") { instance ->
            instance.operate(npc, target, option)
        }

        override suspend fun operate(npc: NPC, target: FloorItem, option: String) = npcFloorItemDispatcher.onFirst("$option:${target.id}") { instance ->
            instance.operate(npc, target, option)
        }
    }
}