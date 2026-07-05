package content.skill.summoning

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Passive, invisible skill-level boosts a summoned familiar grants. These raise the *effective*
 * level used in skill success/yield checks only - they aren't shown in the skill tab, don't decay,
 * and never let the player perform an action above their real level. Because [familiarBoost] reads
 * the boost live from the active follower, the boost disappears the moment the familiar is
 * dismissed or killed (the follower is cleared in dismissFamiliar).
 *
 * Dreadfowl/compost mound's active "Special" Farming boost is a separate, visible mechanic.
 */
/** A familiar's invisible boost: a [flat] amount plus a [multiplier] of the player's base level. */
private data class Boost(val flat: Int, val multiplier: Double = 0.0)

private val FAMILIAR_BOOSTS: Map<String, Map<Skill, Boost>> = mapOf(
    "beaver_familiar" to mapOf(Skill.Woodcutting to Boost(2)),
    "granite_crab_familiar" to mapOf(Skill.Fishing to Boost(1)),
    "ibis_familiar" to mapOf(Skill.Fishing to Boost(3)),
    "granite_lobster_familiar" to mapOf(Skill.Fishing to Boost(4)),
    "arctic_bear_familiar" to mapOf(Skill.Hunter to Boost(7)),
    "wolpertinger_familiar" to mapOf(Skill.Hunter to Boost(5)),
    "spirit_graahk_familiar" to mapOf(Skill.Hunter to Boost(5)),
    "spirit_kyatt_familiar" to mapOf(Skill.Hunter to Boost(5)),
    "spirit_larupia_familiar" to mapOf(Skill.Hunter to Boost(5)),
    "desert_wyrm_familiar" to mapOf(Skill.Mining to Boost(1)),
    "void_ravager_familiar" to mapOf(Skill.Mining to Boost(1)),
    "obsidian_golem_familiar" to mapOf(Skill.Mining to Boost(7)),
    "lava_titan_familiar" to mapOf(Skill.Mining to Boost(10), Skill.Firemaking to Boost(10)),
    "pyrelord_familiar" to mapOf(Skill.Firemaking to Boost(3)),
    "forge_regent_familiar" to mapOf(Skill.Firemaking to Boost(4)),
    "magpie_familiar" to mapOf(Skill.Thieving to Boost(3)),
    "stranger_plant_familiar" to mapOf(Skill.Farming to Boost(1, 0.04)),
    "geyser_titan_familiar" to mapOf(Skill.Ranged to Boost(1, 0.03)),
)

/**
 * The invisible skill-level boost the player's currently-summoned familiar grants for [skill],
 * or 0 if the player has no familiar or it doesn't boost that skill. Add this to the visible level
 * in a skill's success check, e.g. `levels.get(Skill.Mining) + familiarBoost(Skill.Mining)`.
 */
fun Player.familiarBoost(skill: Skill): Int {
    val id = follower?.id ?: return 0
    val boost = FAMILIAR_BOOSTS[id]?.get(skill) ?: return 0
    return boost.flat + (levels.getMax(skill) * boost.multiplier).toInt()
}

/**
 * The ibis and granite lobster spear fish for their owner, standing in for a harpoon when fishing
 * tuna, swordfish or sharks.
 */
fun Player.familiarActsAsHarpoon(): Boolean = follower?.id == "ibis_familiar" || follower?.id == "granite_lobster_familiar"
