package content.skill.hunter

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Hunter skill — trap-based and direct-catch mechanics.
 *
 * Architecture overview
 * ─────────────────────
 * Traps (box trap, bird snare, net, boulder trap):
 *   1. Player uses itemOption("Lay") → consumes required items, spawns a temporary
 *      global object at the player's tile, stores the tile id in the player's
 *      "hunter_traps" Set<Int>.
 *   2. A nearby hunter creature NPC uses hunt_mode "hunter_trap" (configured in its
 *      npcs.toml).  huntObject fires when the engine finds a matching active-trap
 *      object.  The NPC stores the target tile/id, then a npcTimerTick resolves
 *      success or failure after a short walk delay.
 *   3. On success the active-trap object is replaced with the creature's caught-obj id.
 *      On failure it is replaced with the trap's fail-obj id.
 *   4. Player clicks the caught or failed object → objectOperate → collect loot / reclaim items.
 *
 * Butterflies:  npcOperate("Catch") with butterfly_net + butterfly_jar in inventory.
 * Implings:     npcOperate("Catch") bare-handed or "Catch with net" with butterfly_net.
 */
class Hunter : Script {

    // ── Lazy table maps, built once on first access ────────────────────────────

    /** Trap rows from [traps] table, keyed by row-id (e.g. "snare", "box_trap"). */
    private val trapRows by lazy {
        Tables.get("traps").rows().associateBy { it.rowId }
    }

    /** Creature rows from [creatures] table, keyed by row-id (creature name). */
    private val creatureRows by lazy {
        Tables.get("creatures").rows().associateBy { it.rowId }
    }

    /**
     * Map from the *active* trap object string-id → trap row-id.
     * E.g. "snare_trap" → "snare".
     */
    private val activeTrapObjToTrapId by lazy {
        buildMap<String, String> {
            for (trap in trapRows.values) {
                put(Tables.obj("traps.${trap.rowId}.trap"), trap.rowId)
            }
        }
    }

    /**
     * Map from the *caught* object string-id → creature row-id.
     * E.g. "snare_crimson_swift" → "crimson_swift".
     */
    private val caughtObjToCreatureId by lazy {
        buildMap<String, String> {
            for (creature in creatureRows.values) {
                put(Tables.obj("creatures.${creature.rowId}.caught_obj"), creature.rowId)
            }
        }
    }

    // ── Max concurrent traps: 1 + level/20, cap 5 ─────────────────────────────

    private fun maxTraps(level: Int) = (1 + level / 20).coerceAtMost(5)

