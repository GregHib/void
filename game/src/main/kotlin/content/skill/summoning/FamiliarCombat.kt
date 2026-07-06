package content.skill.summoning

import content.area.wilderness.inMultiCombat
import content.area.wilderness.inWilderness
import content.entity.combat.Target
import content.entity.combat.target
import content.entity.effect.clearTransform
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
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
 * Directs the player's familiar to attack [target]. Player targets follow the owner's own PvP
 * rules (wilderness or PvP area, combat-level bracket); against NPCs it fights solo (even in
 * single-way) but, like any attacker, can't share a target someone else is already fighting -
 * including its own owner - so single-combat rules in [Target.attackable] are pre-checked.
 * [silent] suppresses the rejection messages (auto-assist).
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
        // The owner's own PvP rules (wilderness or PvP area, level range) decide who the familiar
        // may be sent at.
        is Player -> if (!Target.attackable(this, target, message = !silent)) {
            return
        }
        // A familiar can't pile onto an NPC that's already under attack (e.g. one the owner is
        // fighting in single-way); it must be sent at its own, separate target.
        is NPC -> if (!Target.attackable(familiar, target, message = false)) {
            if (!silent) message("Your familiar can't attack that right now.")
            return
        }
    }
    // Drive the familiar with CombatMovement directly rather than the interact-then-combat path:
    // the interact approach gives up (cantReach -> EmptyMode) the moment a player or npc fully
    // blocks the route, whereas CombatMovement re-paths every tick and keeps pursuing, so the
    // familiar resumes as soon as the obstruction clears.
    familiar.mode = CombatMovement(familiar, target)
}

/**
 * Sends an idle combat familiar at [target]. No-op when the familiar is already fighting or
 * cannot fight; rejections are silent so auto-assist never spams the owner.
 */
fun Player.assistFamiliar(target: Character) {
    val familiar = follower ?: return
    // Familiars only auto-join the owner's fight in multi-combat. In single-way the owner's
    // target is theirs alone, so the familiar must be ordered at a separate NPC instead.
    if (!inMultiCombat) {
        return
    }
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

class FamiliarCombat :
    Script,
    CombatApi {

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

        // Re-acquire: each time the owner lands an attack, an idle familiar joins the owner's
        // current (most recently attacked) target. assistFamiliar's idle guard leaves an actively
        // fighting familiar alone, so it only picks a new target once its own is dead/gone.
        combatAttack { assistFamiliar(it.target) }

        variableSet("in_wilderness") { _, _, _ ->
            updateFamiliarPvpForm()
        }
    }
}
