package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.dispatch.CombinedDispatcher
import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface TimerApi {
    /**
     * [timer] started for [player]
     * @return ticks until start or [Timer.CANCEL]
     */
    fun start(player: Player, timer: String, restart: Boolean): Int = start(player as Character, timer, restart)

    /**
     * [timer] started for [npc]
     * @return ticks until start or [Timer.CANCEL]
     */
    fun start(npc: NPC, timer: String, restart: Boolean): Int = start(npc as Character, timer, restart)

    /**
     * [timer] started for any [character]
     * @return ticks until start or [Timer.CANCEL]
     */
    fun start(character: Character, timer: String, restart: Boolean): Int = 0

    /**
     * World [timer] started
     * @return ticks until start or [Timer.CANCEL]
     */
    fun start(timer: String): Int = 0

    /**
     * [timer] ticked for [player]
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun tick(player: Player, timer: String): Int = tick(player as Character, timer)

    /**
     * [timer] ticked for [npc]
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun tick(npc: NPC, timer: String): Int = tick(npc as Character, timer)

    /**
     * [timer] ticked for any [character]
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun tick(character: Character, timer: String): Int = Timer.CONTINUE

    /**
     * World [timer] ticked
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun tick(timer: String): Int = Timer.CONTINUE

    /**
     * [timer] stopped for [player]
     */
    fun stop(player: Player, timer: String, logout: Boolean) {
        stop(player as Character, timer, logout)
    }

    /**
     * [timer] stopped for [npc]
     */
    fun stop(npc: NPC, timer: String, death: Boolean) {
        stop(npc as Character, timer, death)
    }

    /**
     * [timer] stopped for any [character]
     */
    fun stop(character: Character, timer: String, logout: Boolean) {}

    /**
     * World [timer] stopped
     */
    fun stop(timer: String, shutdown: Boolean) {}

    companion object : TimerApi {
        var playerStartDispatcher = MapDispatcher<TimerApi>("@Timer")
        var playerTickDispatcher = MapDispatcher<TimerApi>("@Timer")
        var playerStopDispatcher = MapDispatcher<TimerApi>("@Timer")
        var npcStartDispatcher = MapDispatcher<TimerApi>("@Timer")
        var npcTickDispatcher = MapDispatcher<TimerApi>("@Timer")
        var npcStopDispatcher = MapDispatcher<TimerApi>("@Timer")
        var worldStartDispatcher = MapDispatcher<TimerApi>("@Timer")
        var worldTickDispatcher = MapDispatcher<TimerApi>("@Timer")
        var worldStopDispatcher = MapDispatcher<TimerApi>("@Timer")

        val characterStartDispatcher = CombinedDispatcher(playerStartDispatcher, npcStartDispatcher)
        val characterTickDispatcher = CombinedDispatcher(playerTickDispatcher, npcTickDispatcher)
        val characterStopDispatcher = CombinedDispatcher(playerStopDispatcher, npcStopDispatcher)

        override fun start(player: Player, timer: String, restart: Boolean): Int {
            var interval = 0
            for (instance in playerStartDispatcher.instances[timer] ?: return 0) {
                val result = instance.start(player, timer, restart)
                if (result == Timer.CANCEL) {
                    return result
                }
                if (result != Timer.CONTINUE) {
                    interval = result
                }
            }
            return interval
        }

        override fun start(npc: NPC, timer: String, restart: Boolean): Int {
            var interval = 0
            for (instance in npcStartDispatcher.instances[timer] ?: return 0) {
                val result = instance.start(npc, timer, restart)
                if (result == Timer.CANCEL) {
                    return result
                }
                if (result != Timer.CONTINUE) {
                    interval = result
                }
            }
            return interval
        }

        override fun start(timer: String): Int {
            var interval = 0
            for (instance in worldStartDispatcher.instances[timer] ?: return 0) {
                val result = instance.start(timer)
                if (result == Timer.CANCEL) {
                    return result
                }
                if (result != Timer.CONTINUE) {
                    interval = result
                }
            }
            return interval
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