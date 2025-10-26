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

/**
 * Target Entity interaction within close-proximity
 */
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
     * Interface/Item on Player
     */
    suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: Player) {}


    /**
     * Npc option
     */
    suspend fun operate(player: Player, target: NPC, option: String) {}

    /**
     * Interface/Item on NPC
     */
    suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: NPC) {}

    /**
     * GameObject option
     */
    suspend fun operate(player: Player, target: GameObject, option: String) {}

    /**
     * Interface/Item on GameObject
     */
    suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: GameObject) {}


    /**
     * FloorItem option
     */
    suspend fun operate(player: Player, target: FloorItem, option: String) {}

    /**
     * Interface/Item on FloorItem
     */
    suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: FloorItem) {}


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

        var onPlayerDispatcher = MapDispatcher<Operation>("@ItemOn", "@UseOn")
        var onNpcDispatcher = MapDispatcher<Operation>("@ItemOn", "@UseOn")
        var onObjectDispatcher = NoDelayDispatcher(noDelays, "@ItemOn", "@UseOn")
        var onFloorItemDispatcher = NoDelayDispatcher(noDelays, "@ItemOn", "@UseOn")

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

        override suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: Player) = onPlayerDispatcher.onFirst(if (item.isEmpty()) id else item.id) { instance ->
            instance.operate(player, id, item, slot, target)
        }

        override suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: NPC) = onNpcDispatcher.onFirst(if (item.isEmpty()) "$id:${target.def(player).stringId}" else "${item.id}:${target.def(player).stringId}") { instance ->
            instance.operate(player, id, item, slot, target)
        }

        override suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: GameObject) = onObjectDispatcher.onFirst(if (item.isEmpty()) "$id:${target.def(player).stringId}" else "${item.id}:${target.def(player).stringId}") { instance ->
            instance.operate(player, id, item, slot, target)
        }

        override suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: FloorItem) = onFloorItemDispatcher.onFirst(if (item.isEmpty()) "$id:${target.id}" else "${item.id}:${target.id}") { instance ->
            instance.operate(player, id, item, slot, target)
        }
    }
}