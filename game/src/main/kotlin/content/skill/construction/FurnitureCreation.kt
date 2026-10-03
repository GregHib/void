package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.GROUND_LEVEL
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.connectStairs
import content.skill.construction.House.Companion.exitPortals
import content.skill.construction.House.Companion.freeBuild
import content.skill.construction.House.Companion.furnishRoom
import content.skill.construction.House.Companion.hotspot
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseFurniture
import content.skill.construction.House.Companion.houseRoom
import content.skill.construction.House.Companion.houseStairs
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.loadHouse
import content.skill.construction.House.Companion.placeFurniture
import content.skill.construction.House.Companion.removeHouseFurniture
import content.skill.construction.House.Companion.removeHouseStairs
import content.skill.construction.House.Companion.roomAbove
import content.skill.construction.House.Companion.roomBelow
import content.skill.construction.House.Companion.roomLevel
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.stairsDown
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Zone

/**
 * Furniture is built on hotspots in building mode and upgraded into better furniture on the same hotspot.
 * Workbenches make flatpacks which can be built in place of the furniture's materials.
 */
class FurnitureCreation : Script {
    init {
        objectOperate("Build", "*_space*") { (target) ->
            val hotspot = Rows.getOrNull("house_hotspots.${target.id}") ?: return@objectOperate
            if (!inOwnHouse() || !get("house_build_mode", false)) {
                return@objectOperate
            }
            set("house_hotspot", target)
            openMenu(hotspot.itemList("furniture"))
        }

        objectOperate("Upgrade") { (target) ->
            val base = houseBase()
            if (base == null || !inOwnHouse()) {
                return@objectOperate
            }
            if (!get("house_build_mode", false)) {
                message("You can only do that in building mode.") // TODO proper message
                return@objectOperate
            }
            val original = GameObjects.original(target) ?: return@objectOperate
            val hotspot = hotspot(original) ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val current = houseFurniture(position, hotspot) ?: return@objectOperate
            val furniture = Tables.itemList("house_hotspots.${original.id}.furniture")
            set("house_hotspot", target)
            openMenu(furniture.drop(furniture.indexOf(current) + 1))
        }

        objectOperate("Work-at", WORKBENCHES) { (target) ->
            set("house_workbench", target.id)
            open("flatpack_creation")
        }

        interfaceOption("Select", "flatpack_creation:*") {
            val workbench: String = get("house_workbench") ?: return@interfaceOption
            val hotspot = Tables.objOrNull("house_flatpacks.${it.component}.hotspot") ?: return@interfaceOption
            openMenu(Tables.itemList("house_hotspots.$hotspot.furniture"))
            set("house_workbench", workbench)
        }

        interfaceOption("Close", "flatpack_creation:close") {
            close("flatpack_creation")
        }

        interfaceClosed("flatpack_creation") {
            clear("house_workbench")
        }

        interfaceOpened("furniture_creation") {
            interfaceOptions.send("furniture_creation", "items")
            interfaceOptions.unlockAll("furniture_creation", "items", 0 until 8)
        }

        interfaceClosed("furniture_creation") {
            inventories.clear("poh_furniture_menu_inv")
            clear("house_hotspot")
            clear("house_workbench")
        }

        interfaceOption("Build", "furniture_creation:items") { (item) ->
            val workbench: String? = get("house_workbench")
            val target: GameObject? = get("house_hotspot")
            close("furniture_creation")
            if (workbench != null) {
                makeFlatpack(workbench, item.id)
            } else if (target != null) {
                build(target, item.id)
            }
        }

        interfaceOption("Close", "furniture_creation:close") {
            close("furniture_creation")
        }

        objectOperate("Remove") { (target) ->
            val base = houseBase()
            if (base == null || !inOwnHouse()) {
                return@objectOperate
            }
            val original = GameObjects.original(target) ?: return@objectOperate
            val hotspot = hotspot(original) ?: return@objectOperate
            if (!get("house_build_mode", false)) {
                message("You can only do that in building mode.") // TODO proper message
                return@objectOperate
            }
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            if (houseFurniture(position, hotspot) == "exit_portal" && exitPortals() <= 1) {
                message("Your house must have at least one exit portal.") // TODO proper message
                return@objectOperate
            }
            choice("Really remove it?") {
                option("Yes") {
                    if (!GameObjects.contains(target) || houseFurniture(position, hotspot) == null) {
                        return@option
                    }
                    if (houseFurniture(position, hotspot) == houseStairs(position)) {
                        // Staircases are removed from both floors
                        removeHouseStairs(if (stairsDown(position)) roomBelow(position) else roomAbove(position))
                        removeHouseFurniture(position, hotspot)
                        loadHouse(base, buildMode = true)
                    } else {
                        removeHouseFurniture(position, hotspot)
                        GameObjects.reset(target.tile.zone)
                        furnishRoom(base, position, buildMode = true)
                    }
                    anim("construction_remove")
                }
                option("No")
            }
        }
    }

