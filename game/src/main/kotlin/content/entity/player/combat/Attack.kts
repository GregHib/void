package content.entity.player.combat

import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.player.characterApproachPlayer
import content.entity.combat.CombatInteraction
import content.skill.melee.weapon.attackRange
import content.entity.combat.combatPrepare
import content.entity.player.dialogue.type.statement
import content.skill.magic.spell.spell
import content.skill.melee.weapon.fightStyle
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.npc.npcApproachNPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

npcApproach("Attack") {
    if (player.equipped(EquipSlot.Weapon).id.endsWith("_greegree")) {
        statement("You cannot attack as a monkey.")
        cancel()
        return@npcApproach
    }
    if (target.id.endsWith("_dummy") && !handleCombatDummies()) {
        return@npcApproach
    }
    if (character.attackRange != 1) {
        approachRange(character.attackRange, update = false)
    } else {
        approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

npcApproachNPC("Attack") {
    if (character.attackRange != 1) {
        approachRange(character.attackRange, update = false)
    } else {
        approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

characterApproachPlayer("Attack") {
    if (character.attackRange != 1) {
        approachRange(character.attackRange, update = false)
    } else {
        approachRange(null, update = true)
    }
    combatInteraction(character, target)
}

itemOnNPCApproach(id = "*_spellbook") {
    approachRange(8, update = false)
    player.spell = component
    if (target.id.endsWith("_dummy") && !handleCombatDummies()) {
        player.clear("spell")
        return@itemOnNPCApproach
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

/**
 * Switch out the current Interaction with [CombatInteraction] to allow hits this tick
 */
fun combatInteraction(character: Character, target: Character) {
    val interact = character.mode as? Interact ?: return
    interact.updateInteraction(CombatInteraction(character, target))
}

suspend fun TargetInteraction<Player, NPC>.handleCombatDummies(): Boolean {
    val type = target.id.removeSuffix("_dummy")
    if (player.fightStyle == type) {
        return true
    }
    player.message("You can only use ${type.toTitleCase()} against this dummy.")
    approachRange(10, false)
    player.mode = EmptyMode
    cancel()
    return false
}