package world.gregs.voidps.engine.timer

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface TimerApi {
    /**
     * [timer] started for a player
     * @return ticks until start or [Timer.CANCEL]
     */
    fun timerStart(timer: String, handler: Player.(restart: Boolean) -> Int) {
        playerStart.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    /**
     * [timer] ticked for a player
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun timerTick(timer: String, handler: Player.() -> Int) {
        playerTick.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    /**
     * [timer] stopped for a player
     */
    fun timerStop(timer: String, handler: Player.(logout: Boolean) -> Unit) {
        playerStop.getOrPut(timer) { mutableListOf() }.add(handler)
    }


    /**
     * [timer] started for an npc
     * @return ticks until start or [Timer.CANCEL]
     */
    fun npcTimerStart(timer: String, handler: NPC.(restart: Boolean) -> Int) {
        npcStart.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    /**
     * [timer] ticked for an npc
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun npcTimerTick(timer: String, handler: NPC.() -> Int) {
        npcTick.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    /**
     * [timer] stopped for an npc
     */
    fun npcTimerStop(timer: String, handler: NPC.(death: Boolean) -> Unit) {
        npcStop.getOrPut(timer) { mutableListOf() }.add(handler)
    }


    /**
     * World [timer] started
     * @return ticks until start or [Timer.CANCEL]
     */
    fun worldTimerStart(timer: String, handler: () -> Int) {
        worldStart.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    /**
     * World [timer] ticked
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun worldTimerTick(timer: String, handler: () -> Int) {
        worldTick.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    /**
     * World [timer] stopped
     */
    fun worldTimerStop(timer: String, handler: (shutdown: Boolean) -> Unit) {
        worldStop.getOrPut(timer) { mutableListOf() }.add(handler)
    }

    companion object : AutoCloseable {
        private val playerStart = Object2ObjectOpenHashMap<String, MutableList<(Player, Boolean) -> Int>>(50)
        private val playerTick = Object2ObjectOpenHashMap<String, MutableList<(Player) -> Int>>(50)
        private val playerStop = Object2ObjectOpenHashMap<String, MutableList<(Player, Boolean) -> Unit>>(50)
        private val npcStart = Object2ObjectOpenHashMap<String, MutableList<(NPC, Boolean) -> Int>>(25)
        private val npcTick = Object2ObjectOpenHashMap<String, MutableList<(NPC) -> Int>>(25)
        private val npcStop = Object2ObjectOpenHashMap<String, MutableList<(NPC, Boolean) -> Unit>>(25)
        private val worldStart = Object2ObjectOpenHashMap<String, MutableList<() -> Int>>(2)
        private val worldTick = Object2ObjectOpenHashMap<String, MutableList<() -> Int>>(2)
        private val worldStop = Object2ObjectOpenHashMap<String, MutableList<(Boolean) -> Unit>>(2)

        fun start(player: Player, timer: String, restart: Boolean): Int {
            var interval = 0
            for (handler in playerStart[timer] ?: return 0) {
                val result = handler(player, restart)
                if (result == Timer.CANCEL) {
                    return result
                }
                if (result != Timer.CONTINUE) {
                    interval = result
                }
            }
            return interval
        }

        fun start(npc: NPC, timer: String, restart: Boolean): Int {
            var interval = 0
            for (handler in npcStart[timer] ?: return 0) {
                val result = handler(npc, restart)
                if (result == Timer.CANCEL) {
                    return result
                }
                if (result != Timer.CONTINUE) {
                    interval = result
                }
            }
            return interval
        }

        fun start(timer: String): Int {
            var interval = 0
            for (handler in worldStart[timer] ?: return 0) {
                val result = handler()
                if (result == Timer.CANCEL) {
                    return result
                }
                if (result != Timer.CONTINUE) {
                    interval = result
                }
            }
            return interval
        }

        fun tick(player: Player, timer: String): Int {
            for (handler in playerTick[timer] ?: return Timer.CONTINUE) {
                val result = handler(player)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        fun tick(npc: NPC, timer: String): Int {
            for (handler in npcTick[timer] ?: return Timer.CONTINUE) {
                val result = handler(npc)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        fun tick(timer: String): Int {
            for (handler in worldTick[timer] ?: return Timer.CONTINUE) {
                val result = handler()
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        fun stop(player: Player, timer: String, logout: Boolean) {
            for (handler in playerStop[timer] ?: return) {
                handler(player, logout)
            }
        }

        fun stop(npc: NPC, timer: String, death: Boolean) {
            for (handler in npcStop[timer] ?: return) {
                handler(npc, death)
            }
        }

        fun stop(timer: String, shutdown: Boolean) {
            for (handler in worldStop[timer] ?: return) {
                handler(shutdown)
            }
        }

        override fun close() {
            playerStart.clear()
            playerTick.clear()
            playerStop.clear()
            npcStart.clear()
            npcTick.clear()
            npcStop.clear()
            worldStart.clear()
            worldTick.clear()
            worldStop.clear()
        }
    }
}