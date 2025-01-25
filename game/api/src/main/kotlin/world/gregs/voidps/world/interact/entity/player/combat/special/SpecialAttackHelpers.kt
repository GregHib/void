package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.entity.character.player.Player

const val MAX_SPECIAL_ATTACK = 1000

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