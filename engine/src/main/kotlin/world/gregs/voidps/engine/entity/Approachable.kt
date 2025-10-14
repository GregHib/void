package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.mode.interact.arriveDelay
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject

interface Approachable {

    /**
     * Player option
     */
    suspend fun approach(player: Player, target: Player, option: String) {}

    /**
     * Interface on Player
     */
    suspend fun approach(player: Player, id: String, target: Player) {}

    /**
     * Item on Player
     */
    suspend fun approach(player: Player, id: String, item: Item, target: Player) {}


    /**
     * Npc option
     */
    suspend fun approach(player: Player, target: NPC, option: String) {}

    /**
     * Interface on NPC
     */
    suspend fun approach(player: Player, id: String, target: NPC) {}

    /**
     * Item on NPC
     */
    suspend fun approach(player: Player, id: String, item: Item, target: NPC) {}


    /**
     * GameObject option
     */
    suspend fun approach(player: Player, target: GameObject, option: String) {}

    /**
     * Interface on GameObject
     */
    suspend fun approach(player: Player, id: String, target: GameObject) {}

    /**
     * Item on GameObject
     */
    suspend fun approach(player: Player, id: String, item: Item, target: GameObject) {}


    /**
     * FloorItem option
     */
    suspend fun approach(player: Player, target: FloorItem, option: String) {}

    /**
     * Interface on FloorItem
     */
    suspend fun approach(player: Player, id: String, target: FloorItem) {}

    /**
     * Item on FloorItem
     */
    suspend fun approach(player: Player, id: String, item: Item, target: FloorItem) {}


    /**
     * Npc player option
     */
    suspend fun approach(npc: NPC, target: Player, option: String) {}

    /**
     * Npc npc option
     */
    suspend fun approach(npc: NPC, target: NPC, option: String) {}

    /**
     * Npc game object option
     */
    suspend fun approach(npc: NPC, target: GameObject, option: String) {}

    /**
     * Player option
     */
    suspend fun approach(npc: NPC, target: FloorItem, option: String) {}

    companion object : Approachable {
        var playerPlayerDispatcher = MapDispatcher<Approachable>("@Approach")
        var playerNpcDispatcher = MapDispatcher<Approachable>("@Approach")
        private val noDelays = mutableSetOf<Approachable>()
        var playerObjectDispatcher = NoDelayDispatcher(noDelays)
        var playerFloorItemDispatcher = NoDelayDispatcher(noDelays)
        var npcPlayerDispatcher = MapDispatcher<Approachable>("@Approach")
        var npcNpcDispatcher = MapDispatcher<Approachable>("@Approach")
        var npcObjectDispatcher = MapDispatcher<Approachable>("@Approach")
        var npcFloorItemDispatcher = MapDispatcher<Approachable>("@Approach")

        override suspend fun approach(player: Player, target: Player, option: String) = playerPlayerDispatcher.onFirst(option) { instance ->
            instance.approach(player, target, option)
        }

        override suspend fun approach(player: Player, target: NPC, option: String) = playerNpcDispatcher.onFirst("$option:${target.id}", option) { instance ->
            instance.approach(player, target, option)
        }

        override suspend fun approach(player: Player, target: GameObject, option: String) = playerObjectDispatcher.onFirst("$option:${target.id}", option) { instance ->
            if (!noDelays.contains(instance)) {
                player.arriveDelay()
            }
            instance.approach(player, target, option)
        }

        override suspend fun approach(player: Player, target: FloorItem, option: String) = playerFloorItemDispatcher.onFirst("$option:${target.id}", option) { instance ->
            if (!noDelays.contains(instance)) {
                player.arriveDelay()
            }
            instance.approach(player, target, option)
        }

        override suspend fun approach(npc: NPC, target: Player, option: String) = npcPlayerDispatcher.onFirst(option) { instance ->
            instance.approach(npc, target, option)
        }

        override suspend fun approach(npc: NPC, target: NPC, option: String) = npcNpcDispatcher.onFirst("$option:${target.id}", option) { instance ->
            instance.approach(npc, target, option)
        }

        override suspend fun approach(npc: NPC, target: GameObject, option: String) = npcObjectDispatcher.onFirst("$option:${target.id}", option) { instance ->
            instance.approach(npc, target, option)
        }

        override suspend fun approach(npc: NPC, target: FloorItem, option: String) = npcFloorItemDispatcher.onFirst("$option:${target.id}", option) { instance ->
            instance.approach(npc, target, option)
        }
    }
}