    init {

        // ══════════════════════════════════════════════════════════════════════
        // 1.  LAYING TRAPS
        //     Each trap type registers its primary item's "Lay" option.
        // ══════════════════════════════════════════════════════════════════════

        for ((trapId, trap) in trapRows.entries) {
            val items = trap.itemList("items")
            val primaryItem = items.firstOrNull() ?: continue

            itemOption("Lay", primaryItem) {
                val level = levels.get(Skill.Hunter)
                val required = trap.int("level")

                if (!has(Skill.Hunter, required, message = true)) return@itemOption

                val placedTiles = getOrPut("hunter_traps") { mutableSetOf<Int>() }
                if (placedTiles.size >= maxTraps(level)) {
                    message("You cannot place more than ${maxTraps(level)} traps at once.")
                    return@itemOption
                }

                for (item in items) {
                    if (!carriesItem(item)) {
                        message("You need ${item.toLowerSpaceCase()} to lay this trap.")
                        return@itemOption
                    }
                }

                arriveDelay()
                anim(trap.anim("setup_anim"))
                delay(3)

                for (item in items) {
                    inventory.remove(item)
                }

                val activeObjId = Tables.obj("traps.$trapId.trap")
                GameObjects.add(activeObjId, tile, ObjectShape.CENTRE_PIECE_STRAIGHT, 0, ticks = 50 * 60)
                placedTiles.add(tile.id)
                message("You set up the ${trapId.toLowerSpaceCase()}.")
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // 2.  DISMANTLING a waiting (active, uncaught) trap
        // ══════════════════════════════════════════════════════════════════════

        for ((activeObjId, trapId) in activeTrapObjToTrapId) {
            val trap = trapRows[trapId] ?: continue
            objectOperate("Dismantle", activeObjId) { (target) ->
                val placedTiles = get<MutableSet<Int>>("hunter_traps")
                if (placedTiles == null || !placedTiles.contains(target.tile.id)) {
                    message("This is not your trap.")
                    return@objectOperate
                }
                anim(trap.anim("take_down_anim"))
                delay(2)
                GameObjects.remove(target)
                placedTiles.remove(target.tile.id)
                for (item in trap.itemList("items")) {
                    inventory.add(item)
                }
                message("You dismantle the trap and retrieve your equipment.")
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // 3a. INVESTIGATING a failed (collapsed) trap
        // ══════════════════════════════════════════════════════════════════════

        for ((trapId, trap) in trapRows) {
            val failObjId = Tables.objOrNull("traps.$trapId.fail") ?: continue
            objectOperate("Investigate", failObjId) { (target) ->
                val placedTiles = get<MutableSet<Int>>("hunter_traps")
                if (placedTiles == null || !placedTiles.contains(target.tile.id)) {
                    message("This is not your trap.")
                    return@objectOperate
                }
                anim(trap.anim("take_down_anim"))
                delay(2)
                GameObjects.remove(target)
                placedTiles.remove(target.tile.id)
                for (item in trap.itemList("items")) {
                    inventory.add(item)
                }
                message("Your trap collapsed. You retrieve what you can.")
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // 3b. CHECKING a triggered (creature-caught) trap
        // ══════════════════════════════════════════════════════════════════════

        for ((caughtObjId, creatureId) in caughtObjToCreatureId) {
            val creature = creatureRows[creatureId] ?: continue
            val trapId = creature.string("trap")
            val trap = trapRows[trapId] ?: continue

            objectOperate("Check", caughtObjId) { (target) ->
                val placedTiles = get<MutableSet<Int>>("hunter_traps")
                if (placedTiles == null || !placedTiles.contains(target.tile.id)) {
                    message("This is not your trap.")
                    return@objectOperate
                }
                anim(trap.anim("take_down_anim"))
                delay(2)
                GameObjects.remove(target)
                placedTiles.remove(target.tile.id)
                for (item in trap.itemList("items")) {
                    inventory.add(item)
                }
                for (item in creature.itemList("loot")) {
                    inventory.add(item)
                }
                exp(Skill.Hunter, creature.int("xp") / 10.0)
                message("You've caught a ${creatureId.toLowerSpaceCase()}!")
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // 4.  NPC HUNT — creature approaches a set trap
        //
        //     NPCs that hunt with mode "hunter_trap" (set in npcs.toml) will
        //     have huntObject fire when the engine detects a matching trap object
        //     within hunt_range.  The NPC records the target then uses a
        //     soft-timer to resolve success/fail after it has walked over.
        // ══════════════════════════════════════════════════════════════════════

        huntObject("hunter_trap") { trapObj ->
            // Guard: only engage if we don't already have a target
            if (get<Int>("hunter_trap_tile") != null) return@huntObject
            set("hunter_trap_tile", trapObj.tile.id)
            set("hunter_trap_obj", trapObj.id)
            softTimers.start("hunter_capture")
        }

        npcTimerStart("hunter_capture") { _ -> 7 }

        npcTimerTick("hunter_capture") {
            val trapTileId = get<Int>("hunter_trap_tile") ?: return@npcTimerTick Timer.CANCEL
            val trapObjId = get<String>("hunter_trap_obj") ?: return@npcTimerTick Timer.CANCEL
            val trapTile = Tile(trapTileId)

            val trapObj = GameObjects.findOrNull(trapTile, trapObjId)
            if (trapObj == null) {
                clear("hunter_trap_tile")
                clear("hunter_trap_obj")
                return@npcTimerTick Timer.CANCEL
            }

            // Find the player who owns this trap tile
            var owner = Players.firstOrNull { p ->
                p.get<MutableSet<Int>>("hunter_traps")?.contains(trapTileId) == true
            }
            if (owner == null) {
                clear("hunter_trap_tile")
                clear("hunter_trap_obj")
                return@npcTimerTick Timer.CANCEL
            }

            val creature = creatureRows[id] ?: return@npcTimerTick Timer.CANCEL
            val hunterLevel = owner.levels.get(Skill.Hunter)
            val lureLevel = creature.int("level")

            // Catch chance scales from ~10% at minimum level to ~90% at high levels
            val catchChance = ((hunterLevel.toDouble() / lureLevel) * 50).toInt().coerceIn(10, 90)
            val success = random.nextInt(100) < catchChance

            val catchAnimId = creature.animOrNull("catch_anim")
            if (catchAnimId != null) anim(catchAnimId)

            if (success) {
                val caughtObjId = Tables.obj("creatures.$id.caught_obj")
                GameObjects.replace(trapObj, caughtObjId)
                owner.message("Something has been caught in your trap!")
            } else {
                val trapId = creature.string("trap")
                val failObjId = Tables.objOrNull("traps.$trapId.fail")
                if (failObjId != null) {
                    GameObjects.replace(trapObj, failObjId)
                } else {
                    GameObjects.remove(trapObj)
                    owner.get<MutableSet<Int>>("hunter_traps")?.remove(trapTileId)
                }
                val failAnimId = creature.animOrNull("fail_anim")
                if (failAnimId != null) anim(failAnimId)
                owner.message("Your trap has been disturbed.")
            }

            clear("hunter_trap_tile")
            clear("hunter_trap_obj")
            Timer.CANCEL
        }

        // ══════════════════════════════════════════════════════════════════════
        // 5.  BUTTERFLY CATCHING
        // ══════════════════════════════════════════════════════════════════════

        npcOperate("Catch", "ruby_harvest,sapphire_glacialis,snowy_knight,black_warlock") { (target) ->
            val row = Tables.get("butterflies").rows().firstOrNull {
                Tables.npc("butterflies.${it.rowId}.npc") == target.id
            } ?: return@npcOperate
            val level = row.int("level")
            if (!has(Skill.Hunter, level, message = true)) return@npcOperate
            if (!carriesItem("butterfly_net")) {
                message("You need a butterfly net to catch butterflies.")
                return@npcOperate
            }
            if (!carriesItem("butterfly_jar")) {
                message("You need an empty butterfly jar.")
                return@npcOperate
            }
            anim("butterfly_catch")
            delay(2)
            inventory.remove("butterfly_jar")
            inventory.add(row.item("jar"))
            exp(Skill.Hunter, row.int("xp") / 10.0)
            message("You catch the ${target.id.toLowerSpaceCase()} and place it in a jar.")
        }

        // ══════════════════════════════════════════════════════════════════════
        // 6.  IMPLING CATCHING
        // ══════════════════════════════════════════════════════════════════════

        npcOperate("Catch", "*_impling") { (target) ->
            val row = Tables.get("implings").rows().firstOrNull {
                Tables.npc("implings.${it.rowId}.npc") == target.id
            } ?: return@npcOperate
            val level = row.int("level")
            if (!has(Skill.Hunter, level, message = true)) return@npcOperate
            if (!carriesItem("butterfly_jar")) {
                message("You need an empty jar to catch an impling.")
                return@npcOperate
            }
            anim("butterfly_catch")
            delay(2)
            inventory.remove("butterfly_jar")
            inventory.add(row.item("jar"))
            exp(Skill.Hunter, row.int("xp") / 10.0)
            message("You catch the ${target.id.toLowerSpaceCase()}!")
        }

        npcOperate("Catch with net", "*_impling") { (target) ->
            val row = Tables.get("implings").rows().firstOrNull {
                Tables.npc("implings.${it.rowId}.npc") == target.id
            } ?: return@npcOperate
            // Net reduces effective level requirement by 10
            val level = (row.int("level") - 10).coerceAtLeast(1)
            if (!has(Skill.Hunter, level, message = true)) return@npcOperate
            if (!carriesItem("butterfly_net")) {
                message("You need a butterfly net.")
                return@npcOperate
            }
            if (!carriesItem("butterfly_jar")) {
                message("You need an empty jar to catch an impling.")
                return@npcOperate
            }
            anim("butterfly_catch")
            delay(2)
            inventory.remove("butterfly_jar")
            inventory.add(row.item("jar"))
            exp(Skill.Hunter, row.int("xp") / 10.0)
            message("You catch the ${target.id.toLowerSpaceCase()}!")
        }
    }
}
