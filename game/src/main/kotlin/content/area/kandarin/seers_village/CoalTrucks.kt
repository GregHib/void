package content.area.kandarin.seers_village

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

private const val COAL_TRUCK_DISPLAY_KEY = "coal_truck_coal"
private const val COAL_TRUCK_COUNT = "coal_truck_coal_count"
private const val COAL_TRUCK_DEFAULT_CAPACITY = 120
private const val COAL_TRUCK_HEADBAND_1_CAPACITY = 140
private const val COAL_TRUCK_HEADBAND_2_CAPACITY = 168
private const val COAL_TRUCK_HEADBAND_3_CAPACITY = 196
private const val COAL_TRUCK_HEADBAND_4_CAPACITY = 200
private const val COAL_TRUCK_DISPLAY_SOME = 1
private const val COAL_TRUCK_DISPLAY_ALMOST_HALF = 40
private const val COAL_TRUCK_DISPLAY_NEARLY_FULL = 80
private const val COAL_TRUCK_DISPLAY_FULL = 140
private const val COAL_TRUCK_DISPLAY_IMPRESSIVELY_STACKED = 168
private const val COAL_TRUCK_DISPLAY_MAXIMUM_STACK = 196
private const val ACTIVE_COAL_TRUCKS =
    "coal_truck,coal_truck_with_some_coal,coal_truck_almost_half_full,coal_truck_nearly_full,coal_truck_full,coal_truck_impressively_stacked,coal_truck_maximum_stack"

private enum class CoalTruckTier(val capacity: Int, val headbandId: String? = null) {
    Base(COAL_TRUCK_DEFAULT_CAPACITY),
    Easy(COAL_TRUCK_HEADBAND_1_CAPACITY, "seers_headband_1"),
    Medium(COAL_TRUCK_HEADBAND_2_CAPACITY, "seers_headband_2"),
    Hard(COAL_TRUCK_HEADBAND_3_CAPACITY, "seers_headband_3"),
    Elite(COAL_TRUCK_HEADBAND_4_CAPACITY, "seers_headband_4"),
}

class CoalTrucks : Script {

    init {
        playerSpawn {
            normaliseCoalTruckState()
        }

        itemOnObjectOperate("coal", ACTIVE_COAL_TRUCKS) {
            val stored = coalTruckCount()
            val capacity = coalTruckCapacity()
            val space = capacity - stored
            if (space <= 0) {
                message("The coal truck is full.")
                return@itemOnObjectOperate
            }

            val amount = inventory.count("coal").coerceAtMost(space)
            if (amount <= 0 || !inventory.remove("coal", amount)) {
                return@itemOnObjectOperate
            }
            val total = stored + amount
            storeCoalTruckCount(total)
            sound("coalfill")
            message("You put the coal in the truck.")
            if (total >= capacity) {
                message("The coal truck is now full.")
            }
        }

        objectOperate("Investigate", ACTIVE_COAL_TRUCKS) {
            val stored = coalTruckCount()
            if (stored <= 0) {
                message("There is nothing in this coal truck.")
                return@objectOperate
            }
            if (stored == 1) {
                message("The truck contains one piece of coal.")
                return@objectOperate
            }
            message("The truck contains $stored pieces of coal.")
        }

        objectOperate("Remove-coal", ACTIVE_COAL_TRUCKS) {
            val stored = coalTruckCount()
            if (stored <= 0) {
                message("The coal truck is empty.")
                return@objectOperate
            }
            val amount = minOf(stored, inventory.spaces)
            if (!inventory.add("coal", amount)) {
                message("You do not have enough space in your backpack!")
                return@objectOperate
            }

            val remaining = stored - amount
            storeCoalTruckCount(remaining)
            sound("coalfill")
            if (remaining == 0) {
                message("The truck is now empty.")
                return@objectOperate
            }
            message("You remove some of the coal from the truck.")
        }
    }

