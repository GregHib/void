package world.gregs.voidps.engine.timer

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface TimerApi {
    /**
     * [timer] started for a player
     * @return ticks until start or [Timer.CANCEL]
     */
    fun timerStart(timer: String, block: Player.(restart: Boolean) -> Int) {
        playerStartBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    /**
     * [timer] ticked for a player
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun timerTick(timer: String, block: Player.() -> Int) {
        playerTickBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    /**
     * [timer] stopped for a player
     */
    fun timerStop(timer: String, block: Player.(logout: Boolean) -> Unit) {
        playerStopBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }


    /**
     * [timer] started for an npc
     * @return ticks until start or [Timer.CANCEL]
     */
    fun npcTimerStart(timer: String, block: NPC.(restart: Boolean) -> Int) {
        npcStartBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    /**
     * [timer] ticked for an npc
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun npcTimerTick(timer: String, block: NPC.() -> Int) {
        npcTickBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    /**
     * [timer] stopped for an npc
     */
    fun npcTimerStop(timer: String, block: NPC.(death: Boolean) -> Unit) {
        npcStopBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }


    /**
     * World [timer] started
     * @return ticks until start or [Timer.CANCEL]
     */
    fun worldTimerStart(timer: String, block: () -> Int) {
        worldStartBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    /**
     * World [timer] ticked
     * @return [Timer.CONTINUE] - to continue the timer with the same interval
     * @return [Timer.CANCEL] - to cancel the timer
     * @return interval - to change the timers interval until the next tick
     */
    fun worldTimerTick(timer: String, block: () -> Int) {
        worldTickBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    /**
     * World [timer] stopped
     */
    fun worldTimerStop(timer: String, block: (shutdown: Boolean) -> Unit) {
        worldStopBlocks.getOrPut(timer) { mutableListOf() }.add(block)
    }

    companion object : AutoCloseable {
        val playerStartBlocks = Object2ObjectOpenHashMap<String, MutableList<(Player, Boolean) -> Int>>(50)
        val playerTickBlocks = Object2ObjectOpenHashMap<String, MutableList<(Player) -> Int>>(50)
        val playerStopBlocks = Object2ObjectOpenHashMap<String, MutableList<(Player, Boolean) -> Unit>>(50)
        val npcStartBlocks = Object2ObjectOpenHashMap<String, MutableList<(NPC, Boolean) -> Int>>(25)
        val npcTickBlocks = Object2ObjectOpenHashMap<String, MutableList<(NPC) -> Int>>(25)
        val npcStopBlocks = Object2ObjectOpenHashMap<String, MutableList<(NPC, Boolean) -> Unit>>(25)
        val worldStartBlocks = Object2ObjectOpenHashMap<String, MutableList<() -> Int>>(2)
        val worldTickBlocks = Object2ObjectOpenHashMap<String, MutableList<() -> Int>>(2)
        val worldStopBlocks = Object2ObjectOpenHashMap<String, MutableList<(Boolean) -> Unit>>(2)

        fun start(player: Player, timer: String, restart: Boolean): Int {
            var interval = 0
            for (instance in playerStartBlocks[timer] ?: return 0) {
                val result = instance(player, restart)
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
            for (instance in npcStartBlocks[timer] ?: return 0) {
                val result = instance(npc, restart)
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
            for (instance in worldStartBlocks[timer] ?: return 0) {
                val result = instance()
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
            for (instance in playerTickBlocks[timer] ?: return Timer.CONTINUE) {
                val result = instance(player)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        fun tick(npc: NPC, timer: String): Int {
            for (instance in npcTickBlocks[timer] ?: return Timer.CONTINUE) {
                val result = instance(npc)
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        fun tick(timer: String): Int {
            for (instance in worldTickBlocks[timer] ?: return Timer.CONTINUE) {
                val result = instance()
                if (result != Timer.CONTINUE) {
                    return result
                }
            }
            return Timer.CONTINUE
        }

        fun stop(player: Player, timer: String, logout: Boolean) {
            for (instance in playerStopBlocks[timer] ?: return) {
                instance(player, logout)
            }
        }

        fun stop(npc: NPC, timer: String, death: Boolean) {
            for (instance in npcStopBlocks[timer] ?: return) {
                instance(npc, death)
            }
        }

        fun stop(timer: String, shutdown: Boolean) {
            for (instance in worldStopBlocks[timer] ?: return) {
                instance(shutdown)
            }
        }

        override fun close() {
            playerStartBlocks.clear()
            playerTickBlocks.clear()
            playerStopBlocks.clear()
            npcStartBlocks.clear()
            npcTickBlocks.clear()
            npcStopBlocks.clear()
            worldStartBlocks.clear()
            worldTickBlocks.clear()
            worldStopBlocks.clear()
        }
    }
}