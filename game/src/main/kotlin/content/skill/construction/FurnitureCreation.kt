package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.exitPortals
import content.skill.construction.House.Companion.hotspot
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseFurniture
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.placeFurniture
import content.skill.construction.House.Companion.removeHouseFurniture
import content.skill.construction.House.Companion.roomPosition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear
import world.gregs.voidps.type.Zone

class FurnitureCreation : Script {
    init {
        objectOperate("Build", "*_space*") { (target) ->
            val hotspot = Rows.getOrNull("house_hotspots.${target.id}") ?: return@objectOperate
            if (!inOwnHouse() || !get("house_build_mode", false)) {
                return@objectOperate
            }
            val furniture = hotspot.itemList("furniture")
            inventories.inventory("poh_furniture_menu_inv").transaction {
                clear()
                for ((index, id) in furniture.withIndex()) {
                    // Menu items are listed down each column
                    set(index % 4 * 2 + index / 4, Item(id))
                }
            }
            set("house_hotspot", target)
            open("furniture_creation")
            for (slot in 1..7) {
                sendMenuSlot(slot, furniture.getOrNull(slot - 1))
            }
        }

        interfaceOpened("furniture_creation") {
            interfaceOptions.send("furniture_creation", "items")
            interfaceOptions.unlockAll("furniture_creation", "items", 0 until 8)
        }

        interfaceClosed("furniture_creation") {
            inventories.clear("poh_furniture_menu_inv")
            clear("house_hotspot")
        }

        interfaceOption("Build", "furniture_creation:items") { (item) ->
            val target: GameObject = get("house_hotspot") ?: return@interfaceOption
            close("furniture_creation")
            build(target, item.id)
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
                    removeHouseFurniture(position, hotspot)
                    removeFurniture(target.tile.zone, hotspot)
                    anim("construction_remove")
                }
                option("No")
            }
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
        set("furniture_creation_hide_cross_$slot", row == null || (has(Skill.Construction, row.int("level")) && inventory.contains(materials)))
    }

    private fun Player.build(target: GameObject, furniture: String) {
        val base = houseBase()
        if (base == null || !inOwnHouse() || !get("house_build_mode", false) || !GameObjects.contains(target)) {
            return
        }
        val position = roomPosition(base, target.tile.zone) ?: return
        val hotspot = hotspot(target) ?: return
        val row = Rows.getOrNull("house_furniture.$furniture") ?: return
        if (!has(Skill.Construction, row.int("level"), message = true)) {
            return
        }
        if (!inventory.contains("hammer", "saw")) {
            message("You need a hammer and saw to build furniture.") // TODO proper message
            return
        }
        if (!inventory.remove(materials(row))) {
            message("You don't have the right materials.") // TODO proper message
            return
        }
        addHouseFurniture(position, hotspot, furniture)
        placeFurniture(target.tile.zone, hotspot, furniture)
        anim("construction_build")
        exp(Skill.Construction, row.int("xp") / 10.0)
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

    /**
     * Removes all furniture built on [hotspot] in [zone], revealing the original hotspots
     */
    private fun removeFurniture(zone: Zone, hotspot: String) {
        for (tile in zone.toCuboid()) {
            for (obj in GameObjects.at(tile)) {
                val original = GameObjects.original(obj) ?: continue
                if (hotspot(original) == hotspot) {
                    obj.remove()
                }
            }
        }
    }

    companion object {
        private val nailTypes = listOf("bronze_nails", "iron_nails", "steel_nails", "black_nails", "mithril_nails", "adamant_nails", "rune_nails")
    }
}