    private fun Player.normaliseCoalTruckState(): Int {
        val display = get(COAL_TRUCK_DISPLAY_KEY, 0)
        val count = get(COAL_TRUCK_COUNT, 0)
        val stored = when (count) {
            0 -> legacyCoalTruckCount(display)
            else -> count.coerceIn(0, COAL_TRUCK_HEADBAND_4_CAPACITY)
        }
        val transformed = coalTruckDisplayValue(stored, coalTruckCapacity())
        if (stored != count) {
            set(COAL_TRUCK_COUNT, stored)
        }
        if (transformed != display) {
            set(COAL_TRUCK_DISPLAY_KEY, transformed)
        }
        return stored
    }

    private fun Player.coalTruckCount(): Int = normaliseCoalTruckState()

    private fun Player.storeCoalTruckCount(stored: Int) {
        val amount = stored.coerceIn(0, COAL_TRUCK_HEADBAND_4_CAPACITY)
        set(COAL_TRUCK_COUNT, amount)
        syncCoalTruckDisplay(amount)
    }

    private fun Player.syncCoalTruckDisplay(stored: Int) {
        set(COAL_TRUCK_DISPLAY_KEY, coalTruckDisplayValue(stored, coalTruckCapacity()))
    }

    private fun Player.coalTruckCapacity(): Int = coalTruckTier().capacity

    private fun Player.coalTruckTier(): CoalTruckTier = when {
        hasCoalTruckTier(CoalTruckTier.Elite) -> CoalTruckTier.Elite
        hasCoalTruckTier(CoalTruckTier.Hard) -> CoalTruckTier.Hard
        hasCoalTruckTier(CoalTruckTier.Medium) -> CoalTruckTier.Medium
        hasCoalTruckTier(CoalTruckTier.Easy) -> CoalTruckTier.Easy
        else -> CoalTruckTier.Base
    }

    private fun Player.hasCoalTruckTier(tier: CoalTruckTier): Boolean {
        val headband = tier.headbandId ?: return false
        return equipped(EquipSlot.Hat).id == headband
    }
    // Future diary gate:
    // return equipped(EquipSlot.Hat).id == headband && hasCoalTruckDiaryUnlock(tier)
    //
    // private fun Player.hasCoalTruckDiaryUnlock(tier: CoalTruckTier): Boolean = when (tier) {
    //     CoalTruckTier.Base -> true
    //     CoalTruckTier.Easy -> get("seers_easy_diary_complete", false)
    //     CoalTruckTier.Medium -> get("seers_medium_diary_complete", false)
    //     CoalTruckTier.Hard -> get("seers_hard_diary_complete", false)
    //     CoalTruckTier.Elite -> get("seers_elite_diary_complete", false)
    // }
}

private fun legacyCoalTruckCount(legacy: Int): Int = when {
    legacy <= 0 -> 0
    legacy >= COAL_TRUCK_DISPLAY_FULL -> COAL_TRUCK_DEFAULT_CAPACITY
    else -> legacy.coerceIn(0, COAL_TRUCK_DEFAULT_CAPACITY)
}

private fun coalTruckDisplayValue(stored: Int, capacity: Int): Int = when {
    stored <= 0 -> 0
    capacity < COAL_TRUCK_HEADBAND_3_CAPACITY && stored >= capacity -> COAL_TRUCK_DISPLAY_FULL
    stored < capacity / 4 -> COAL_TRUCK_DISPLAY_SOME
    stored < capacity / 2 -> COAL_TRUCK_DISPLAY_ALMOST_HALF
    capacity < COAL_TRUCK_HEADBAND_3_CAPACITY -> COAL_TRUCK_DISPLAY_NEARLY_FULL
    stored < COAL_TRUCK_DISPLAY_FULL -> COAL_TRUCK_DISPLAY_NEARLY_FULL
    stored < COAL_TRUCK_DISPLAY_IMPRESSIVELY_STACKED -> COAL_TRUCK_DISPLAY_FULL
    stored < COAL_TRUCK_DISPLAY_MAXIMUM_STACK -> COAL_TRUCK_DISPLAY_IMPRESSIVELY_STACKED
    else -> COAL_TRUCK_DISPLAY_MAXIMUM_STACK
}
