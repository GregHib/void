package content.skill.construction

import content.entity.obj.door.Door
import content.entity.obj.door.DoubleDoor
import content.entity.obj.door.closeDoor
import content.entity.obj.door.openDoor
import content.quest.clearInstance
import content.quest.exitInstance
import content.quest.instance
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.skill.construction.HouseGrid.roomZone
import content.skill.construction.HouseHangman.Companion.despawnHangman
import content.skill.construction.HouseHotspots.houseFurnitureIds
import content.skill.construction.HouseHotspots.houseFurnitureRooms
import content.skill.construction.HouseLoader.loadHouse
import content.skill.construction.HouseMenagerie.Companion.despawnHousePets
import content.skill.construction.HouseMenagerie.Companion.spawnHousePets
import content.skill.construction.HousePortalChamber.Companion.stopScrying
import content.skill.construction.HouseRooms.hasHouse
import content.skill.construction.HouseRooms.houseRoomIds
import content.skill.construction.HouseRooms.houseRoomPositions
import content.skill.construction.HouseRooms.newHouse
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Entering, leaving and moving around a players house
 */
class House : Script {
    init {
        droppable {
            if (inOwnHouse() && get("house_build_mode", false)) {
                message("You cannot drop items while in building mode.")
                false
            } else {
                true
            }
        }

        moved {
            if (!contains("house_owner") || contains("scrying_return")) {
                return@moved
            }
            val instance = instance()
            if (instance != null && Instances.owner(tile) == instance) {
                return@moved
            }
            leaveHouse(teleport = false)
        }

        val doors = "basic_wood_door_left_closed,basic_wood_door_right_closed,large_door_45_closed,large_door_47_closed," +
            "whitewashed_stone_door_left_closed,whitewashed_stone_door_right_closed,fremennik_wood_door_left_closed,fremennik_wood_door_right_closed," +
            "tropical_wood_door_left_closed,tropical_wood_door_right_closed,fancy_stone_door_left_closed,fancy_stone_door_right_closed"

        objectOperate("Open", doors) { (target) ->
            openHouseDoor(target)
        }

        objectOperate("Close", doors.replace("_closed", "_opened")) { (target) ->
            closeHouseDoor(target)
        }

        playerDespawn {
            if (inOwnHouse()) {
                despawnHousePets()
                expelGuests()
            }
        }

        playerDeath { death ->
            val owner = houseOwner() ?: return@playerDeath
            val base = houseBase() ?: return@playerDeath
            death.dropItems = false
            death.teleport = get("house_ring_exit") ?: owner.houseSpawn(base)
        }
    }

