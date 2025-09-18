package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.dispatch.ListDispatcher
import world.gregs.voidps.engine.entity.item.floor.FloorItem

// TODO rename Spawn
interface Spawn {
    fun spawn(player: Player) {}
    fun spawn(npc: NPC) {}
    fun spawn(obj: GameObject) {}
    fun spawn(floorItem: FloorItem) {}
    fun worldSpawn() {}

    companion object : Spawn {
        var playerDispatcher = ListDispatcher<Spawn>()
        var npcDispatcher = ListDispatcher<Spawn>()
        var objectDispatcher = ListDispatcher<Spawn>()
        var floorItemDispatcher = ListDispatcher<Spawn>()
        var worldDispatcher = ListDispatcher<Spawn>()

        override fun spawn(player: Player) {
            for (instance in playerDispatcher.instances) {
                instance.spawn(player)
            }
        }

        override fun spawn(npc: NPC) {
            for (instance in npcDispatcher.instances) {
                instance.spawn(npc)
            }
        }

        override fun spawn(obj: GameObject) {
            for (instance in objectDispatcher.instances) {
                instance.spawn(obj)
            }
        }

        override fun spawn(floorItem: FloorItem) {
            for (instance in floorItemDispatcher.instances) {
                instance.spawn(floorItem)
            }
        }

        override fun worldSpawn() {
            for (spawner in worldDispatcher.instances) {
                spawner.worldSpawn()
            }
        }
    }
}
