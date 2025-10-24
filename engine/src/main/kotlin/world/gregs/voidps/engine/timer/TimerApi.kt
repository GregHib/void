package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface TimerApi {
    /**
     * [timer] started for [player]
     */
    fun start(player: Player, timer: String, restart: Boolean): Int = REPEAT

    /**
     * [timer] started for [npc]
     */
    fun start(npc: NPC, timer: String, restart: Boolean): Int = REPEAT

    /**
     * World [timer] started
     */
    fun start(timer: String): Int = REPEAT

    /**
     * [timer] ticked for [player]
     */
    fun tick(player: Player, timer: String): Int = REPEAT

    /**
     * [timer] ticked for [npc]
     */
    fun tick(npc: NPC, timer: String): Int = REPEAT

    /**
     * World [timer] ticked
     */
    fun tick(timer: String): Int = REPEAT

    /**
     * [timer] stopped for [player]
     */
    fun stop(player: Player, timer: String, logout: Boolean) {}

    /**
     * [timer] stopped for [npc]
     */
    fun stop(npc: NPC, timer: String, death: Boolean) {}

    /**
     * World [timer] stopped
     */
    fun stop(timer: String, shutdown: Boolean) {}

    companion object : TimerApi {
        var playerDispatcher = MapDispatcher<TimerApi>("@Key")
        var npcDispatcher = MapDispatcher<TimerApi>("@Key")
        var worldDispatcher = MapDispatcher<TimerApi>("@Key")
        const val CANCEL = -1
        const val REPEAT = -2

        override fun start(player: Player, timer: String, restart: Boolean): Int {
            for (instance in playerDispatcher.instances[timer] ?: return CANCEL) {
                val result = instance.start(player, timer, restart)
                if (result != REPEAT) {
                    return result
                }
            }
            return CANCEL
        }

        override fun start(npc: NPC, timer: String, restart: Boolean): Int {
            for (instance in npcDispatcher.instances[timer] ?: return CANCEL) {
                val result = instance.start(npc, timer, restart)
                if (result != REPEAT) {
                    return result
                }
            }
            return CANCEL
        }

        override fun start(timer: String): Int {
            for (instance in worldDispatcher.instances[timer] ?: return CANCEL) {
                val result = instance.start(timer)
                if (result != REPEAT) {
                    return result
                }
            }
            return CANCEL
        }

        override fun tick(player: Player, timer: String): Int {
            for (instance in playerDispatcher.instances[timer] ?: return REPEAT) {
                val result = instance.tick(player, timer)
                if (result != REPEAT) {
                    return result
                }
            }
            return REPEAT
        }

        override fun tick(npc: NPC, timer: String): Int {
            for (instance in npcDispatcher.instances[timer] ?: return REPEAT) {
                val result = instance.tick(npc, timer)
                if (result != REPEAT) {
                    return result
                }
            }
            return REPEAT
        }

        override fun tick(timer: String): Int {
            for (instance in playerDispatcher.instances[timer] ?: return REPEAT) {
                val result = instance.tick(timer)
                if (result != REPEAT) {
                    return result
                }
            }
            return REPEAT
        }

        override fun stop(player: Player, timer: String, logout: Boolean) {
            for (instance in playerDispatcher.instances[timer] ?: return) {
                instance.stop(player, timer, logout)
            }
        }

        override fun stop(npc: NPC, timer: String, death: Boolean) {
            for (instance in npcDispatcher.instances[timer] ?: return) {
                instance.stop(npc, timer, death)
            }
        }

        override fun stop(timer: String, shutdown: Boolean) {
            for (instance in worldDispatcher.instances[timer] ?: return) {
                instance.stop(timer, shutdown)
            }
        }
    }
}