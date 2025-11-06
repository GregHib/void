package content.entity.combat

import content.area.wilderness.inSingleCombat
import content.entity.combat.hit.characterCombatDamage
import content.entity.player.combat.special.specialAttack
import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.attackSpeed
import content.skill.melee.weapon.fightStyle
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.combat.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Combat : Script, CombatApi {

    init {
        CombatMovement.combatReached = { target ->
            combat(this, target)
        }

        playerDespawn {
            for (attacker in attackers) {
                attacker.mode = EmptyMode
            }
        }

        npcDespawn {
            for (attacker in attackers) {
                attacker.mode = EmptyMode
            }
        }

        combatStart { target ->
            if (target.inSingleCombat) {
                target.attackers.clear()
                target.attacker = this
            }
            target.attackers.add(this)
            retaliate(target, this)
        }

        npcCombatStart { target ->
            if (target.inSingleCombat) {
                target.attackers.clear()
                target.attacker = this
            }
            target.attackers.add(this)
            retaliate(target, this)
        }

        combatStop { target ->
            if (target.dead) {
                set("face_entity", target)
            } else {
                clearWatch()
            }
            this.target?.attackers?.remove(this)
            this.target = null
        }

        playerDeath {
            stop(this)
        }

        npcDeath(handler = ::stop)

        characterCombatDamage { character ->
            if (source == character || type == "poison" || type == "disease" || type == "healed") {
                return@characterCombatDamage
            }
            if (character.mode !is CombatMovement && character.mode !is PauseMode) {
                retaliate(character, source)
            }
        }
    }

    fun stop(character: Character) {
        character.stop("in_combat")
        for (attacker in character.attackers) {
            if (attacker.target == character) {
                attacker.stop("in_combat")
            }
        }
    }

    /**
     * [CombatMovement.combatReached] is emitted by [CombatMovement] every tick the [Character] is within range of the target
     */
    fun retaliates(character: Character) = if (character is NPC) {
        character.def["retaliates", true]
    } else {
        character["auto_retaliate", false]
    }

    fun retaliate(character: Character, source: Character) {
        if (character.dead || character.levels.get(Skill.Constitution) <= 0 || !retaliates(character)) {
            return
        }
        if (character is Player && character.mode != EmptyMode) {
            return
        }
        if (character is NPC && character.mode is CombatMovement && character.hasClock("in_combat")) {
            return
        }
        character.mode = CombatMovement(character, source)
        character.target = source
        val delay = character.attackSpeed / 2
        character.start("action_delay", delay)
        character.start("in_combat", delay + 8)
    }

    companion object {
        fun combat(character: Character, target: Character) {
            if (character.mode !is CombatMovement || character.target != target) {
                character.mode = CombatMovement(character, target)
                character.target = target
            }
            val movement = character.mode as CombatMovement
            if (character is Player && character.dialogue != null) {
                return
            }
            if (character.target == null || !Target.attackable(character, target)) {
                character.mode = EmptyMode
                return
            }
            val attackRange = character.attackRange
            if (!movement.arrived(if (attackRange == 1 && character.weapon.def["weapon_type", ""] != "salamander") -1 else attackRange)) {
                return
            }
            if (character.hasClock("action_delay")) {
                return
            }
            val prepare = CombatPrepare(target)
            character.emit(prepare)
            if (prepare.cancelled) {
                character.mode = EmptyMode
                return
            }
            if (character["debug", false] || target["debug", false]) {
                val player = if (character["debug", false] && character is Player) character else target as Player
                player.message("---- Swing (${character.identifier}) -> (${target.identifier}) -----")
            }
            if (!target.hasClock("in_combat")) {
                if (character is Player) {
                    CombatApi.start(character, target)
                } else if (character is NPC) {
                    CombatApi.start(character, target)
                }
            }
            target.start("in_combat", 8)
            val swing = CombatSwing(target)
            character.emit(swing)
            (character as? Player)?.specialAttack = false
            var nextDelay = character.attackSpeed
            if (character.hasClock("miasmic") && (character.fightStyle == "range" || character.fightStyle == "melee")) {
                nextDelay *= 2
            }
            character.start("action_delay", nextDelay)
        }
    }
}
