package content.skill.dungeoneering

import content.quest.instance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction

class DungeonDoors : Script {
    init {
        objectOperate("Enter", "*door_frozen,*door_abandoned,*door_furnished,*door_occult,*door_warped", handler = ::handleDoor)
        objectApproach("Enter", "*door_frozen,*door_abandoned,*door_furnished,*door_occult,*door_warped", handler = ::handleDoor)

        /*
            Locked doors
         */

        objectOperate("Unlock", "orange_*_door,silver_*_door,yellow_*_door,green_*_door,blue_*_door,purple_*_door,crimson_*_door,gold_*_door") { (target) ->
            val dungeon = dungeonMap ?: return@objectOperate
            val instance = instance() ?: return@objectOperate
            val origin = tile.delta(instance.tile)
            val roomTile = origin.room
            val room = dungeon.room(roomTile.x, roomTile.y) ?: return@objectOperate
            val door = room.doors[target.rotation] ?: return@objectOperate
            if (door !is DungeonDoor.Locked) {
                return@objectOperate
            }
            if (!inventory.remove(door.key)) {
                message("You don't have the correct key.")
                return@objectOperate
            }
            target.remove()
            anim("unlock_dung_door")
            message("You unlock the door.", ChatType.Filter)
        }

        objectOperate("Open", "orange_*_door_*,silver_*_door_*,yellow_*_door_*,green_*_door_*,blue_*_door_*,purple_*_door_*,crimson_*_door_*,gold_*_door_*", handler = ::handleDoor)
        objectApproach("Open", "orange_*_door_*,silver_*_door_*,yellow_*_door_*,green_*_door_*,blue_*_door_*,purple_*_door_*,crimson_*_door_*,gold_*_door_*", handler = ::handleDoor)

        /*
            Skill doors
         */

        objectOperate("Force-bar", "barred_door_*") { (target) ->
            target.replace(target.id.replace("barred_door", "unbarred_door"))
        }

        objectOperate("Imbue-energy", "runed_door_*") { (target) ->
            anim("bind_runes")
            gfx("bind_runes")
            delay(2)
            target.remove()
        }

        objectOperate("Repair", "collapsing_doorframe_*") { (target) ->
            anim("repair_collapsing_doorframe")
            delay(2)
            target.replace(target.id.replace("collapsing_doorframe", "repaired_door"))
        }

        objectOperate("Disarm", "locked_door_*") { (target) ->
            anim("disarm_locked_door")
            delay(2)
            target.replace(target.id.replace("locked_door", "unlocked_door"))
        }

        objectOperate("Pick-lock", "padlocked_door_*") { (target) ->
            anim("pick_padlocked_door")
            delay(2)
            target.replace(target.id.replace("padlocked_door", "picked_door"))
        }

        objectOperate("Fix-pulley", "broken_pulley_door_*") { (target) ->
            anim("disarm_locked_door")
            delay(2)
            target.replace(target.id.replace("broken_pulley", "fixed_pulley"))
        }

        objectOperate("Mine", "pile_of_rocks_*") { (target) ->
            target.remove()
        }

        objectOperate("Repair-key", "broken_key_door_*") { (target) ->
            anim("fix_broken_key_door")
            delay(2)
            target.replace(target.id.replace("broken_key_door", "fixed_key_door"))
        }

        objectOperate("Burn", "flammable_debris_*") { (target) ->
            target.remove()
        }

        objectOperate("Chop-down", "wooden_barricade_*") { (target) ->
            target.replace(target.id.replace("wooden_barricade", "cleared_barricade"))
        }

        objectOperate("Prune-vines", "vine_covered_door_*") { (target) ->
            anim("pruning_mid_high")
            delay(2)
            target.remove()
        }

        objectOperate("Dismiss", "ramokee_exile_*") { (target) ->
            anim("superheat_item")
            delay(2)
            target.remove()
        }

        objectOperate("Dispel", "magical_barrier_*") { (target) ->
            anim("lunar_cast")
            delay(2)
            // obj anim 13551, 13550
            target.remove()
        }

        objectOperate("Exorcise", "dark_spirit_*") { (target) ->
            anim("altar_pray")
            delay(2)
            target.remove() // obj anim 13557/13556
        }

        objectOperate("Add-compound", "liquid_lock_door_*") { (target) ->
            anim("open_liquid_lock_door")
            delay(2)
            target.replace(target.id.replace("liquid_lock_door", "exploded_liquid_door"))
        }
    }

    private suspend fun handleDoor(player: Player, interact: PlayerOnObjectInteract) {
        val target = interact.target
        val dir = direction(target) ?: return
        val under = GameObjects.at(target.tile.add(dir.inverse())).firstOrNull()
        if (under == null) {
            player.approachRange(1)
            player.openDoor(target)
            return
        }
        // TODO handle in method with delay and then can open after?
        if (under.id.endsWith("_door")) {
            player.interactObject(under, "Unlock")
        } else if (under.id.startsWith("runed_door_")) {
            player.interactObject(under, "Imbue-energy")
        } else if (under.id.startsWith("pile_of_rocks_")) {
            player.interactObject(under, "Mine")
        } else if (under.id.startsWith("flammable_debris_")) {
            player.interactObject(under, "Burn")
        } else if (under.id.startsWith("vine_covered_door_")) {
            player.interactObject(under, "Prune-vines")
        } else if (under.id.startsWith("ramokee_exile_")) {
            player.interactObject(under, "Dismiss")
        } else if (under.id.startsWith("magical_barrier_")) {
            player.interactObject(under, "Dispel")
        } else if (under.id.startsWith("dark_spirit_")) {
            player.interactObject(under, "Exorcise")
        } else {
            player.noInterest()
        }
    }

    val Delta.room: Delta
        get() = Delta(x / 16, y / 16)

    private fun Player.openDoor(target: GameObject) {
        val dungeon = dungeonMap ?: return
        val instance = instance() ?: return
        val origin = tile.delta(instance.tile)
        val roomTile = origin.room
        val direction = direction(target) ?: return
        val room = dungeon.room(roomTile.x, roomTile.y) ?: return
        val adj = room.adjacentRooms[target.rotation] ?: return
        if (!adj.open) {
            adj.open(this, dungeon)
            return
        }
        if (direction.isHorizontal()) {
            tele(tile.copy(x = target.tile.x + direction.delta.x * 2))
        } else {
            tele(tile.copy(y = target.tile.y + direction.delta.y * 2))
        }
    }

    private fun direction(target: GameObject): Direction? = when (target.rotation) {
        0 -> Direction.WEST
        1 -> Direction.NORTH
        2 -> Direction.EAST
        3 -> Direction.SOUTH
        else -> null
    }
}
