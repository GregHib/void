package content.skill.summoning

import content.area.wilderness.inMultiCombat
import content.area.wilderness.inPvp
import content.area.wilderness.inWilderness
import content.entity.combat.target
import content.entity.effect.clearTransform
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get

/**
 * True when this NPC has a combat definition with at least one attack, i.e. it is a combat
 * familiar. Skill/passive familiars have no combat block and so cannot fight.
 */
fun NPC.canFight(): Boolean {
    val definitions: CombatDefinitions = get()
    return definitions.getOrNull(def["combat_def", id])?.attacks?.isNotEmpty() == true
}

/**
 * Directs the player's familiar to attack [target], enforcing authentic combat rules: familiars
 * may only attack players in PvP areas and NPCs in multi-combat zones. [silent] suppresses the
 * rejection messages (used by auto-assist).
 */
fun Player.commandFamiliarAttack(target: Character, silent: Boolean = false) {
    val familiar = follower ?: return
    if (familiar == target || this == target) {
        return
    }
    if (!familiar.canFight()) {
        if (!silent) message("Your familiar cannot fight.")
        return
    }
    when (target) {
        is Player -> if (!inPvp || !target.inPvp) {
            if (!silent) message("You can only attack players in a player-vs-player area.")
            return
        }
        // Familiars can only fight in multi-combat zones; in single-way combat the player can
        // still use the familiar (storage, foraging, specials) but it won't assist in the fight.
        is NPC -> if (!inMultiCombat || !target.inMultiCombat) {
            if (!silent) message("You can only use your familiar in a multi-zone area.")
            return
        }
    }
    when (target) {
        is NPC -> familiar.interactNpc(target, "Attack")
        is Player -> familiar.interactPlayer(target, "Attack")
    }
}

/**
 * Sends an idle combat familiar at [target]. No-op when the familiar is already fighting or
 * cannot fight; rejections are silent so auto-assist never spams the owner.
 */
fun Player.assistFamiliar(target: Character) {
    val familiar = follower ?: return
    if (familiar.target != null || familiar.mode is CombatMovement) {
        return
    }
    if (!familiar.canFight()) {
        return
    }
    commandFamiliarAttack(target, silent = true)
}

/**
 * Transforms a combat familiar to its PvP variant (npc id + 1, the form carrying the "Attack"
 * option) while in the wilderness so enemy players can target it, and reverts otherwise. Safe to
 * call on summon, call, and on entering/leaving the wilderness.
 */
fun Player.updateFamiliarPvpForm() {
    val familiar = follower ?: return
    if (!familiar.canFight()) {
        return
    }
    val variant = "${familiar.id}_combat"
    if (inWilderness && NPCDefinitions.contains(variant)) {
        if (familiar.transform != variant) {
            familiar.transform(variant)
        }
    } else if (familiar.transform != "") {
        familiar.clearTransform()
    }
}

class FamiliarCombat : Script, CombatApi {

    init {
        // Explicit command (cast-on-target): pick the attack option from the follower details
        // interface (662:65) or the summoning orb (component 14 / its left-click 21), then click an
        // NPC or player to send the familiar at it.
        onNPCApproach("familiar_details:attack", "*") { (target) ->
            approachRange(16)
            commandFamiliarAttack(target)
        }
        onNPCApproach("summoning_orb:*attack", "*") { (target) ->
            approachRange(16)
            commandFamiliarAttack(target)
        }
        onPlayerApproach("familiar_details:attack") { (target) ->
            approachRange(16)
            commandFamiliarAttack(target)
        }
        onPlayerApproach("summoning_orb:*attack") { (target) ->
            approachRange(16)
            commandFamiliarAttack(target)
        }

        // Offensive assist: when the owner attacks something, an idle combat familiar joins in.
        // Defensive assist: when the owner is attacked, the familiar turns on the attacker.
        combatStart { target ->
            assistFamiliar(target)
            if (target is Player) {
                target.assistFamiliar(this)
            }
        }
        npcCombatStart { target ->
            if (target is Player) {
                target.assistFamiliar(this)
            }
        }

        variableSet("in_wilderness") { _, _, _ ->
            updateFamiliarPvpForm()
        }
    }
}