    /**
     * Opens the furniture creation menu listing [furniture]
     */
    private fun Player.openMenu(furniture: List<String>) {
        inventories.inventory("poh_furniture_menu_inv").transaction {
            clear()
            for ((index, id) in furniture.withIndex()) {
                // Menu items are listed down each column
                set(index % 4 * 2 + index / 4, Item(id))
            }
        }
        open("furniture_creation")
        for (slot in 1..7) {
            sendMenuSlot(slot, furniture.getOrNull(slot - 1))
        }
    }

    private fun Player.sendMenuSlot(slot: Int, furniture: String?) {
        val row = if (furniture == null) null else Rows.get("house_furniture.$furniture")
        val materials = if (row == null) emptyList() else materials(row)
        interfaces.sendText("furniture_creation", "name_$slot", if (furniture == null) "" else ItemDefinitions.get(furniture).name)
        for (line in 1..4) {
            val item = materials.getOrNull(line - 1)
            interfaces.sendText("furniture_creation", "material_${slot}_$line", if (item == null) "" else "${item.amount} ${item.def.name}")
        }
        interfaces.sendText("furniture_creation", "level_$slot", if (row == null) "" else "Level ${row.int("level")}")
        val flatpack = row?.itemOrNull("flatpack")
        val buildable = row != null && has(Skill.Construction, row.int("level")) && (inventory.contains(materials) || (flatpack != null && inventory.contains(flatpack)))
        set("furniture_creation_hide_cross_$slot", row == null || freeBuild || buildable)
    }

    /**
     * Makes a flatpack of [furniture] at a [workbench] which can make furniture of its level
     */
    private fun Player.makeFlatpack(workbench: String, furniture: String) {
        val row = Rows.getOrNull("house_furniture.$furniture") ?: return
        val flatpack = row.itemOrNull("flatpack") ?: return
        if (row.int("level") > Tables.int("house_workbenches.$workbench.level")) {
            message("You need a better workbench to make that.") // TODO proper message
            return
        }
        if (!has(Skill.Construction, row.int("level"), message = true)) {
            return
        }
        if (!inventory.contains("hammer", "saw")) {
            message("You need a hammer and saw to make furniture.") // TODO proper message
            return
        }
        inventory.transaction {
            for (item in materials(row)) {
                remove(item.id, item.amount)
            }
            add(flatpack)
        }
        if (inventory.transaction.error != TransactionError.None) {
            message("You don't have the right materials.") // TODO proper message
            return
        }
        anim("construction_build")
        exp(Skill.Construction, row.int("xp") / 10.0)
    }

    private suspend fun Player.build(target: GameObject, furniture: String) {
        val base = houseBase()
        if (base == null || !inOwnHouse() || !get("house_build_mode", false) || !GameObjects.contains(target)) {
            return
        }
        val position = roomPosition(base, target.tile.zone) ?: return
        // Upgrades are built on the furniture rather than the hotspot
        val hotspot = hotspot(GameObjects.original(target) ?: target) ?: return
        val row = Rows.getOrNull("house_furniture.$furniture") ?: return
        if (!canBuild(row) || !canUpgrade(position, hotspot, row)) {
            return
        }
        val down = Tables.objOrNull("house_rooms.${houseRoom(position)}.stairs_down")
        if (roomLevel(position) == GROUND_LEVEL && down != null && hotspot == Tables.objOrNull("house_rooms.${houseRoom(position)}.stairs")) {
            choice("Build stairs in which direction") {
                option("Up") {
                    build(base, target, position, hotspot, furniture)
                }
                option("Down") {
                    build(base, target, position, down, furniture)
                }
            }
            return
        }
        build(base, target, position, hotspot, furniture)
    }

