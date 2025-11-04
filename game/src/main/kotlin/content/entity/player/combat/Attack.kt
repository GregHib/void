package content.entity.player.combat

import content.entity.combat.Combat
import content.entity.combat.CombatSwing
import content.entity.combat.combatPrepare
import content.entity.player.dialogue.type.statement
import content.skill.magic.spell.spell
import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.fightStyle
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Attack : Script {

    init {
        npcApproach("Attack") {
            val target = it.target
            if (!has(Skill.Slayer, target.def["slayer_level", 0])) {
                message("You need a higher slayer level to know how to wound this monster.")
                return@npcApproach
            }
            if (equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
                statement("You cannot attack as a monkey.")
                return@npcApproach
            }
            if (target.id.endsWith("_dummy") && !handleCombatDummies(target)) {
                return@npcApproach
            }
            if (attackRange != 1) {
                approachRange(attackRange, update = false)
            } else {
                approachRange(null, update = true)
            }
            it.combatInteraction(target)
        }

        npcApproach("Destroy", "door_support*") {
            if (attackRange != 1) {
                approachRange(attackRange, update = false)
            } else {
                approachRange(null, update = true)
            }
            it.combatInteraction(it.target)
        }

        npcApproachNPC("Attack") {
            if (attackRange != 1) {
                approachRange(attackRange, update = false)
            } else {
                approachRange(null, update = true)
            }
            it.combatInteraction(it.target)
        }

        playerApproach("Attack") {
            if (attackRange != 1) {
                approachRange(attackRange, update = false)
            } else {
                approachRange(null, update = true)
            }
            it.combatInteraction(it.target)
        }

        npcApproachPlayer("Attack") {
            if (attackRange != 1) {
                approachRange(attackRange, update = false)
            } else {
                approachRange(null, update = true)
            }
            it.combatInteraction(it.target)
        }

        onNPCApproach("*_spellbook:*") {
            val (target, id) = it
            if (!has(Skill.Slayer, target.def["slayer_level", 0])) {
                message("You need a higher slayer level to know how to wound this monster.")
                return@onNPCApproach
            }
            approachRange(8, update = false)
            spell = id.substringAfter(":")
            if (target.id.endsWith("_dummy") && !handleCombatDummies(target)) {
                clear("spell")
                return@onNPCApproach
            }
            set("attack_speed", 5)
            set("one_time", true)
            attackRange = 8
            face(target)
            it.combatInteraction(target)
        }

        combatPrepare { player ->
            if (player.contains("one_time")) {
                player.mode = EmptyMode
                player.clear("one_time")
            }
        }
    }

    /**
     * Replaces the current [Interact.override] when combat is triggered via [Interact] to
     * allow the first [CombatSwing] to occur on the same tick.
     * After [Interact] is complete it is switched to [CombatMovement]
     */
    fun Interact.combatInteraction(target: Character) {
        updateInteraction {
            Combat.combat(character, target)
        }
    }

    suspend fun Player.handleCombatDummies(target: NPC): Boolean {
        val type = target.id.removeSuffix("_dummy")
        if (fightStyle == type) {
            return true
        }
        message("You can only use ${type.toTitleCase()} against this dummy.")
        approachRange(10, false)
        mode = EmptyMode
        return false
    }

    companion object {
    }
}
