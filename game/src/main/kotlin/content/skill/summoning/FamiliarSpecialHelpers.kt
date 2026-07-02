package content.skill.summoning

import content.area.wilderness.inPvp
import content.entity.combat.Target
import content.entity.combat.hit.hit
import content.entity.proj.shoot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random

/**
 * Shared building blocks for the scroll-driven familiar special moves (the cast-button specials).
 * Each is meant to be called from inside a [castFamiliarSpecial] block and to return whether the
 * move actually happened, so the cast helper only charges a scroll/points on a real cast.
 *
 * Visual ids ([anim]/[sourceGfx]/[projectile]/[targetGfx]) are the named entries added to
 * `summoning_special.anims.toml` / `summoning_special.gfx.toml`, which in turn map to the numeric cache ids.
 */

/**
 * Validates [target] as a thing the current follower may attack (PvP rules for players,
 * single-combat rules for npcs), mirroring [commandFamiliarAttack]'s pre-checks. Returns false and
 * (unless [silent]) messages when the target is invalid.
 */
fun Player.familiarCanSpecial(target: Character, silent: Boolean = false): Boolean {
    val familiar = follower ?: return false
    if (familiar == target || this == target) {
        return false
    }
    if (!familiar.canFight()) {
        if (!silent) message("Your familiar cannot fight.")
        return false
    }
    when (target) {
        is Player -> if (!inPvp || !target.inPvp) {
            if (!silent) message("You can only attack players in a player-vs-player area.")
            return false
        }
        is NPC -> if (!Target.attackable(familiar, target, message = !silent)) {
            return false
        }
    }
    return true
}

/**
 * The standard combat special: the follower faces [target], plays its special visuals, optionally
 * fires a [projectile], deals a single hit of `random(0..maxHit)` of [type], then keeps fighting
 * the target (so the special doubles as an opening attack, as in the live game). Returns true on a
 * real cast; false (charging nothing) if the target is invalid.
 */
fun Player.familiarSpecialHit(
    target: Character,
    maxHit: Int,
    type: String = "magic",
    anim: String? = null,
    sourceGfx: String? = null,
    projectile: String? = null,
    targetGfx: String? = null,
    engage: Boolean = true,
): Boolean {
    if (!familiarCanSpecial(target)) {
        return false
    }
    val familiar = follower ?: return false
    familiar.watch(target)
    anim?.let { familiar.anim(it) }
    sourceGfx?.let { familiar.gfx(it) }
    // shoot returns the projectile's flight time (client ticks); land the hit and impact gfx when
    // it arrives rather than on hit()'s fixed magic delay, so damage lands with the projectile.
    val flight = projectile?.let { familiar.shoot(it, target) }
    val damage = random.nextInt(maxHit + 1)
    if (flight != null) {
        familiar.hit(target, offensiveType = type, damage = damage, delay = flight)
        targetGfx?.let { gfx -> target.queue("familiar_special_gfx", CLIENT_TICKS.toTicks(flight)) { target.gfx(gfx) } }
    } else {
        familiar.hit(target, offensiveType = type, damage = damage)
        targetGfx?.let { target.gfx(it) }
    }
    if (engage) {
        commandFamiliarAttack(target, silent = true)
    }
    return true
}

/**
 * An area special with no picked target: the follower hits up to [maxTargets] attackable npcs within
 * [radius] tiles of itself (Fireball Assault, Sandstorm). Each takes a single `random(0..maxHit)` hit
 * of [type], timed to the projectile flight when a [projectile] is given, and the familiar engages the
 * first of them. Returns false (charging nothing) when there is nothing valid nearby.
 */
fun Player.familiarAoeSpecial(
    maxTargets: Int,
    maxHit: Int,
    radius: Int = 1,
    type: String = "magic",
    anim: String? = null,
    sourceGfx: String? = null,
    projectile: String? = null,
    targetGfx: String? = null,
): Boolean {
    val familiar = follower ?: return false
    val targets = mutableListOf<NPC>()
    for (tile in familiar.tile.spiral(radius)) {
        for (character in NPCs.at(tile)) {
            if (character in targets || !familiarCanSpecial(character, silent = true)) {
                continue
            }
            targets.add(character)
            if (targets.size >= maxTargets) {
                break
            }
        }
        if (targets.size >= maxTargets) {
            break
        }
    }
    if (targets.isEmpty()) {
        message("There is nothing nearby for your familiar to attack.")
        return false
    }
    anim?.let { familiar.anim(it) }
    sourceGfx?.let { familiar.gfx(it) }
    for (target in targets) {
        val flight = projectile?.let { familiar.shoot(it, target) }
        val damage = random.nextInt(maxHit + 1)
        if (flight != null) {
            familiar.hit(target, offensiveType = type, damage = damage, delay = flight)
            targetGfx?.let { gfx -> target.queue("familiar_special_gfx", CLIENT_TICKS.toTicks(flight)) { target.gfx(gfx) } }
        } else {
            familiar.hit(target, offensiveType = type, damage = damage)
            targetGfx?.let { target.gfx(it) }
        }
    }
    commandFamiliarAttack(targets.first(), silent = true)
    return true
}

/**
 * A self/owner buff special with no target: plays the follower's special visuals and runs [effect].
 * Always counts as a successful cast.
 */
fun Player.familiarSelfSpecial(anim: String? = null, sourceGfx: String? = null, playerGfx: String? = null, effect: Player.() -> Unit): Boolean {
    val familiar = follower ?: return false
    anim?.let { familiar.anim(it) }
    sourceGfx?.let { familiar.gfx(it) }
    playerGfx?.let { gfx(it) }
    effect()
    return true
}

/**
 * Boosts [skill] by [amount] above the player's current max, not stacking past it - the common
 * shape of the stat-boost specials (Stony Shell, Testudo, Magic Focus, ...). Returns true.
 */
fun Player.familiarBoostSkill(skill: Skill, amount: Int): Boolean {
    levels.boost(skill, amount)
    return true
}
