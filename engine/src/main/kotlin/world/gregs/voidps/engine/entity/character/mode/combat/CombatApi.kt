package world.gregs.voidps.engine.entity.character.mode.combat

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

interface CombatApi {

    fun combatStart(handler: Player.(Character) -> Unit) {
        start.add(handler)
    }

    fun npcCombatStart(handler: NPC.(Character) -> Unit) {
        startNpc.add(handler)
    }

    /**
     * Combat movement has stopped
     */
    fun combatStop(handler: Player.(Character) -> Unit) {
        stop.add(handler)
    }

    fun npcCombatStop(handler: NPC.(Character) -> Unit) {
        stopNpc.add(handler)
    }

    companion object : AutoCloseable {
        private val start = ObjectArrayList<Player.(Character) -> Unit>(5)
        private val startNpc = ObjectArrayList<NPC.(Character) -> Unit>(5)
        private val stop = ObjectArrayList<Player.(Character) -> Unit>(5)
        private val stopNpc = ObjectArrayList<NPC.(Character) -> Unit>(5)

        fun start(player: Player, target: Character) {
            for (handler in start) {
                handler(player, target)
            }
        }

        fun start(npc: NPC, target: Character) {
            for (handler in startNpc) {
                handler(npc, target)
            }
        }

        fun stop(player: Player, target: Character) {
            for (handler in stop) {
                handler(player, target)
            }
        }

        fun stop(npc: NPC, target: Character) {
            for (handler in stopNpc) {
                handler(npc, target)
            }
        }

        override fun close() {
            start.clear()
            startNpc.clear()
            stop.clear()
            stopNpc.clear()
        }
    }
}