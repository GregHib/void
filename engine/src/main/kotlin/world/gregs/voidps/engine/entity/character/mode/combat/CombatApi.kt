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

    fun npcCombatSwing(handler: NPC.(target: Character) -> Unit) {
        require(swingNpc == null) { "Only one npc swing handler can be registered" }
        swingNpc =  handler
    }

    /**
     * After an [npc] [attack] type
     */
    fun npcAttack(npc: String = "*", attack: String = "*", handler: NPC.(target: Character) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcAttack["$id:$attack"] = handler
        }
    }

    /**
     * After an [npc] [attack] type's impact
     */
    fun npcImpact(npc: String = "*", attack: String = "*", handler: NPC.(target: Character) -> Boolean) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcImpact["$id:$attack"] = handler
        }
    }

    /**
     * Condition required to be able to use a type of [attack]
     */
    fun npcCondition(condition: String, handler: NPC.(target: Character) -> Boolean) {
        npcCondition[condition] = handler
    }

    /**
     * Damage done to a target
     * Emitted on swing, where [combatDamage] is after the attack delay
     * @param style the combat type, typically: melee, range or magic
     */
    fun combatAttack(style: String = "*", handler: Player.(CombatAttack) -> Unit) {
        attacks.getOrPut(style) { mutableListOf() }.add(handler)
    }

    fun npcCombatAttack(npc: String = "*", handler: NPC.(CombatAttack) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            attackNpc.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    /**
     * Damage done by [source] to the emitter
     * Used for defend graphics, for effects use [CombatAttack]
     * @param style the combat type, typically: melee, range or magic
     */
    fun combatDamage(style: String = "*", handler: Player.(CombatDamage) -> Unit) {
        damages.getOrPut(style) { mutableListOf() }.add(handler)
    }

    fun npcCombatDamage(npc: String = "*", style: String = "*", handler: NPC.(CombatDamage) -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            damageNpc.getOrPut("$id:$style") { mutableListOf() }.add(handler)
        }
    }

    fun specialAttack(id: String = "*", block: Player.(target: Character, id: String) -> Unit) {
        specials[id] = block
    }

    fun specialAttackPrepare(id: String = "*", block: Player.(id: String) -> Boolean) {
        prepareSpecial[id] = block
    }

    fun specialAttackDamage(id: String = "*", block: Player.(target: Character, damage: Int) -> Unit) {
        damageSpecial[id] = block
    }

    companion object : AutoCloseable {
        private val start = ObjectArrayList<Player.(Character) -> Unit>(5)
        private val startNpc = ObjectArrayList<NPC.(Character) -> Unit>(5)
        private val stop = ObjectArrayList<Player.(Character) -> Unit>(5)
        private val stopNpc = ObjectArrayList<NPC.(Character) -> Unit>(5)
        private val prepare = Object2ObjectOpenHashMap<String, MutableList<Player.(Character) -> Boolean>>(25)
        private val prepareNpc = Object2ObjectOpenHashMap<String, MutableList<NPC.(Character) -> Boolean>>(5)
        private val swing = Object2ObjectOpenHashMap<String, MutableList<Player.(Character) -> Unit>>(25)
        private var swingNpc: (NPC.(target: Character) -> Unit)? = null
        private val attacks = Object2ObjectOpenHashMap<String, MutableList<Player.(CombatAttack) -> Unit>>(40)
        private val attackNpc = Object2ObjectOpenHashMap<String, MutableList<NPC.(CombatAttack) -> Unit>>(30)
        private val damages = Object2ObjectOpenHashMap<String, MutableList<Player.(CombatDamage) -> Unit>>(40)
        private val damageNpc = Object2ObjectOpenHashMap<String, MutableList<NPC.(CombatDamage) -> Unit>>(30)

        private val specials = Object2ObjectOpenHashMap<String, Player.(Character, String) -> Unit>()
        private val prepareSpecial = Object2ObjectOpenHashMap<String, Player.(String) -> Boolean>()
        private val damageSpecial = Object2ObjectOpenHashMap<String, Player.(Character, Int) -> Unit>()

        private val npcAttack = Object2ObjectOpenHashMap<String, NPC.(Character) -> Unit>(30)
        private val npcImpact = Object2ObjectOpenHashMap<String, NPC.(Character) -> Boolean>(30)
        private val npcCondition = Object2ObjectOpenHashMap<String, NPC.(Character) -> Boolean>(30)

        fun attack(player: Player, attack: CombatAttack) {
            for (handler in attacks[attack.type] ?: emptyList()) {
                handler(player, attack)
            }
            for (handler in attacks["*"] ?: return) {
                handler(player, attack)
            }
        }

        fun attack(npc: NPC, target: Character, id: String) {
            npcAttack[id]?.invoke(npc, target) ?: return
        }

        fun impact(npc: NPC, target: Character, id: String): Boolean {
            return npcImpact[id]?.invoke(npc, target) ?: return true
        }

        fun condition(npc: NPC, target: Character, condition: String): Boolean {
            return npcCondition[condition]?.invoke(npc, target) ?: return true
        }

        fun attack(npc: NPC, attack: CombatAttack) {
            for (handler in attackNpc["${npc.id}:*"] ?: emptyList()) {
                handler(npc, attack)
            }
            for (handler in attackNpc["*:*"] ?: return) {
                handler(npc, attack)
            }
        }

        fun damage(player: Player, damage: CombatDamage) {
            for (handler in damages[damage.type] ?: emptyList()) {
                handler(player, damage)
            }
            for (handler in damages["*"] ?: return) {
                handler(player, damage)
            }
        }

        fun damage(npc: NPC, damage: CombatDamage) {
            for (handler in damageNpc["${npc.id}:${damage.type}"] ?: emptyList()) {
                handler(npc, damage)
            }
            for (handler in damageNpc["*:${damage.type}"] ?: emptyList()) {
                handler(npc, damage)
            }
            for (handler in damageNpc["${npc.id}:*"] ?: emptyList()) {
                handler(npc, damage)
            }
            for (handler in damageNpc["*:*"] ?: return) {
                handler(npc, damage)
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
            swingNpc?.invoke(npc, target) ?: return
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

        fun special(player: Player, target: Character, id: String) {
            (specials[id] ?: specials["*"])?.invoke(player, target, id)
        }

        fun prepareSpec(player: Player, id: String): Boolean = (prepareSpecial[id] ?: prepareSpecial["*"])?.invoke(player, id) ?: true

        fun damageSpec(player: Player, target: Character, mode: String, damage: Int) {
            (damageSpecial[mode] ?: damageSpecial["*"])?.invoke(player, target, damage)
        }

        override fun close() {
            start.clear()
            startNpc.clear()
            stop.clear()
            stopNpc.clear()
            prepare.clear()
            prepareNpc.clear()
            swing.clear()
            swingNpc = null
            attacks.clear()
            attackNpc.clear()
            damages.clear()
            damageNpc.clear()
            specials.clear()
            prepareSpecial.clear()
            damageSpecial.clear()
        }
    }
}