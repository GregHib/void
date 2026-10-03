package content.skill.construction

import content.entity.obj.door.Door
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

/**
 * Dungeon furniture: doors which are locked to guests in challenge mode and treasure room chests the owner keeps coins in.
 * https://runescape.wiki/w/Treasure_room
 */
class HouseDungeon : Script {
    init {
        objectOperate("Open", DOORS) { (target) ->
            if (locked()) {
                message("The door is locked.") // TODO proper message
                return@objectOperate
            }
            Door.openDoor(this, target)
        }

        objectOperate("Pick-lock", DOORS) { (target) ->
            unlock(target, Skill.Thieving, "pick_lock", "You attempt to pick the lock...", "You fail to pick the lock.") // TODO proper messages
        }

        objectOperate("Force", DOORS) { (target) ->
            unlock(target, Skill.Strength, "force_lock", "You attempt to force the door...", "You fail to force the door.") // TODO proper messages
        }

        objectOperate("Open", "wooden_treasure_crate,oak_treasure_room_chest,teak_treasure_room_chest,mahogany_treasure_room_chest,magic_treasure_room_chest") {
            if (!inOwnHouse()) {
                // TODO open after killing the treasure room guardian in challenge mode
                message("The chest is locked.") // TODO proper message
                return@objectOperate
            }
            treasure()
        }
    }

    /**
     * Doors are only locked to guests in challenge mode
     */
    private fun Player.locked(): Boolean = !inOwnHouse() && houseOwner()?.get("house_challenge_mode", false) == true

    /**
     * Attempts to get through a locked door using [skill], the stronger the door the harder it is
     */
    private suspend fun Player.unlock(door: GameObject, skill: Skill, animation: String, attempt: String, fail: String) {
        if (!locked()) {
            message("The door isn't locked.") // TODO proper message
            return
        }
        message(attempt, ChatType.Filter)
        anim(animation)
        delay(2)
        val strength = doorLevels[DOORS.split(',').indexOf(door.id) / 2]
        if (random.nextInt(levels.get(skill) + 1) < random.nextInt(strength + 1)) {
            message(fail, ChatType.Filter)
            return
        }
        enterDoor(door)
    }

    /**
     * Lets the owner add coins to or take coins from the treasure kept in the treasure room
     */
    private suspend fun Player.treasure() {
        val treasure = get("house_treasure", 0)
        choice("The chest holds $treasure coins.") {
            // TODO proper message
            option("Add coins") {
                val amount = intEntry("How many coins would you like to add?").coerceAtMost(inventory.count("coins"))
                if (amount > 0 && inventory.remove("coins", amount)) {
                    set("house_treasure", get("house_treasure", 0) + amount)
                }
            }
            option("Take coins") {
                val stored = get("house_treasure", 0)
                val amount = intEntry("How many coins would you like to take?").coerceAtMost(stored)
                if (amount > 0 && inventory.add("coins", amount)) {
                    set("house_treasure", stored - amount)
                }
            }
            option("Cancel")
        }
    }

    companion object {
        private const val DOORS = "door_309_closed,door_310_closed,door_311_closed,door_312_closed,door_313_closed,door_314_closed"

        // Oak, steel-plated and marble door strengths guessed from other servers
        private val doorLevels = intArrayOf(35, 66, 80)
    }
}
