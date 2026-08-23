package content.skill.prayer.bone

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.statement
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.type.Tile

class BoneGrinder : Script {

    init {
        objectOperate("Fill", "ectofuntus_hopper") { (target) ->
            turn(target.tile)
            fill(null)
        }

        itemOnObjectOperate(obj = "ectofuntus_hopper") { (target, item) ->
            turn(target.tile)
            if (item.id == SILVER_BAR) {
                grindSilver(target.tile)
                return@itemOnObjectOperate
            }
            fill(item)
        }

        objectOperate("Wind", "ectofuntus_bone_grinder") { (target) ->
            turn(target.tile)
            wind()
        }

        objectOperate("Empty", "ectofuntus_bin") { (target) ->
            turn(target.tile)
            empty()
        }

        objectOperate("Status", "ectofuntus_bone_grinder") {
            status()
        }

        objectOperate("Settings", "ectofuntus_bone_grinder") {
            settings()
        }
    }

    private suspend fun Player.fill(item: Item?) {
        if (get("bone_grinder_stage", 0) != EMPTY) {
            message("You already have some bones in the hopper.")
            return
        }
        if (item != null && Tables.intOrNull("bones.${item.id}.xp") != null && Tables.itemOrNull("bones.${item.id}.bonemeal") == null) {
            statement("These bones could break the bone grinder. Perhaps I should find some different bones.")
            return
        }
        val row = boneRow(item)
        if (row == null) {
            message("You have no bones to grind.")
            return
        }
        if (get("bone_grinder_auto", false)) {
            grind(row)
            return
        }
        load(row)
    }

    private fun Player.load(row: RowDefinition) {
        anim("fill_bone_hopper")
        sound("fill_grinder")
        if (!inventory.remove(row.rowId)) {
            return
        }
        set("bone_grinder_bones", row.rowId)
        set("bone_grinder_stage", HOPPER)
        message("You fill the hopper with bones.")
    }

    private fun Player.wind() {
        when (get("bone_grinder_stage", 0)) {
            EMPTY -> {
                message("You have no bones loaded to grind.")
                return
            }
            BIN -> {
                message("You already have some bonemeal that needs to be collected.")
                return
            }
        }
        anim("wind_bone_grinder")
        sound("grinder_grinding")
        set("bone_grinder_stage", BIN)
        message("You wind the grinder handle.")
        message("Some crushed bones pour into the bin.")
    }

    private fun Player.empty() {
        if (get("bone_grinder_stage", 0) != BIN) {
            message("You have no bonemeal to collect.")
            return
        }
        if (!inventory.contains("empty_pot")) {
            message("You don't have any pots to take the bonemeal with.")
            return
        }
        val bones = get("bone_grinder_bones", "")
        val bonemeal = Tables.itemOrNull("bones.$bones.bonemeal") ?: return
        anim("empty_bone_bin")
        sound("grinder_empty")
        if (!inventory.replace("empty_pot", bonemeal)) {
            return
        }
        set("bone_grinder_stage", EMPTY)
        set("bone_grinder_bones", "")
        message("You empty the bin into the pot.")
    }

    /**
     * Silver bars skip the hopper, wind and bin steps entirely; each bar is ground straight
     * into dust and no pot is used to collect it. Each bar waits out the full length of the
     * fill animation so one visible grind produces one dust.
     */
    private suspend fun Player.grindSilver(hopper: Tile) {
        val (_, amount) = makeAmount(
            items = listOf(SILVER_DUST),
            type = "Make",
            maximum = inventory.count(SILVER_BAR),
            text = "How many would you like to make?",
        )
        delay()
        softTimers.start("grind_silver")
        crush(hopper, amount)
    }

    private fun Player.crush(hopper: Tile, amount: Int) {
        if (amount <= 0 || !inventory.contains(SILVER_BAR)) {
            softTimers.stop("grind_silver")
            return
        }
        face(hopper)
        val ticks = anim("fill_bone_hopper")
        sound("fill_grinder")
        weakQueue("grind_silver", ticks.coerceAtLeast(GRIND_TICKS)) {
            if (!inventory.replace(SILVER_BAR, SILVER_DUST)) {
                softTimers.stop("grind_silver")
                return@weakQueue
            }
            message("You grind the silver bar into dust.")
            crush(hopper, amount - 1)
        }
    }