    companion object {
        private const val BLOCKED = CollisionFlag.FLOOR or CollisionFlag.FLOOR_DECORATION or CollisionFlag.OBJECT

        // Items which can't leave the house
        private val houseItems = listOf(
            "boxing_gloves_red",
            "boxing_gloves_blue",
            "wooden_sword",
            "wooden_shield_weapons_rack",
            "pugel",
            "treasure_stone",
            "prize_key",
            "hoop",
            "dart",
            "bow_and_arrow",
        )

        /**
         * Whether furniture and rooms can be built without levels, tools, materials or coins
         */
        val freeBuild: Boolean
            get() = Settings["construction.freeBuild", false]

        val Player.buildLevel: Int
            get() = if (freeBuild) 99 else levels.get(Skill.Construction)

        fun Player.inOwnHouse() = get<String>("house_owner") == accountName

        /**
         * The owner of the house the player is currently in
         */
        fun Player.houseOwner(): Player? {
            val owner: String = get("house_owner") ?: return null
            return if (owner == accountName) this else Players.findByAccount(owner)
        }

        /**
         * South-west zone of the house the player is currently in
         */
        fun Player.houseBase(): Zone? {
            if (!contains("house_owner")) {
                return null
            }
            return instance()?.tile?.zone
        }

        /**
         * Tile in front of the exit portal of this players house starting at [base], or the middle of the garden without one
         */
        fun Player.houseSpawn(base: Zone): Tile {
            val portal = houseFurnitureIds.indexOf("exit_portal")
            if (portal != -1) {
                val zone = roomZone(base, houseFurnitureRooms[portal])
                val obj = zone.toCuboid().firstNotNullOfOrNull { GameObjects.findOrNull(it, "exit_portal") }
                if (obj != null) {
                    return obj.tile.add(0, -1)
                }
            }
            val garden = houseRoomIds.indexOf("garden").coerceAtLeast(0)
            return roomZone(base, houseRoomPositions[garden]).tile.add(3, 3)
        }

        fun isFloorFree(tile: Tile) = !Collisions.check(tile, BLOCKED)

        /**
         * Moves to the nearest free tile to [tile] in its room
         */
        fun Player.changeFloor(tile: Tile) {
            tele(tile.zone.toCuboid().filter(::isFloorFree).minByOrNull { it.distanceTo(tile) } ?: tile)
        }

        /**
         * Opens a [door] in a house, doors elsewhere which share its id open as normal
         */
        suspend fun Player.openHouseDoor(door: GameObject) {
            if (houseBase() == null) {
                openDoor(door)
            } else {
                moveHouseDoor(door, open = true)
            }
        }

        /**
         * Closes a [door] in a house, doors elsewhere which share its id close as normal
         */
        suspend fun Player.closeHouseDoor(door: GameObject) {
            if (houseBase() == null) {
                closeDoor(door)
            } else {
                moveHouseDoor(door, open = false)
            }
        }

        /**
         * Opens or closes a house [door] and its double, opened doors stay open until closed which reverts them back to where they were.
         * Doorways have hotspots under them which come back when a door moves off of them, so outside of building mode they're removed again.
         */
        private fun Player.moveHouseDoor(door: GameObject, open: Boolean) {
            val def = door.def(this)
            val double = DoubleDoor.get(this, door, def, if (open) 0 else 1)
            val moved = if (open) Door.openDoor(this, door, def, ticks = Int.MAX_VALUE) else Door.closeDoor(this, door, def)
            if (!moved || get("house_build_mode", false)) {
                return
            }
            for (tile in listOfNotNull(door.tile, double?.tile)) {
                val hotspot = GameObjects.getShape(tile, door.shape) ?: continue
                if (hotspot.def(this).containsOption("Build")) {
                    hotspot.remove()
                }
            }
        }

        /**
         * Creates a new instance of the players house, returning the tile to arrive at
         */
        fun Player.createHouse(buildMode: Boolean): Tile {
            leaveHouse(teleport = false)
            set("house_build_mode", buildMode)
            if (!hasHouse()) {
                newHouse()
            }
            val instance = smallInstance()
            loadHouse(instance.tile.zone, buildMode)
            val tile = arrival(this, instance)
            spawnHousePets(instance.tile.zone)
            return tile
        }

        /**
         * Marks the player as inside [owner]'s house [instance], returning the tile to arrive at
         */
        fun Player.arrival(owner: Player, instance: Region): Tile {
            setInstanceLogout(Tables.tile("house_locations.${owner["house_location", ""]}.exit"))
            set("house_owner", owner.accountName)
            return owner.houseSpawn(instance.tile.zone)
        }

        suspend fun Player.houseLoading() {
            open("house_loading")
            delay(3)
            close("house_loading")
        }

        fun Player.leaveHouse(teleport: Boolean = true) {
            val owner: String = remove("house_owner") ?: return
            stopScrying()
            despawnHangman()
            removeHouseItems()
            if (hasOpen("house_options")) {
                open("options")
            }
            if (owner == accountName) {
                despawnHousePets()
                expelGuests()
            }
            if (teleport) {
                exitInstance()
            } else {
                clearInstance()
            }
        }

        private fun Player.removeHouseItems() {
            for (inventory in listOf(inventory, equipment, beastOfBurden)) {
                for (item in houseItems) {
                    val count = inventory.count(item)
                    if (count > 0) {
                        inventory.remove(item, count)
                    }
                }
            }
        }

        fun Player.expelGuests() {
            for (player in Players) {
                if (player != this && player.get<String>("house_owner") == accountName) {
                    player.leaveHouse()
                }
            }
        }
    }
}
