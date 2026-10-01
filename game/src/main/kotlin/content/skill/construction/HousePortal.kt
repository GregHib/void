package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.nameEntry
import content.quest.instance
import content.quest.joinInstance
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.skill.construction.House.Companion.hasHouse
import content.skill.construction.House.Companion.houseLoading
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.leaveHouse
import content.skill.construction.House.Companion.loadHouse
import content.skill.construction.House.Companion.newHouse
import content.skill.construction.House.Companion.roomZone
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class HousePortal : Script {
    init {
        objectOperate("Enter", "house_portal_*") { (target) ->
            val location = target.id.removePrefix("house_portal_")
            val owned = get("house_location", "") == location
            choice {
                if (owned) {
                    option("Go to your house.") {
                        enterOwnHouse(buildMode = false)
                    }
                    option("Go to your house (building mode).") {
                        enterOwnHouse(buildMode = true)
                    }
                }
                option("Go to a friend's house.") {
                    visitFriend(location)
                }
                option("Never mind.")
            }
        }

        interfaceOption("Cast", "modern_spellbook:teleport_to_house") {
            teleportHome("modern", it.component, xp = Tables.int("spells.teleport_to_house.xp") / 10.0)
        }

        itemOption("Break", "teleport_to_house") {
            teleportHome("tablet", it.item.id)
        }

        teleportLand("modern") {
            delay(1)
            close("house_loading")
        }

        teleportLand("tablet") {
            delay(3)
            close("house_loading")
        }
    }

    private suspend fun Player.enterOwnHouse(buildMode: Boolean) {
        tele(createHouse(buildMode))
        houseLoading()
    }

    /**
     * Teleport into the players own house by the portal
     */
    private fun Player.teleportHome(type: String, spell: String, xp: Double = 0.0) {
        if (!contains("house_location")) {
            message("You don't have a house to teleport to.") // TODO proper message
            return
        }
        val tile = createHouse(buildMode = false)
        closeInterfaces()
        Teleport.teleport(this, type, spell, xp = xp, clearInterfaces = false) {
            open("house_loading")
            tile
        }
    }

    /**
     * Creates a new instance of the players house, returning the tile to arrive at
     */
    private fun Player.createHouse(buildMode: Boolean): Tile {
        leaveHouse(teleport = false)
        set("house_build_mode", buildMode)
        if (!hasHouse()) {
            newHouse()
        }
        val instance = smallInstance()
        loadHouse(instance.tile.zone, buildMode)
        return arrival(this, instance)
    }

    private suspend fun Player.visitFriend(location: String) {
        val owner = Players.find(nameEntry("Enter name:"))
        if (owner == null || !owner.inOwnHouse()) {
            message("That player is offline, or has privacy mode enabled.") // TODO proper messages
            return
        }
        if (owner["house_location", ""] != location) {
            message("That player's house isn't here.") // TODO proper messages
            return
        }
        if (owner["house_build_mode", false]) {
            message("The owner currently has build mode turned on.") // TODO proper messages
            return
        }
        val instance = owner.instance() ?: return
        set("instance_logout", true)
        joinInstance(instance)
        tele(arrival(owner, instance))
        houseLoading()
    }

    /**
     * Marks the player as inside [owner]'s house [instance], returning the tile to arrive at
     */
    private fun Player.arrival(owner: Player, instance: Region): Tile {
        setInstanceLogout(Tables.tile("house_locations.${owner["house_location", ""]}.exit"))
        set("house_owner", owner.accountName)
        // TODO arrive at the exit portal once furniture is added
        val garden = owner.houseRoomIds.indexOf("garden").coerceAtLeast(0)
        return roomZone(instance.tile.zone, owner.houseRoomPositions[garden]).tile.add(3, 3)
    }
}
