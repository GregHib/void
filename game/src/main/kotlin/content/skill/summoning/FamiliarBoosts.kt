package content.skill.summoning

import world.gregs.voidps.engine.data.definition.Rows
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
/**
 * The invisible skill-level boost the player's currently-summoned familiar grants for [skill],
 * or 0 if the player has no familiar or it doesn't boost that skill - a row of the
 * `familiar_boosts` table (see `familiar_boosts.tables.toml`). Add this to the visible level
 * in a skill's success check, e.g. `levels.get(Skill.Mining) + familiarBoost(Skill.Mining)`.
 */
fun Player.familiarBoost(skill: Skill): Int {
    val id = follower?.id ?: return 0
    val row = Rows.getOrNull("familiar_boosts.$id") ?: return 0
    if (skill.name !in row.stringList("skills")) {
        return 0
    }
    return row.int("flat") + levels.getMax(skill) * row.int("multiplier_percent") / 100
}

/**
 * The ibis and granite lobster spear fish for their owner, standing in for a harpoon when fishing
 * tuna, swordfish or sharks.
 */
fun Player.familiarActsAsHarpoon(): Boolean = follower?.id == "ibis_familiar" || follower?.id == "granite_lobster_familiar"

/**
 * Style-gated passive defence: while summoned, the iron/steel titan make their owner 10%/15%
 * harder to hit with melee, and the wolpertinger 5% harder to hit with magic. Applied as a
 * multiplier to the owner's defensive rating for an incoming attack of [type].
 */
fun Player.familiarDefenceMultiplier(type: String, melee: Boolean): Double = when (follower?.id) {
    "iron_titan_familiar" -> if (melee) 1.10 else 1.0
    "steel_titan_familiar" -> if (melee) 1.15 else 1.0
    "wolpertinger_familiar" -> if (type == "magic") 1.05 else 1.0
    else -> 1.0
}
