package world.gregs.voidps.world.interact.entity.player.combat.special

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player

const val MAX_SPECIAL_ATTACK = 1000

val Player.specialAttack: Boolean
    get() = getVar("special_attack", false)

var Player.specialAttackEnergy: Int
    get() = getVar("special_attack_energy", MAX_SPECIAL_ATTACK)
    set(value) = setVar("special_attack_energy", value)