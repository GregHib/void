package content.entity.combat

import content.area.wilderness.inSingleCombat
import content.entity.effect.stunned
import content.entity.player.combat.special.specialAttack
import content.skill.magic.Magic
import content.skill.melee.weapon.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.combat.*
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement.Companion.leashAnchor
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class Combat(val combatDefinitions: CombatDefinitions) :
    Script,
    CombatApi {

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

        npcCombatStop { target ->
            if (target.dead) {
                set("face_entity", target)
            } else {
                clearWatch()
            }
            target.stop("under_attack")
            target.attackers.remove(this)
            this.target = null
        }

        playerDeath(handler = ::stop)
        npcDeath(handler = ::stop)

        combatDamage(handler = ::damage)
        npcCombatDamage(handler = ::damage)
    }

    fun damage(character: Character, it: CombatDamage) {
        val (source, type) = it
        if (source == character || type == "poison" || type == "disease" || type == "healed") {
            return
        }
        if (character.mode !is CombatMovement && character.mode !is PauseMode) {
            retaliate(character, source)
        }
    }

    fun stop(character: Character, onDeath: Death.OnDeath) {
        character.stop("under_attack")
        for (attacker in character.attackers) {
            if (attacker.target == character) {
                attacker.stop("under_attack")
                // Clear the stale target too - a familiar (whose combat mode ends without a
                // combatStop) would otherwise re-engage it, e.g. via a titan's called special,
                // even after the npc dies and respawns.
                attacker.target = null
            }
        }
    }

    /**
     * [CombatMovement.combatReached] is emitted by [CombatMovement] every tick the [Character] is within range of the target
     */
    fun retaliates(character: Character) = if (character is NPC) {
        character.transformDef["retaliates", true]
    } else {
        character["auto_retaliate", false]
    }

    fun retaliate(character: Character, source: Character) {
        if (source is NPC && source.def.options.contains("Tease")) {
            return
        }
        if (character.dead || character.levels.get(Skill.Constitution) <= 0 || !retaliates(character)) {
            return
        }
        if (character is Player && character.mode != EmptyMode) {
            return
        }
        if (character is NPC && character.attacking && character.underAttack) {
            return
        }
        // A familiar attacking an npc should always draw retaliation - the spawn/aggro leash (which
        // keeps an npc from being dragged off its spawn) must not stop it defending itself, or an npc
        // hit by a familiar away from its spawn just walks off without ever fighting back.
        val sourceIsFamiliar = source is NPC && source["owner_index", -1] != -1
        if (character is NPC && !sourceIsFamiliar) {
            // Retreat
            val definition = combatDefinitions.getOrNull(character.transformDef["combat_def", character.id]) ?: return
            val spawn: Tile = character.leashAnchor() ?: return
            if (!CombatMovement.withinAggro(source, spawn, definition)) {
                if (character.mode !is Retreat || (character.mode as Retreat).target != source) {
                    character.mode = Retreat(character, source, spawn, definition.retreatRange)
                }
                return
            }
        }
        character.mode = CombatMovement(character, source)
        character.target = source
        val delay = character.attackSpeed / 2
        character.start("action_delay", delay)
        character.start("under_attack", delay + 8)
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
            // A stunned character can't swing. CombatMovement keeps ticking (and re-entering here)
            // while stunned, so without this a stunned npc/player would still land its attacks -
            // only its movement is otherwise gated by the stun's "delay". Skipping leaves the stun
            // to expire and the fight resumes on the next tick.
            if (character.stunned) {
                return
            }
            (character.mode as? CombatMovement)?.started = true
            val prepared = when (character) {
                is Player -> CombatApi.prepare(character, target, character.fightStyle)
                is NPC -> CombatApi.prepare(character, target)
                else -> return
            }
            if (!prepared) {
                character.mode = EmptyMode
                return
            }
            if (character["debug", false] || target["debug", false]) {
                val player = if (character["debug", false] && character is Player) character else target as Player
                val id = when (character) {
                    is NPC -> character.id
                    is Player -> character.name
                    else -> ""
                }
                val targetId = when (target) {
                    is NPC -> target.id
                    is Player -> target.name
                    else -> ""
                }
                player.message("---- Swing ($id) -> ($targetId) -----")
            }
            if (!target.hasClock("under_attack")) {
                if (character is Player) {
                    CombatApi.start(character, target)
                } else if (character is NPC) {
                    CombatApi.start(character, target)
                }
            }
            target.start("under_attack", 8)
            if (character is NPC) {
                CombatApi.swing(character, target, character.fightStyle)
            } else if (character is Player) {
                val style = character.fightStyle
                if (style == "magic" || style == "blaze") {
                    if (Magic.castSpell(character, target)) {
                        CombatApi.swing(character, target, character.weapon.id, style)
                    }
                } else {
                    CombatApi.swing(character, target, character.weapon.id, style)
                }
            }
            (character as? Player)?.specialAttack = false
            var nextDelay = character.attackSpeed
            if (character.hasClock("miasmic") && (character.fightStyle == "range" || character.fightStyle == "melee")) {
                nextDelay *= 2
            }
            character.start("action_delay", nextDelay)
        }
    }
}
