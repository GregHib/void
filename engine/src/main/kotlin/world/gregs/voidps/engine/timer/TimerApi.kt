package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface TimerApi {
    /**
     * [timer] started for [player]
     */
    fun start(player: Player, timer: String, restart: Boolean): Int = Timer.CONTINUE

    /**
     * [timer] started for [npc]
     */
    fun start(npc: NPC, timer: String, restart: Boolean): Int = Timer.CONTINUE

    /**
     * World [timer] started
     */
    fun start(timer: String): Int = Timer.CONTINUE

    /**
     * [timer] ticked for [player]
     */
    fun tick(player: Player, timer: String): Int = Timer.CONTINUE

    /**
     * [timer] ticked for [npc]
     */
    fun tick(npc: NPC, timer: String): Int = Timer.CONTINUE

    /**
     * World [timer] ticked
     */
    fun tick(timer: String): Int = Timer.CONTINUE

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
        var playerStartDispatcher = MapDispatcher<TimerApi>("@Key")
        var playerTickDispatcher = MapDispatcher<TimerApi>("@Key")
        var playerStopDispatcher = MapDispatcher<TimerApi>("@Key")
        var npcStartDispatcher = MapDispatcher<TimerApi>("@Key")
        var npcTickDispatcher = MapDispatcher<TimerApi>("@Key")
        var npcStopDispatcher = MapDispatcher<TimerApi>("@Key")
        var worldStartDispatcher = MapDispatcher<TimerApi>("@Key")
        var worldTickDispatcher = MapDispatcher<TimerApi>("@Key")
        var worldStopDispatcher = MapDispatcher<TimerApi>("@Key")


        override fun start(player: Player, timer: String, restart: Boolean): Int {
            for (instance in playerStartDispatcher.instances[timer] ?: return Timer.CANCEL) {
                val result = instance.start(player, timer, restart)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CANCEL
        }

        override fun start(npc: NPC, timer: String, restart: Boolean): Int {
            for (instance in npcStartDispatcher.instances[timer] ?: return Timer.CANCEL) {
                val result = instance.start(npc, timer, restart)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CANCEL
        }

        override fun start(timer: String): Int {
            for (instance in worldStartDispatcher.instances[timer] ?: return Timer.CANCEL) {
                val result = instance.start(timer)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CANCEL
        }

        override fun tick(player: Player, timer: String): Int {
            for (instance in playerTickDispatcher.instances[timer] ?: return Timer.CONTINUE) {
                val result = instance.tick(player, timer)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        override fun tick(npc: NPC, timer: String): Int {
            for (instance in npcTickDispatcher.instances[timer] ?: return Timer.CONTINUE) {
                val result = instance.tick(npc, timer)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        override fun tick(timer: String): Int {
            for (instance in worldTickDispatcher.instances[timer] ?: return Timer.CONTINUE) {
                val result = instance.tick(timer)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        override fun stop(player: Player, timer: String, logout: Boolean) {
            for (instance in playerStopDispatcher.instances[timer] ?: return) {
                instance.stop(player, timer, logout)
            }
        }

        override fun stop(npc: NPC, timer: String, death: Boolean) {
            for (instance in npcStopDispatcher.instances[timer] ?: return) {
                instance.stop(npc, timer, death)
            }
        }

        override fun stop(timer: String, shutdown: Boolean) {
            for (instance in worldStopDispatcher.instances[timer] ?: return) {
                instance.stop(timer, shutdown)
            }
        }
    }
}