    /**
     * Builds [furniture] on [hotspot] in the room at [position], replacing any furniture it's upgrading.
     * Staircases are built on both floors.
     */
    private fun Player.build(base: Zone, target: GameObject, position: Int, hotspot: String, furniture: String) {
        if (houseBase() != base || !GameObjects.contains(target)) {
            return
        }
        val row = Rows.getOrNull("house_furniture.$furniture") ?: return
        if (!canBuild(row) || !canUpgrade(position, hotspot, row)) {
            return
        }
        val flatpack = row.itemOrNull("flatpack")
        // Materials are used before flatpacks
        val packed = !freeBuild && flatpack != null && !inventory.contains(materials(row)) && inventory.contains(flatpack)
        if (packed) {
            if (!inventory.remove(flatpack ?: return)) {
                return
            }
        } else if (!freeBuild && !inventory.remove(materials(row))) {
            message("You don't have the right materials.") // TODO proper message
            return
        }
        val upgrade = houseFurniture(position, hotspot) != null
        if (upgrade) {
            removeHouseFurniture(position, hotspot)
        }
        addHouseFurniture(position, hotspot, furniture)
        if (upgrade) {
            GameObjects.reset(target.tile.zone)
            furnishRoom(base, position, buildMode = true)
        } else if (furniture == houseStairs(position)) {
            connectStairs(if (stairsDown(position)) roomBelow(position) else roomAbove(position))
            // Stairs leading down change the rooms template
            loadHouse(base, buildMode = true)
        } else if (furniture == "dungeon_entrance" && connectStairs(roomBelow(position))) {
            // The entrance leads down to stairs in the dungeon room below
            loadHouse(base, buildMode = true)
        } else {
            placeFurniture(target.tile.zone, hotspot, furniture)
        }
        anim("construction_build")
        // Free building doesn't give experience so it can't be used for training, flatpacks gave theirs when made
        if (!freeBuild && !packed) {
            exp(Skill.Construction, row.int("xp") / 10.0)
        }
    }

    /**
     * Whether [row] can be built on [hotspot] in the room at [position], furniture already built can only be replaced
     * by better furniture, and furniture which upgrades another can only be built on top of it.
     */
    private fun Player.canUpgrade(position: Int, hotspot: String, row: RowDefinition): Boolean {
        val current = houseFurniture(position, hotspot)
        val required = row.itemOrNull("upgrade")
        if (required != null && current != required) {
            message("You need to upgrade a ${ItemDefinitions.get(required).name.lowercase()} to build that.") // TODO proper message
            return false
        }
        if (current == null) {
            return true
        }
        val furniture = Tables.get("house_hotspots").rows().firstOrNull { (it.stringOrNull("group") ?: it.rowId) == hotspot }?.itemList("furniture") ?: return false
        return furniture.indexOf(row.rowId) > furniture.indexOf(current)
    }

    private fun Player.canBuild(row: RowDefinition): Boolean {
        if (freeBuild) {
            return true
        }
        if (!has(Skill.Construction, row.int("level"), message = true)) {
            return false
        }
        val flatpack = row.itemOrNull("flatpack")
        if (flatpack != null && inventory.contains(flatpack)) {
            return true
        }
        if (!inventory.contains("hammer", "saw")) {
            message("You need a hammer and saw to build furniture.") // TODO proper message
            return false
        }
        if (!inventory.contains(materials(row))) {
            message("You don't have the right materials.") // TODO proper message
            return false
        }
        return true
    }

    /**
     * Materials needed to build [row], using the first type of nails the player has enough of
     */
    private fun Player.materials(row: RowDefinition): List<Item> {
        val materials = row.itemList("materials").zip(row.intList("amounts")) { id, amount -> Item(id, amount) }
        val nails = row.int("nails")
        if (nails == 0) {
            return materials
        }
        val type = nailTypes.firstOrNull { inventory.contains(it, nails) } ?: nailTypes.first()
        return materials + Item(type, nails)
    }

    companion object {
        private const val WORKBENCHES = "wooden_workbench,oak_workbench,steel_framed_bench,bench_with_vice,bench_with_lathe"
        private val nailTypes = listOf("bronze_nails", "iron_nails", "steel_nails", "black_nails", "mithril_nails", "adamant_nails", "rune_nails")
    }
}
