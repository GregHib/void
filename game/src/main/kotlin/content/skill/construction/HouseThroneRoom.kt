package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.changeFloor
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseFurniture
import content.skill.construction.House.Companion.houseRoom
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.leaveHouse
import content.skill.construction.House.Companion.roomBelow
import content.skill.construction.House.Companion.roomPosition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Zone

/**
 * Throne room levers trigger the trap built on the floor in front of the throne and toggle the dungeons challenge mode
 * https://oldschool.runescape.wiki/w/Throne_room
 */
class HouseThroneRoom : Script {
    init {
        objectOperate("Pull", LEVERS) { (target) ->
            val base = houseBase() ?: return@objectOperate
            if (!canPull()) {
                return@objectOperate
            }
            pull(target)
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val trap = FLOORS.firstNotNullOfOrNull { houseFurniture(position, it) }
            val victims = victims(target.tile.zone)
            when (trap) {
                "steel_cage_2" -> cage(victims, CAGE_TICKS)
                "trapdoor_4" -> drop(base, position, victims)
                "lesser_magic_cage", "greater_magic_cage" -> {
                    cage(victims, MAGIC_CAGE_TICKS)
                    if (victims.isNotEmpty()) {
                        magicCage(base, position, victims, greater = trap == "greater_magic_cage")
                    }
                }
                else -> message("Nothing interesting happens.")
            }
        }

        objectOperate("Challenge-mode", LEVERS) { (target) ->
            if (!canPull()) {
                return@objectOperate
            }
            if (get("house_challenge_mode", false)) {
                clear("house_challenge_mode")
                clear("house_pvp_mode")
                pull(target)
                message("Challenge mode is now off.") // TODO proper message
                return@objectOperate
            }
            choice("Which mode would you like?") {
                // https://youtu.be/kLYcT26KFRE?t=73
                option("Challenge mode") {
                    set("house_challenge_mode", true)
                    pull(target)
                    message("Challenge mode is now on.") // TODO proper message
                }
                option("PvP challenge mode") {
                    set("house_challenge_mode", true)
                    set("house_pvp_mode", true)
                    pull(target)
                    message("PvP Challenge mode is on.")
                }
                option("Cancel")
            }
        }
    }

    private fun Player.canPull(): Boolean {
        if (!LEVERS_ENABLED) {
            message("<purple>Not yet implemented.") // TODO
            return false
        }
        if (!inOwnHouse()) {
            message("You can only do that in your own house.") // TODO proper message
            return false
        }
        if (get("house_build_mode", false)) {
            message("You can't do that in building mode.") // TODO proper message
            return false
        }
        return true
    }

    private fun Player.pull(lever: GameObject) {
        face(lever)
        anim("pull_throne_room_lever")
        lever.anim("throne_room_lever_pulled")
    }

    /**
     * Guests standing on the throne room floor trap in [zone]
     */
    private fun Player.victims(zone: Zone): List<Player> {
        val floor = zone.toCuboid().filter { tile -> GameObjects.at(tile).any { it.id.startsWith("floor_decoration_") } }.toSet()
        return Players.filter { it != this && it.get<String>("house_owner") == accountName && it.tile in floor }
    }

    private fun cage(victims: List<Player>, ticks: Int) {
        for (victim in victims) {
            victim.start("movement_delay", ticks)
            victim.message("You've been trapped in a cage!") // TODO proper message
        }
    }

    /**
     * Drops [victims] into the oubliette below the throne room at [position], if there is one
     */
    private fun Player.drop(base: Zone, position: Int, victims: List<Player>) {
        if (houseRoom(roomBelow(position)) != "oubliette") {
            message("Nothing interesting happens.")
            return
        }
        for (victim in victims) {
            if (victim.houseBase() != base) {
                continue
            }
            victim.changeFloor(victim.tile.addLevel(-1))
            victim.message("You fall through the trapdoor!") // TODO proper message
        }
    }

    /**
     * Lets the owner pick what happens to [victims] trapped in the magic cage, the greater cage can also teleport them out of the house
     */
    private suspend fun Player.magicCage(base: Zone, position: Int, victims: List<Player>, greater: Boolean) {
        choice("What would you like to do with your prisoners?") {
            // TODO proper message
            option("Release them") {
                for (victim in victims) {
                    victim.stop("movement_delay")
                }
            }
            option("Drop them into the oubliette") {
                drop(base, position, victims)
            }
            if (greater) {
                option("Teleport them away") {
                    for (victim in victims) {
                        if (victim.houseBase() == base) {
                            victim.leaveHouse()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val LEVERS_ENABLED = false
        private const val LEVERS = "oak_lever,teak_lever,mahogany_lever"
        private const val CAGE_TICKS = 50 // Guessed
        private const val MAGIC_CAGE_TICKS = 100 // Guessed
        private val FLOORS = listOf("throne_room_floor_space", "throne_room_floor_space_2", "throne_room_floor_space_3", "throne_room_floor_space_4", "throne_room_floor_space_5", "throne_room_floor_space_6")
    }
}
