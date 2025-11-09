package world.gregs.voidps.engine.entity.character.player

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile

interface Teleport {

    fun teleportTakeOff(type: String, block: Player.() -> Boolean) {
        takeOff[type] = block
    }

    fun teleportLand(type: String, block: Player.() -> Unit) {
        land[type] = block
    }

    fun objTeleportTakeOff(option: String = "*", obj: String = "*", block: Player.(obj: GameObject, option: String) -> Int) {
        Wildcards.find(obj, Wildcard.Object) { id ->
            objectTakeOff["$option:$id"] = block
        }
    }

    fun objTeleportLand(option: String = "*", obj: String = "*", block: Player.(obj: GameObject, option: String) -> Unit) {
        Wildcards.find(obj, Wildcard.Object) { id ->
            objectLand["$option:$id"] = block
        }
    }

    companion object : AutoCloseable {
        private val takeOff = Object2ObjectOpenHashMap<String, Player.() -> Boolean>(5)
        private val land = Object2ObjectOpenHashMap<String, Player.() -> Unit>(5)
        private val objectTakeOff = Object2ObjectOpenHashMap<String, Player.(GameObject, String) -> Int>(50)
        private val objectLand = Object2ObjectOpenHashMap<String, Player.(GameObject, String) -> Unit>(20)

        const val CONTINUE = 0
        const val CANCEL = -1

        fun takeOff(player: Player, type: String): Boolean {
            val handler = takeOff[type] ?: return true
            return handler.invoke(player)
        }

        fun land(player: Player, type: String) {
            land[type]?.invoke(player)
        }

        fun takeOff(player: Player, target: GameObject, option: String): Int {
            val handler = objectTakeOff["$option:${target.id}"] ?: objectTakeOff["*:${target.id}"] ?: objectTakeOff["$option:*"] ?: objectTakeOff["*:*"] ?: return CONTINUE
            return handler.invoke(player, target, option)
        }

        fun land(player: Player, target: GameObject, option: String) {
            val handler = objectLand["$option:${target.id}"] ?: objectLand["*:${target.id}"] ?: objectLand["$option:*"] ?: objectLand["*:*"] ?: return
            handler.invoke(player, target, option)
        }

        override fun close() {
            takeOff.clear()
            land.clear()
            objectTakeOff.clear()
            objectLand.clear()
        }

        fun teleport(player: Player, area: String, type: String) {
            teleport(player, get<AreaDefinitions>()[area].random(player)!!, type)
        }

        fun teleport(player: Player, tile: Tile, type: String, sound: Boolean = true) {
            if (player.queue.contains("teleport")) {
                return
            }
            player.closeInterfaces()
            player.strongQueue("teleport", onCancel = null) {
                if (!takeOff(player, type)) {
                    return@strongQueue
                }
                player.steps.clear()
                if (sound) {
                    player.sound("teleport")
                }
                player.gfx("teleport_$type")
                player.animDelay("teleport_$type")
                player.tele(tile)
                player.delay(1)
                if (sound) {
                    player.sound("teleport_land")
                }
                player.gfx("teleport_land_$type")
                player.animDelay("teleport_land_$type")
                if (type == "ancient" || type == "ectophial") {
                    player.delay(1)
                    player.clearAnim()
                }
                land(player, type)
            }
        }
    }
}
