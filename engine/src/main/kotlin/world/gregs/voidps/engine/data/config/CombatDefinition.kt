package world.gregs.voidps.engine.data.config

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
 *  - Accuracy is rolled and [targetHit] calculated
 *
 * 3. Impact
 *  - 'Miss' visuals are sent if accuracy roll was unsuccessful
 *  - Target visuals (animation, gfx, sounds) are sent when applying hit
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
 *  @param areaGfx played on the tile of the attacking npc when the swing is executed.
 *  @param sound played to the attacker when the swing is executed.
 *  @param areaSound played on the tile of the attacking npc when the swing is executed.
 *  @param style Combat style used by the attacking (e.g. "crush", "range", "magic").
 *  @param projectiles Projectiles fired by the attacking npc.
 *  === Target ===
 *  @param targetGfx gfx played on the target when the swing is executed.
 *  @param targetAnim animation played on the target when the swing is executed.
 *  @param targetSounds sounds played on the target when the swing is executed.
 *  @param targetHit damage hit queued when the swing is executed.
 *  == Impact ==
 *  @param impactAnim animation played on the target when the [targetHit] impacts.
 *  @param missGfx gfx played on the target if the [targetHit] was not successful.
 *  @param impactGfx gfx played on the target [targetHit] after the hit's delay.
 *  @param impactAreaGfx gfx played at the target location after the hit's delay.
 *  @param missSound sound played to the target if the [targetHit] was not successful.
 *  @param impactSound sound played to the target [targetHit] after the hit's delay.
 *  @param impactAreaSound sound played at the target location after the hit's delay.
 *  == Effects ==
 *  @param drainSkills list of skills drained by the attacking npc after the hit's delay.
 *  @param freeze duration of a freeze applied to target after the hit's delay.
 *  @param poison poison damage applied to target after the hit's delay.
 *  @param message message sent to the target after the hit's delay.
 */
data class CombatDefinition(
    val npc: String,
    val id: String,
    // Selection
    val chance: Int,
    val range: Int = 1,
    val condition: String = "",
    // Execution
    val anim: String = "",
    val gfx: String = "",
    val areaGfx: AreaGfx? = null,
    val sound: String = "",
    val areaSound: AreaSound? = null,
    val style: String = "",
    val projectiles: List<Projectile> = emptyList(),
    // Target
    val targetGfx: String = "",
    val targetAnim: String = "",
    val targetSounds: List<String> = emptyList(),
    val targetHit: Hit? = null,
    // Impact
    val impactAnim: String = "",
    val missGfx: String = "",
    val impactGfx: String = "",
    val impactAreaGfx: AreaGfx? = null,
    val missSound: String = "",
    val impactSound: String = "",
    val impactAreaSound: AreaSound? = null,
    // Effects
    val drainSkills: List<Drain> = emptyList(),
    val freeze: Int = 0,
    val poison: Int = 0,
    val message: String = "",
) {
    /**
     * @param offense offensiveType
     * @param defence defensiveType
     * @param spell id of the spell used.
     * @param special whether hit is from a special attack
     * @param min minimum damage that can be dealt by this hit.
     * @param max maximum damage that can be dealt by this hit.
     */
    data class Hit(val offense: String = "", val defence: String = "", val spell: String = "", val special: Boolean = false, val min: Int = 0, val max: Int = 0)

    data class Projectile(val id: String, val delay: Int? = null, val curve: Int? = null, val endHeight: Int? = null)

    /**
     * @param skill skill to drain (e.g. "Attack", "Hunter", "all")
     * @param amount number of levels to drain
     * @param multiplier multiplier applied to drained skill
     */
    data class Drain(val skill: String, val amount: Int = 0, val multiplier: Double = 0.0)

    data class AreaSound(val id: String, val delay: Int = 0, val radius: Int = 0)

    data class AreaGfx(val id: String, val delay: Int = 0)
}