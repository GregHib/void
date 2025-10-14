package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.dispatch.ListDispatcher
import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.item.floor.FloorItem

interface Spawn {
    fun spawn(player: Player) {}
    fun spawn(npc: NPC) {}
    fun spawn(obj: GameObject) {}
    fun spawn(floorItem: FloorItem) {}
    fun worldSpawn(files: ConfigFiles) {
        worldSpawn()
    }

    fun worldSpawn() {}

    companion object : Spawn {
        var playerDispatcher = ListDispatcher<Spawn>()
        var npcDispatcher = MapDispatcher<Spawn>("Id")
        var objectDispatcher = MapDispatcher<Spawn>("Id")
        var floorItemDispatcher = MapDispatcher<Spawn>("Id")
        var worldDispatcher = ListDispatcher<Spawn>()

        override fun spawn(player: Player) {
            for (instance in playerDispatcher.instances) {
                instance.spawn(player)
            }
        }

        override fun spawn(npc: NPC) {
            npcDispatcher.forEach(npc.id) { instance ->
                instance.spawn(npc)
            }
        }

        override fun spawn(obj: GameObject) {
            objectDispatcher.forEach(obj.id) { instance ->
                instance.spawn(obj)
            }
        }

        override fun spawn(floorItem: FloorItem) {
            floorItemDispatcher.forEach(floorItem.id) { instance ->
                instance.spawn(floorItem)
            }
        }

        override fun worldSpawn(files: ConfigFiles) {
            for (spawner in worldDispatcher.instances) {
                spawner.worldSpawn(files)
            }
        }
    }
}
