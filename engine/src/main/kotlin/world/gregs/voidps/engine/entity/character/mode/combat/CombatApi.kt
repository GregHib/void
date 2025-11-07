package world.gregs.voidps.engine.entity.character.mode.combat

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface CombatApi {

    fun combatStart(handler: Player.(target: Character) -> Unit) {
        start.add(handler)
    }

    fun npcCombatStart(handler: NPC.(target: Character) -> Unit) {
        startNpc.add(handler)
    }

    /**
     * Combat movement has stopped
     */
    fun combatStop(handler: Player.(target: Character) -> Unit) {
        stop.add(handler)
    }

    fun npcCombatStop(handler: NPC.(target: Character) -> Unit) {
        stopNpc.add(handler)
    }

    /**
     * Prepare for combat by checking resources and calculating attack style against [target]
     */
    fun combatPrepare(style: String = "*", handler: Player.(target: Character) -> Boolean) {
        prepare.getOrPut(style) { mutableListOf() }.add(handler)
    }

    fun npcCombatPrepare(npc: String = "*", handler: NPC.(target: Character) -> Boolean) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            prepareNpc.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    /**
     * A turn in a combat scenario resulting one or many hits
     */
    fun combatSwing(weapon: String = "*", style: String = "*", handler: Player.(target: Character) -> Unit) {
        Wildcards.find(weapon, Wildcard.Item) { id ->
            swing.getOrPut("$id:$style") { mutableListOf() }.add(handler)
        }
    }

    fun npcCombatSwing(npc: String = "*", style: String = "*", handler: NPC.(target: Character) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            swingNpc.getOrPut("$id:$style") { mutableListOf() }.add(handler)
        }
    }

    /**
     * Damage done to a target
     * Emitted on swing, where [combatDamage] is after the attack delay
     * @param type the combat type, typically: melee, range or magic
     * @param damage the damage inflicted upon the [target]
     * @param delay until hit in client ticks
     */
    fun combatAttack(style: String = "*", handler: Player.(CombatAttack) -> Unit) {
        attacks.getOrPut(style) { mutableListOf() }.add(handler)
    }

    fun npcCombatAttack(npc: String = "*", style: String = "*", handler: NPC.(CombatAttack) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            attackNpc.getOrPut("$id:$style") { mutableListOf() }.add(handler)
        }
    }

    companion object : AutoCloseable {
        private val start = ObjectArrayList<Player.(Character) -> Unit>(5)
        private val startNpc = ObjectArrayList<NPC.(Character) -> Unit>(5)
        private val stop = ObjectArrayList<Player.(Character) -> Unit>(5)
        private val stopNpc = ObjectArrayList<NPC.(Character) -> Unit>(5)
        private val prepare = Object2ObjectOpenHashMap<String, MutableList<Player.(Character) -> Boolean>>(25)
        private val prepareNpc = Object2ObjectOpenHashMap<String, MutableList<NPC.(Character) -> Boolean>>(5)
        private val swing = Object2ObjectOpenHashMap<String, MutableList<Player.(Character) -> Unit>>(25)
        private val swingNpc = Object2ObjectOpenHashMap<String, MutableList<NPC.(Character) -> Unit>>(30)
        private val attacks = Object2ObjectOpenHashMap<String, MutableList<Player.(CombatAttack) -> Unit>>(40)
        private val attackNpc = Object2ObjectOpenHashMap<String, MutableList<NPC.(CombatAttack) -> Unit>>(30)

        fun attack(player: Player, attack: CombatAttack) {
            for (handler in attacks[attack.type] ?: emptyList()) {
                handler(player, attack)
            }
            for (handler in attacks["*"] ?: return) {
                handler(player, attack)
            }
        }

        fun attack(npc: NPC, attack: CombatAttack) {
            for (handler in attackNpc["${npc.id}:${attack.type}"] ?: emptyList()) {
                handler(npc, attack)
            }
            for (handler in attackNpc["*:${attack.type}"] ?: emptyList()) {
                handler(npc, attack)
            }
            for (handler in attackNpc["${npc.id}:*"] ?: emptyList()) {
                handler(npc, attack)
            }
            for (handler in attackNpc["*:*"] ?: return) {
                handler(npc, attack)
            }
        }

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

        fun swing(player: Player, target: Character, weapon: String, style: String) {
            for (handler in swing["$weapon:$style"] ?: swing["*:$style"] ?: swing["$weapon:*"] ?: swing["*:*"] ?: return) {
                handler(player, target)
            }
        }

        fun swing(npc: NPC, target: Character, style: String) {
            for (handler in swingNpc["${npc.id}:$style"] ?: swingNpc["*:$style"] ?: swingNpc["${npc.id}:*"] ?: swingNpc["*:*"] ?: return) {
                handler(npc, target)
            }
        }

        fun prepare(player: Player, target: Character, style: String): Boolean {
            for (handler in prepare[style] ?: emptyList()) {
                if (!handler(player, target)) {
                    return false
                }
            }
            for (handler in prepare["*"] ?: emptyList()) {
                if (!handler(player, target)) {
                    return false
                }
            }
            return true
        }

        fun prepare(npc: NPC, target: Character): Boolean {
            for (handler in prepareNpc[npc.id] ?: emptyList()) {
                if (!handler(npc, target)) {
                    return false
                }
            }
            for (handler in prepareNpc["*"] ?: emptyList()) {
                if (!handler(npc, target)) {
                    return false
                }
            }
            return true
        }

        override fun close() {
            start.clear()
            startNpc.clear()
            stop.clear()
            stopNpc.clear()
            prepare.clear()
            prepareNpc.clear()
            swing.clear()
            swingNpc.clear()
            attacks.clear()
            attackNpc.clear()
        }
    }
}