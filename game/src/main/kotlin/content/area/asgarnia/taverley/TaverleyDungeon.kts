package content.area.asgarnia.taverley

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.entity.obj.door.enterDoor
import java.util.concurrent.TimeUnit

val npcs: NPCs by inject()
val objects: GameObjects by inject()

val leftSpawn = Tile(2887, 9832)
val rightSpawn = Tile(2887, 9829)

objectOperate("Open", "door_taverley_1_closed", "door_taverley_2_closed") {
    if (player.tile.x >= 2889 || !spawn(player, leftSpawn) && !spawn(player, rightSpawn)) {
        enterDoor(target)
    }
}

fun spawn(player: Player, tile: Tile): Boolean {
    val armour = objects.getLayer(tile, ObjectLayer.GROUND) ?: return false
    armour.remove(TimeUnit.MINUTES.toTicks(5))
    val suit = npcs.add("suit_of_armour", armour.tile) ?: return false
    player.message("Suddenly the suit of armour comes to life!")
//    suit.setAnimation("suit_of_armour_stand") TODO find animation
    suit.softQueue("despawn", TimeUnit.MINUTES.toTicks(5)) {
        World.queue("despawn_${suit.index}") {
            npcs.remove(suit)
            npcs.removeIndex(suit)
        }
    }
    return true
}


itemOnObjectOperate("raw_beef", "cauldron_of_thunder") {
    dip(player, item.id)
}

itemOnObjectOperate("raw_rat_meat", "cauldron_of_thunder") {
    dip(player, item.id)
}

itemOnObjectOperate("raw_bear_meat", "cauldron_of_thunder") {
    dip(player, item.id)
}

itemOnObjectOperate("raw_chicken", "cauldron_of_thunder") {
    dip(player, item.id)
}

fun dip(player: Player, required: String) {
    if (player.quest("druidic_ritual") == "cauldron") {
        if (player.inventory.replace(required, required.replace("raw_", "enchanted_"))) {
            player.message("You dip the ${required.toLowerSpaceCase()} in the cauldron.")
        }
    } else {
        player.noInterest()
    }
}