    private fun Player.status() {
        val mode = if (get("bone_grinder_auto", false)) "automatic" else "manual"
        val state = when (get("bone_grinder_stage", 0)) {
            HOPPER -> "There are ${get("bone_grinder_bones", "").toLowerSpaceCase()} in the hopper."
            BIN -> "There is bonemeal waiting in the bin."
            else -> "The grinder is empty."
        }
        message("$state The grinder is set to $mode.")
    }

    private suspend fun Player.settings() {
        choice("How should the grinder operate?") {
            option("Automatic.") {
                set("bone_grinder_auto", true)
                message("The grinder is now set to automatic.")
            }
            option("Manual.") {
                set("bone_grinder_auto", false)
                message("The grinder is now set to manual.")
            }
        }
    }

    /**
     * Turns to the machine on [obj], resting a tick afterwards so the turn reads separately from
     * the animation that follows. The interaction itself walks the player into reach, so anyone
     * already stood by the machine stays put.
     *
     * Waits for the walk to finish first; the tile updates as a step is taken but the client is
     * still walking into it, so turning straight away animates the player mid-stride.
     */
    private suspend fun Player.turn(obj: Tile) {
        var ticks = 0
        while ((steps.isNotEmpty() || steps.last > GameLoop.tick) && ticks++ < STATION_TIMEOUT) {
            pause(1)
        }
        face(obj)
        pause(1)
    }

    /**
     * Takes up [target] and turns to the machine on [obj], resting a tick before the walk, before
     * the turn and before the caller's animation so each part of the sequence reads separately.
     * Every wait is interruptible, so walking away abandons the batch; false when they never
     * arrived.
     */
    private suspend fun Player.station(obj: Tile, target: Tile): Boolean {
        pause(1)
        walkTo(target)
        var ticks = 0
        while (tile != target) {
            if (ticks++ >= STATION_TIMEOUT) {
                return false
            }
            pause(1)
        }
        turn(obj)
        return true
    }

    /**
     * Automatic mode; walks between the hopper, grinder and bin running each step unattended,
     * repeating until the player runs out of either bones or empty pots.
     */
    private suspend fun Player.grind(row: RowDefinition) {
        var bones = row
        // The interaction has already walked the player to the hopper and turned them to it, so
        // the first set is ground where they stand rather than shuffling onto the circuit.
        var stationed = true
        while (true) {
            if (!inventory.contains("empty_pot")) {
                message("You don't have any pots to take the bonemeal with.")
                return
            }
            if (!stationed && !station(HOPPER_OBJECT, HOPPER_TILE)) {
                return
            }
            stationed = false
            val filling = anim("fill_bone_hopper")
            sound("fill_grinder")
            if (!inventory.remove(bones.rowId)) {
                return
            }
            pause(filling.coerceAtLeast(GRIND_TICKS))

            if (!station(GRINDER_OBJECT, GRINDER_TILE)) {
                return
            }
            val winding = anim("wind_bone_grinder")
            sound("grinder_grinding")
            pause(winding.coerceAtLeast(GRIND_TICKS))

            if (!station(BIN_OBJECT, BIN_TILE)) {
                return
            }
            if (!collect(bones)) {
                return
            }
            bones = boneRow(null) ?: return
        }
    }

    private fun Player.collect(row: RowDefinition): Boolean {
        if (!inventory.replace("empty_pot", row.item("bonemeal"))) {
            return false
        }
        anim("empty_bone_bin")
        sound("grinder_empty")
        message("You grind the ${row.rowId.toLowerSpaceCase()} into the pot.")
        return true
    }

    private fun Player.boneRow(item: Item?): RowDefinition? {
        if (item != null) {
            val row = Tables.get("bones").rows().firstOrNull { it.rowId == item.id } ?: return null
            row.itemOrNull("bonemeal") ?: return null
            return row
        }
        return Tables.get("bones").rows().firstOrNull {
            it.itemOrNull("bonemeal") ?: return@firstOrNull false
            inventory.contains(it.rowId)
        }
    }

    companion object {
        private const val EMPTY = 0
        private const val HOPPER = 1
        private const val BIN = 2
        private const val GRIND_TICKS = 3
        private const val STATION_TIMEOUT = 50
        private const val SILVER_BAR = "silver_bar"
        private const val SILVER_DUST = "silver_dust"
        private val HOPPER_OBJECT = Tile(3660, 3525, 1)
        private val GRINDER_OBJECT = Tile(3659, 3525, 1)
        private val BIN_OBJECT = Tile(3658, 3525, 1)
        private val HOPPER_TILE = Tile(3660, 3524, 1)
        private val GRINDER_TILE = Tile(3659, 3524, 1)
        private val BIN_TILE = Tile(3658, 3524, 1)
    }
}
