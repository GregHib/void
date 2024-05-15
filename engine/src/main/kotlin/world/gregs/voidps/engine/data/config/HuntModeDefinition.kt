package world.gregs.voidps.engine.data.config

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.obj.ObjectLayer

/**
 * @param type player, npc, object, or floor_item
 * @param checkVisual line_of_sight, line_of_walk, or none
 * @param checkNotTooStrong check that the targets combat level difference isn't too large
 * @param checkNotCombat checks if target is already in combat
 * @param checkNotCombatSelf checks if npc is in combat
 * @param checkNotBusy checks target doesn't have menu open
 * @param checkAfk check if target is "afk" aka has tolerance by staying in the same area for 10 minutes (dark beasts don't check)
 * @param findKeepHunting unknown
 * @param pauseIfNobodyNear stop finding new target when no players are around
 * @param rate ticks between checking for new targets. Non-player targets have min 3 ticks.
 * @param id the id for object or floor item target
 * @param layer the [ObjectLayer] for object targets
 * @param maxMultiAttackers maximum number of attackers the target can have (custom)
 */
data class HuntModeDefinition(
    val type: String,
    val checkVisual: String = "none",
    val checkNotTooStrong: Boolean = type == "player",
    val checkNotCombat: Boolean = true,
    val checkNotCombatSelf: Boolean = true,
    val checkNotBusy: Boolean = true,
    val checkAfk: Boolean = type == "player",
    val findKeepHunting: Boolean = false,
    val pauseIfNobodyNear: Boolean = true,
    val rate: Int = if (type == "player") 1 else 3,
    val id: String? = null,
    val layer: Int = -1,
    val maxMultiAttackers: Int = 2,
) {
    var filter: ((Entity) -> Boolean)? = null

    init {
        if (type != "player") {
            check(rate >= 3) { "$type hunt rates must be more frequent than 3 ticks." }
        }
        if (type == "object") {
            check(layer != -1) { "Objects require an object layer." }
        }
        if (checkNotTooStrong) {
            check(type == "player") { "Shouldn't compare combat levels with types other than player." }
        }
        if (layer != -1) {
            check(type == "object") { "Only object hunt types should have an object layer." }
        }
    }

    companion object {
        val EMPTY = HuntModeDefinition("")

        fun fromMap(map: Map<String, Any>) = HuntModeDefinition(
            type = map["type"] as String,
            checkVisual = map["check_visual"] as? String ?: EMPTY.checkVisual,
            checkNotTooStrong = map["check_not_too_strong"] as? Boolean ?: EMPTY.checkNotTooStrong,
            checkNotCombat = map["check_not_combat"] as? Boolean ?: EMPTY.checkNotCombat,
            checkNotCombatSelf = map["check_not_combat_self"] as? Boolean ?: EMPTY.checkNotCombatSelf,
            checkNotBusy = map["check_not_busy"] as? Boolean ?: EMPTY.checkNotBusy,
            checkAfk = map["check_afk"] as? Boolean ?: EMPTY.checkAfk,
            findKeepHunting = map["find_keep_hunting"] as? Boolean ?: EMPTY.findKeepHunting,
            pauseIfNobodyNear = map["pause_if_nobody_near"] as? Boolean ?: EMPTY.pauseIfNobodyNear,
            id = map["id"] as? String ?: EMPTY.id,
            rate = map["rate"] as? Int ?: EMPTY.rate,
            maxMultiAttackers = map["max_multi_attackers"] as? Int ?: EMPTY.maxMultiAttackers,
        )
    }
}