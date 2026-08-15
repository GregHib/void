package content.area.asgarnia.taverley

import content.entity.obj.door.enterDoor
import content.quest.quest
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class TaverleyDungeon : Script {

    val leftSpawn = Tile(2887, 9832)
    val rightSpawn = Tile(2887, 9829)

    init {
        objectOperate("Open", "door_taverley_1_closed,door_taverley_2_closed") { (target) ->
            if (tile.x >= 2889 || !spawn(this, leftSpawn) && !spawn(this, rightSpawn)) {
                enterDoor(target)
            }
        }

        itemOnObjectOperate("raw_beef", "cauldron_of_thunder", handler = ::dip)
        itemOnObjectOperate("raw_rat_meat", "cauldron_of_thunder", handler = ::dip)
        itemOnObjectOperate("raw_bear_meat", "cauldron_of_thunder", handler = ::dip)
        itemOnObjectOperate("raw_chicken", "cauldron_of_thunder", handler = ::dip)

        objectOperate("Squeeze-through", "taverly_dungeon_pipe_sc") { (target) ->
            if (!has(Skill.Agility, 70)) {
                message("You need an Agility level of 70 to squeeze through the pipe.")
                return@objectOperate
            }
            val dir = if (target.tile.x < 2889) Direction.EAST else Direction.WEST
            walkToDelay(Tile(if (dir == Direction.EAST) 2886 else 2892, 9799))
            exactMove(Tile(2889, 9799), startDelay = 30, delay = 96, direction = dir)
            anim("climb_through_pipe")
            delay(3)
            exactMove(Tile(if (dir == Direction.EAST) 2892 else 2886, 9799), startDelay = 30, delay = 96, direction = dir)
            delay(1)
            anim("climb_through_pipe")
            delay(2)
            exp(Skill.Agility, 10.0)
        }
    }

    fun spawn(player: Player, tile: Tile): Boolean {
        val armour = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return false
        armour.remove(TimeUnit.MINUTES.toTicks(5))
        NPCs.add("suit_of_armour", armour.tile, ticks = TimeUnit.MINUTES.toTicks(5))
        player.message("Suddenly the suit of armour comes to life!")
        //    suit.setAnimation("suit_of_armour_stand") TODO find animation
        return true
    }

    fun dip(player: Player, interact: ItemOnObjectInteract) {
        val required = interact.item.id
        if (player.quest("druidic_ritual") == "cauldron") {
            if (player.inventory.replace(required, required.replace("raw_", "enchanted_"))) {
                player.message("You dip the ${required.toLowerSpaceCase()} in the cauldron.")
            }
        } else {
            player.noInterest()
        }
    }
}
