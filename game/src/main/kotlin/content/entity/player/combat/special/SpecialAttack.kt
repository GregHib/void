package content.entity.player.combat.special

import content.skill.melee.weapon.weapon
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.math.floor

const val MAX_SPECIAL_ATTACK = 1000

interface SpecialAttack {

    fun specialAttack(id: String = "*", block: Player.(target: Character, id: String) -> Unit) {
        specials[id] = block
    }

    fun specialAttackPrepare(id: String = "*", block: Player.(id: String) -> Boolean) {
        prepare[id] = block
    }

    fun specialAttackDamage(id: String = "*", block: Player.(target: Character, damage: Int) -> Unit) {
        damaging[id] = block
    }

    companion object : AutoCloseable {
        val specials = Object2ObjectOpenHashMap<String, Player.(Character, String) -> Unit>()
        val prepare = Object2ObjectOpenHashMap<String, Player.(String) -> Boolean>()
        val damaging = Object2ObjectOpenHashMap<String, Player.(Character, Int) -> Unit>()

        fun special(player: Player, target: Character, id: String) {
            (specials[id] ?: specials["*"])?.invoke(player, target, id)
        }

        fun prepare(player: Player, id: String): Boolean {
            return (prepare[id] ?: prepare["*"])?.invoke(player, id) ?: true
        }

        fun damage(player: Player, target: Character, mode: String, damage: Int) {
            (damaging[mode] ?: damaging["*"])?.invoke(player, target, damage)
        }

        override fun close() {
            specials.clear()
            prepare.clear()
            damaging.clear()
        }

        fun hasEnergy(player: Player) = drain(player, drain = false)

        fun drain(player: Player, drain: Boolean = true): Boolean {
            val amount: Int? = player.weapon.def.getOrNull("special_energy")
            if (amount == null) {
                player.message("This weapon does not have a special attack.")
                player.specialAttack = false
                return false
            }
            var energy = amount
            if (player.equipped(EquipSlot.Ring).id == "ring_of_vigour") {
                energy = floor(energy * 0.9).toInt()
            }
            if (player.specialAttackEnergy < energy) {
                player.message("You don't have enough power left.")
                player.specialAttack = false
                return false
            }
            if (drain) {
                player.specialAttackEnergy -= energy
            }
            return true
        }
    }
}

var Player.specialAttack: Boolean
    get() = get("special_attack", false)
    set(value) = set("special_attack", value)

var Player.specialAttackEnergy: Int
    get() = get("special_attack_energy", MAX_SPECIAL_ATTACK)
    set(value) {
        set("special_attack_energy", value)
        if (value < MAX_SPECIAL_ATTACK) {
            softTimers.startIfAbsent("restore_special_energy")
        }
    }
