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
private val FAMILIAR_BOOSTS: Map<String, Map<Skill, Int>> = mapOf(
    "beaver_familiar" to mapOf(Skill.Woodcutting to 2),
    "granite_crab_familiar" to mapOf(Skill.Fishing to 1),
    "ibis_familiar" to mapOf(Skill.Fishing to 3),
    "granite_lobster_familiar" to mapOf(Skill.Fishing to 4),
    "arctic_bear_familiar" to mapOf(Skill.Hunter to 7),
    "wolpertinger_familiar" to mapOf(Skill.Hunter to 5),
    "spirit_kyatt_familiar" to mapOf(Skill.Hunter to 5),
    "spirit_larupia_familiar" to mapOf(Skill.Hunter to 5),
    "desert_wyrm_familiar" to mapOf(Skill.Mining to 1),
    "void_ravager_familiar" to mapOf(Skill.Mining to 1),
    "obsidian_golem_familiar" to mapOf(Skill.Mining to 7),
    "lava_titan_familiar" to mapOf(Skill.Mining to 10, Skill.Firemaking to 10),
    "pyrelord_familiar" to mapOf(Skill.Firemaking to 3),
    "forge_regent_familiar" to mapOf(Skill.Firemaking to 4),
    "magpie_familiar" to mapOf(Skill.Thieving to 3),
)

/**
 * The invisible skill-level boost the player's currently-summoned familiar grants for [skill],
 * or 0 if the player has no familiar or it doesn't boost that skill. Add this to the visible level
 * in a skill's success check, e.g. `levels.get(Skill.Mining) + familiarBoost(Skill.Mining)`.
 */
fun Player.familiarBoost(skill: Skill): Int {
    val id = follower?.id ?: return 0
    return FAMILIAR_BOOSTS[id]?.get(skill) ?: 0
}
