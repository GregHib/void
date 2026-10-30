package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.skill.construction.House.Companion.freeBuild
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.isFloorFree
import content.skill.construction.HouseConnections.connectStairs
import content.skill.construction.HouseConnections.houseStairs
import content.skill.construction.HouseConnections.ladders
import content.skill.construction.HouseConnections.removeHouseStairs
import content.skill.construction.HouseConnections.removeLadder
import content.skill.construction.HouseConnections.stairsDown
import content.skill.construction.HouseConnections.trapdoors
import content.skill.construction.HouseGrid.GROUND_LEVEL
import content.skill.construction.HouseGrid.roomAbove
import content.skill.construction.HouseGrid.roomBelow
import content.skill.construction.HouseGrid.roomLevel
import content.skill.construction.HouseGrid.roomPosition
import content.skill.construction.HouseHotspots.addHouseFurniture
import content.skill.construction.HouseHotspots.exitPortals
import content.skill.construction.HouseHotspots.hotspot
import content.skill.construction.HouseHotspots.houseFurniture
import content.skill.construction.HouseHotspots.removeHouseFurniture
import content.skill.construction.HouseLoader.furnishRoom
import content.skill.construction.HouseLoader.loadHouse
import content.skill.construction.HouseLoader.placeFurniture
import content.skill.construction.HouseRooms.houseRoom
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.buildingMode
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
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
    private val combatRings = setOf("boxing_ring", "fencing_ring", "combat_ring", "ranging_pedestals", "balance_beam")
    private val nailTypes = listOf("bronze_nails", "iron_nails", "steel_nails", "black_nails", "mithril_nails", "adamant_nails", "rune_nails")

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
                buildingMode()
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

        objectOperate("Work-at", "wooden_workbench,oak_workbench,steel_framed_bench,bench_with_vice,bench_with_lathe") { (target) ->
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
            removeFurniture(target)
        }

        objectApproach("Remove") { (target) ->
            if (houseBase() == null || !inOwnHouse() || GameObjects.original(target)?.let { hotspot(it) } == null) {
                return@objectApproach
            }
            if (!get("house_build_mode", false)) {
                buildingMode()
                return@objectApproach
            }
            approachRange(1)
            removeFurniture(target)
        }
    }

    private suspend fun Player.removeFurniture(target: GameObject) {
        val base = houseBase()
        if (base == null || !inOwnHouse()) {
            return
        }
        val original = GameObjects.original(target) ?: return
        val hotspot = hotspot(original) ?: return
        if (!get("house_build_mode", false)) {
            buildingMode()
            return
        }
        val position = roomPosition(base, target.tile.zone) ?: return
        if (houseFurniture(position, hotspot) == "exit_portal" && exitPortals() <= 1) {
            statement("Your house must have at least one exit portal.")
            return
        }
        choice("Really remove it?") {
            option("Yes") {
                val furniture = houseFurniture(position, hotspot)
                if (!GameObjects.contains(target) || furniture == null) {
                    return@option
                }
                when (furniture) {
                    houseStairs(position) -> {
                        removeHouseStairs(if (stairsDown(position)) roomBelow(position) else roomAbove(position))
                        removeHouseFurniture(position, hotspot)
                        loadHouse(base, buildMode = true)
                    }
                    in trapdoors, in ladders -> {
                        removeLadder(position, furniture)
                        removeHouseFurniture(position, hotspot)
                        loadHouse(base, buildMode = true)
                    }
                    else -> {
                        removeHouseFurniture(position, hotspot)
                        GameObjects.reset(target.tile.zone)
                        furnishRoom(base, position, buildMode = true)
                    }
                }
                anim("construction_remove")
            }
            option("No")
        }
    }

    private fun Player.openMenu(furniture: List<String>) {
        inventories.inventory("poh_furniture_menu_inv").transaction {
            clear()
            for ((index, id) in furniture.withIndex()) {
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
        val lines = materialLines(materials)
        for (line in 1..4) {
            interfaces.sendText("furniture_creation", "material_${slot}_$line", lines.getOrElse(line - 1) { "" })
        }
        interfaces.sendText("furniture_creation", "level_$slot", if (row == null) "" else "Level ${row.int("level")}")
        val flatpack = row?.itemOrNull("flatpack")
        val buildable = row != null && has(Skill.Construction, row.int("level")) && (inventory.contains(materials) || (flatpack != null && inventory.contains(flatpack)))
        set("furniture_creation_hide_cross_$slot", row == null || freeBuild || buildable)
    }

    /**
     * Text for each of the menu's [materials] lines, the last lines hold two materials each when there are too many for one per line
     */
    private fun materialLines(materials: List<Item>): List<String> {
        val leading = (8 - materials.size).coerceIn(0, 4)
        val lines = materials.take(leading).map { listOf(it) } + materials.drop(leading).chunked(2)
        return lines.map { line -> line.joinToString(", ") { "${it.amount} ${it.def.name}" } }
    }

    private suspend fun Player.makeFlatpack(workbench: String, furniture: String) {
        val row = Rows.getOrNull("house_furniture.$furniture") ?: return
        val flatpack = row.itemOrNull("flatpack") ?: return
        if (row.int("level") > Tables.int("house_workbenches.$workbench.level")) {
            return
        }
        if (!has(Skill.Construction, row.int("level"), message = true)) {
            return
        }
        if (!inventory.contains("hammer", "saw")) {
            message("You need a saw and a hammer in order to make furniture.")
            return
        }
        val materials = materials(row)
        inventory.transaction {
            for (item in materials) {
                remove(item.id, item.amount)
            }
            add(flatpack)
        }
        if (inventory.transaction.error != TransactionError.None) {
            missingMaterials(materials)
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
    private suspend fun Player.build(base: Zone, target: GameObject, position: Int, hotspot: String, furniture: String) {
        if (houseBase() != base || !GameObjects.contains(target)) {
            return
        }
        val row = Rows.getOrNull("house_furniture.$furniture") ?: return
        if (!canBuild(row) || !canUpgrade(position, hotspot, row)) {
            return
        }
        val flatpack = row.itemOrNull("flatpack")
        val materials = materials(row)
        val packed = !freeBuild && flatpack != null && !inventory.contains(materials) && inventory.contains(flatpack)
        if (packed) {
            if (!inventory.remove(flatpack)) {
                return
            }
        } else if (!freeBuild && !inventory.remove(materials)) {
            missingMaterials(materials)
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
            loadHouse(base, buildMode = true)
        } else if ((furniture == "dungeon_entrance" || furniture in trapdoors) && connectStairs(roomBelow(position))) {
            loadHouse(base, buildMode = true)
        } else {
            placeFurniture(target.tile.zone, hotspot, furniture)
        }
        anim(if (target.shape < ObjectShape.CENTRE_PIECE_STRAIGHT) "human_poh_build_wall" else "construction_build")
        if (furniture in combatRings && tile.zone == target.tile.zone) {
            moveToEdge()
        }
        if (!freeBuild && !packed) {
            exp(Skill.Construction, row.int("xp") / 10.0)
        }
    }

    private suspend fun Player.missingMaterials(materials: List<Item>) {
        val items = materials.joinToString { "<${if (inventory.contains(it.id, it.amount)) "black" else "red"}>${it.amount}x ${it.def.name}" }
        statement("You don't have the right materials:<br>$items")
    }

    /**
     * Moves the player out of a combat ring built around them, to the nearest free tile on the edge of the room
     */
    private fun Player.moveToEdge() {
        val corner = tile.zone.tile
        val edge = tile.zone.toCuboid().filter { it.x == corner.x || it.y == corner.y || it.x == corner.x + 7 || it.y == corner.y + 7 }
        tele(edge.filter(::isFloorFree).minByOrNull { it.distanceTo(tile) } ?: return)
    }

    /**
     * Whether [row] can be built on [hotspot] in the room at [position], furniture already built can only be replaced
     * by better furniture, and furniture which upgrades another can only be built on top of it.
     */
    private fun Player.canUpgrade(position: Int, hotspot: String, row: RowDefinition): Boolean {
        val current = houseFurniture(position, hotspot)
        val required = row.itemOrNull("upgrade")
        if (required != null && current != required) {
            return false
        }
        if (current == null) {
            return true
        }
        val furniture = Tables.get("house_hotspots").rows().firstOrNull { (it.stringOrNull("group") ?: it.rowId) == hotspot }?.itemList("furniture") ?: return false
        return furniture.indexOf(row.rowId) > furniture.indexOf(current)
    }

    private suspend fun Player.canBuild(row: RowDefinition): Boolean {
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
            message("You need a saw and a hammer in order to make furniture.")
            return false
        }
        val materials = materials(row)
        if (!inventory.contains(materials)) {
            missingMaterials(materials)
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
}
