package content.area.asgarnia.taverley

import content.entity.obj.door.enterDoor
import content.quest.quest
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class TaverleyDungeon(
    val npcs: NPCs,
    val objects: GameObjects,
) : Script {

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
    }

    fun spawn(player: Player, tile: Tile): Boolean {
        val armour = objects.getLayer(tile, ObjectLayer.GROUND) ?: return false
        armour.remove(TimeUnit.MINUTES.toTicks(5))
        val suit = npcs.add("suit_of_armour", armour.tile)
        player.message("Suddenly the suit of armour comes to life!")
        //    suit.setAnimation("suit_of_armour_stand") TODO find animation
        suit.softQueue("despawn", TimeUnit.MINUTES.toTicks(5)) {
            World.queue("despawn_${suit.index}") {
                npcs.remove(suit)
            }
        }
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
