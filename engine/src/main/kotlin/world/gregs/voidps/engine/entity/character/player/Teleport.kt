package world.gregs.voidps.engine.entity.character.player

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile

interface Teleport {

    fun teleportTakeOff(type: String, block: Player.(String) -> Boolean) {
        Script.checkLoading()
        takeOff.getOrPut(type) { mutableSetOf() }.add(block)
    }

    fun teleportLand(type: String, block: Player.() -> Unit) {
        Script.checkLoading()
        land[type] = block
    }

    fun teleportRemoveItems(type: String, block: Player.(String) -> Boolean) {
        Script.checkLoading()
        items.getOrPut(type) { mutableSetOf() }.add(block)
    }

    fun objTeleportTakeOff(option: String = "*", obj: String = "*", block: suspend Player.(obj: GameObject, option: String) -> Int) {
        Script.checkLoading()
        Wildcards.find(obj, Wildcard.Object) { id ->
            objectTakeOff["$option:$id"] = block
        }
    }

    fun objTeleportLand(option: String = "*", obj: String = "*", block: suspend Player.(obj: GameObject, option: String) -> Unit) {
        Script.checkLoading()
        Wildcards.find(obj, Wildcard.Object) { id ->
            objectLand["$option:$id"] = block
        }
    }

    companion object : AutoCloseable {
        private val takeOff = Object2ObjectOpenHashMap<String, MutableSet<Player.(String) -> Boolean>>(5)
        private val items = Object2ObjectOpenHashMap<String, MutableSet<Player.(String) -> Boolean>>(5)
        private val land = Object2ObjectOpenHashMap<String, Player.() -> Unit>(5)
        private val objectTakeOff = Object2ObjectOpenHashMap<String, suspend Player.(GameObject, String) -> Int>(50)
        private val objectLand = Object2ObjectOpenHashMap<String, suspend Player.(GameObject, String) -> Unit>(20)

        const val CONTINUE = 0
        const val CANCEL = -1

        fun takeOff(player: Player, type: String, item: String): Boolean {
            for (handler in takeOff[type] ?: return true) {
                if (!handler.invoke(player, item)) {
                    return false
                }
            }
            return true
        }

        fun removeItems(player: Player, type: String, item: String): Boolean {
            for (handler in items[type] ?: return true) {
                if (!handler.invoke(player, item)) {
                    return false
                }
            }
            return true
        }

        fun land(player: Player, type: String) {
            land[type]?.invoke(player)
        }

        suspend fun takeOff(player: Player, target: GameObject, option: String): Int {
            val handler = objectTakeOff["$option:${target.id}"] ?: objectTakeOff["*:${target.id}"] ?: objectTakeOff["$option:*"] ?: objectTakeOff["*:*"] ?: return CONTINUE
            return handler.invoke(player, target, option)
        }

        suspend fun land(player: Player, target: GameObject, option: String) {
            val handler = objectLand["$option:${target.id}"] ?: objectLand["*:${target.id}"] ?: objectLand["$option:*"] ?: objectLand["*:*"] ?: return
            handler.invoke(player, target, option)
        }

        override fun close() {
            items.clear()
            takeOff.clear()
            land.clear()
            objectTakeOff.clear()
            objectLand.clear()
        }

        fun teleport(player: Player, area: String, type: String, spell: String? = null, sound: Boolean = true, force: Boolean = false, xp: Double = 0.0) {
            teleport(player, Areas[area].random(player)!!, type, spell, sound, force, xp)
        }

        fun teleport(player: Player, tile: Tile, type: String, spell: String? = null, sound: Boolean = true, force: Boolean = false, xp: Double = 0.0): Boolean {
            if (!force && player.queue.contains(ActionPriority.Strong)) {
                return false
            }
            player.closeInterfaces()
            player.strongQueue("teleport") {
                if (!takeOff(player, type, spell ?: "")) {
                    return@strongQueue
                }
                if (spell != null && !removeItems(player, type, spell)) {
                    return@strongQueue
                }
                player.steps.clear()
                player.exp(Skill.Magic, xp)
                if (sound) {
                    player.sound("teleport_${type}")
                }
                player.gfx("teleport_$type")
                player.animDelay("teleport_$type")
                player.tele(tile)
                player.delay(1)
                if (sound) {
                    player.sound("teleport_land_${type}")
                }
                player.gfx("teleport_land_$type")
                val delay = player.anim("teleport_land_$type")
                if (delay == -1) {
                    player.clearAnim()
                } else {
                    player.delay(delay)
                }
                if (type == "ancient" || type == "ectophial") {
                    player.delay(1)
                }
                land(player, type)
            }
            return true
        }
    }
}
