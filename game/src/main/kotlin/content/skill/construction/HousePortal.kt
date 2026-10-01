package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.nameEntry
import content.quest.instance
import content.quest.joinInstance
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.skill.construction.House.Companion.houseLoading
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.loadHouse
import content.skill.construction.House.Companion.roomZone
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.Region

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
    }

    private suspend fun Player.enterOwnHouse(buildMode: Boolean) {
        set("house_build_mode", buildMode)
        val instance = smallInstance()
        loadHouse(instance.tile.zone, buildMode)
        arrive(this, instance)
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
        arrive(owner, instance)
    }

    private suspend fun Player.arrive(owner: Player, instance: Region) {
        setInstanceLogout(Tables.tile("house_locations.${owner["house_location", ""]}.exit"))
        set("house_owner", owner.accountName)
        tele(roomZone(instance.tile.zone, owner.houseRoomPositions.first()).tile.add(3, 3))
        houseLoading()
    }
}
