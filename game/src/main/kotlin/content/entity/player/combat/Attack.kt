package content.entity.player.combat

import content.entity.combat.Combat
import content.entity.combat.Combat.Companion.combat
import content.entity.combat.combatPrepare
import content.entity.player.dialogue.type.statement
import content.skill.magic.spell.spell
import content.skill.melee.weapon.attackRange
import content.skill.melee.weapon.fightStyle
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.interfaceOnNPCApproach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.InteractionType
import world.gregs.voidps.engine.entity.character.mode.interact.approachRange
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class Attack : Api {

    init {
        npcApproach("Attack") { player, target ->
            if (!player.has(Skill.Slayer, target.def["slayer_level", 0])) {
                player.message("You need a higher slayer level to know how to wound this monster.")
//                cancel()
                return@npcApproach
            }
            if (player.equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
                player.dialogue { statement("You cannot attack as a monkey.") }
//                cancel()
                return@npcApproach
            }
            if (target.id.endsWith("_dummy") && !handleCombatDummies(player, target)) {
                return@npcApproach
            }
            if (player.attackRange != 1) {
                player.approachRange(player.attackRange, update = false)
            } else {
                player.approachRange(null, update = true)
            }
            combatInteraction(player, target)
        }

        npcApproach("Destroy", "door_support*") { player, target ->
            if (player.attackRange != 1) {
                player.approachRange(player.attackRange, update = false)
            } else {
                player.approachRange(null, update = true)
            }
            combatInteraction(player, target)
        }

        npcApproachNpc("Attack") { npc, target ->
            if (npc.attackRange != 1) {
                npc.approachRange(npc.attackRange, update = false)
            } else {
                npc.approachRange(null, update = true)
            }
            combatInteraction(npc, target)
        }

        playerApproach("Attack") { player, target ->
            if (player.attackRange != 1) {
                player.approachRange(player.attackRange, update = false)
            } else {
                player.approachRange(null, update = true)
            }
            combatInteraction(player, target)
        }

        npcApproachPlayer("Attack") { npc, target ->
            if (npc.attackRange != 1) {
                npc.approachRange(npc.attackRange, update = false)
            } else {
                npc.approachRange(null, update = true)
            }
            combatInteraction(npc, target)
        }

        interfaceOnNPCApproach(id = "*_spellbook") {
            if (!player.has(Skill.Slayer, target.def["slayer_level", 0])) {
                player.message("You need a higher slayer level to know how to wound this monster.")
                cancel()
                return@interfaceOnNPCApproach
            }
            approachRange(8, update = false)
            player.spell = component
            if (target.id.endsWith("_dummy") && !handleCombatDummies(player, target)) {
                player.clear("spell")
                return@interfaceOnNPCApproach
            }
            player["attack_speed"] = 5
            player["one_time"] = true
            player.attackRange = 8
            player.face(target)
            combatInteraction(player, target)
            cancel()
        }

        combatPrepare { player ->
            if (player.contains("one_time")) {
                player.mode = EmptyMode
                player.clear("one_time")
            }
        }
    }

    /**
     * When triggered via [Interact] replace the Interaction with [Combat.callback]
     * to allow movement & [Interact] to complete and start [combat] on the same tick
     * After [Interact] is complete switch to using [CombatMovement]
     */
    fun combatInteraction(character: Character, target: Character) {
        val interact = character.mode as? Interact ?: return
        interact.updateInteraction(object : InteractionType {
            override fun hasOperate() = true

            override fun hasApproach() = true

            override fun operate() {
                Combat.callback.invoke(character, target)
            }

            override fun approach() {
                Combat.callback.invoke(character, target)
            }
        })
    }

    suspend fun handleCombatDummies(player: Player, target: NPC): Boolean {
        val type = target.id.removeSuffix("_dummy")
        if (player.fightStyle == type) {
            return true
        }
        player.message("You can only use ${type.toTitleCase()} against this dummy.")
        player.approachRange(10, false)
        player.mode = EmptyMode
//        cancel()
        return false
    }
}
