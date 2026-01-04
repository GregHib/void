package world.gregs.voidps.engine.data.config

import world.gregs.voidps.type.random
import kotlin.ranges.random

data class CombatDefinition(
    val npc: String = "",
    val attackSpeed: Int = 4,
    val attackRange: Int = 1,
    val retreatRange: Int = 8,
    val defendAnim: String = "",
    val defendSound: CombatSound? = null,
    val deathAnim: String = "",
    val deathSound: CombatSound? = null,
    val attacks: Map<String, CombatAttack> = mutableMapOf(),
) {
    /**
     * Defines a single combat swing for an NPC.
     *
     * A swing represents one complete attack turn made up of three stages:
     * 1. Selection
     *  - Eligible swings are filtered by [range] and [condition]
     *  - [chance] is used as a weight to randomly select one swing
     *
     * 2. Execution
     *  - Source and target visuals (animation, gfx, sounds, projectiles) are sent
     *  - If any projectiles are present, impact is called after projectile delay
     *  - Accuracy is rolled and [targetHits] calculated
     *
     * 3. Impact
     *  - 'Miss' visuals are sent if accuracy roll was unsuccessful
     *  - Impact visuals (animation, gfx, sounds) are sent when applying hit to target
     *  - Optional effects (poison, frozen, skill draining) can be applied
     *
     *  @param npc this swing belongs to (e.g. "dark_wizard")
     *  @param id unique id per [npc]
     *  == Selection ==
     *  @param chance weight for this swing. Note: relative weight, not a percentage.
     *  @param range maximum range (in tiles) at which this swing may be selected.
     *  @param condition optional check performed during filtering
     *  == Execution ==
     *  @param anim played by the attacking npc when the swing is executed.
     *  @param gfx played by the attacking npc when the swing is executed.
     *  @param sounds played to the attacker when the swing is executed.
     *  @param projectiles Projectiles fired by the attacking npc.
     *  === Target ===
     *  @param targetGfx gfx played on the target when the swing is executed.
     *  @param targetAnim animation played on the target when the swing is executed.
     *  @param targetSounds sounds played on the target when the swing is executed.
     *  @param targetHits damage hit queued when the swing is executed.
     *  @param targetMultiple attack more than one target at once.
     *  == Impact ==
     *  @param impactAnim animation played on the target when the [targetHits] impacts.
     *  @param impactGfx gfx played on the target [targetHits] after the hit's delay.
     *  @param impactSounds sound played to the target [targetHits] after the hit's delay.
     *  @param missGfx gfx played on the target if the [targetHits] was not successful.
     *  @param missSounds sound played to the target if the [targetHits] was not successful.
     *  == Effects ==
     *  @param impactRegardless Apply impact gfx/sounds/effects even if no damage was dealt (usually considered a miss).
     *  @param impactDrainSkills list of skills drained by the attacking npc after the hit's delay.
     *  @param impactFreeze duration of a freeze applied to target after the hit's delay.
     *  @param impactPoison poison damage applied to target after the hit's delay.
     *  @param impactMessage message sent to the target after the hit's delay.
     */
    data class CombatAttack(
        val id: String = "",
        // Selection
        val chance: Int,
        val range: Int = 1,
        val condition: String = "",
        // Execution
        val anim: String = "",
        val gfx: List<CombatGfx> = emptyList(),
        val sounds: List<CombatSound> = emptyList(),
        val projectileOrigin: Origin = Origin.Entity,
        val projectiles: List<Projectile> = emptyList(),
        // Target
        val targetAnim: String = "",
        val targetGfx: List<CombatGfx> = emptyList(),
        val targetSounds: List<CombatSound> = emptyList(),
        val targetHits: List<CombatHit> = emptyList(),
        val targetMultiple: Boolean = false,
        // Impact
        val impactAnim: String = "",
        val impactGfx: List<CombatGfx> = emptyList(),
        val impactSounds: List<CombatSound> = emptyList(),
        val missGfx: List<CombatGfx> = emptyList(),
        val missSounds: List<CombatSound> = emptyList(),
        // Effects
        val impactRegardless: Boolean = false,
        val impactDrainSkills: List<Drain> = emptyList(),
        val impactFreeze: Int = 0,
        val impactPoison: Int = 0,
        val impactMessage: String = "",
    )

    /**
     * @param offense offensiveType (e.g. "crush", "range", "magic")
     * @param defence defensiveType (e.g. "dragonfire", "typeless_crush", "stab")
     * @param spell id of the spell used.
     * @param special whether hit is from a special attack
     * @param min minimum damage that can be dealt by this hit.
     * @param max maximum damage that can be dealt by this hit.
     */
    data class CombatHit(
        val offense: String = "",
        val defence: String = "",
        val spell: String = "",
        val special: Boolean = false,
        val min: Int = 0,
        val max: Int = 0,
    )

    data class Projectile(val id: String, val delay: Int? = null, val curve: Int? = null, val endHeight: Int? = null)

    enum class Origin {
        Entity,
        Tile,
        Centre,
    }

    /**
     * @param skill skill to drain (e.g. "Attack", "Hunter", "all")
     * @param min number of levels to drain
     * @param max number of levels to drain
     * @param multiplier multiplier applied to drained skill
     */
    data class Drain(val skill: String, val min: Int = 0, val max: Int = 0, val multiplier: Double = 0.0) {
        val amount: Int get() = (min..max).random(random)
    }

    data class CombatSound(val id: String, val delay: Int = 0, val radius: Int = 0)

    data class CombatGfx(val id: String, val delay: Int? = null, val area: Boolean = false)
}