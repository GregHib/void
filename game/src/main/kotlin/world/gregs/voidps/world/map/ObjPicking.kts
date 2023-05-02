package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

on<ObjectOption>({ obj.id == "wheat" || obj.id == "wheat_2" || obj.id == "wheat_3" || obj.id == "wheat_4" || obj.id == "wheat_5" && option == "Pick" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("grain")) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(30))
        player.message("You pick some wheat.")
    } else {
        player.inventoryFull()
    }
}

on<ObjectOption>({ obj.id == "onion" && option == "Pick" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("onion")) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(30))
        player.message("You pick an onion.")
    } else {
        player.inventoryFull()
    }
}

on<ObjectOption>({ obj.id == "cabbage_draynor_manor" && option == "Pick" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("cabbage_draynor_manor")) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(45))
        player.message("You pick a cabbage.")
    } else {
        player.inventoryFull()
    }
}

on<ObjectOption>({ obj.id == "cabbage" || obj.id == "cabbage_2" && option == "Pick" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("cabbage")) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(45))
        player.message("You pick a cabbage.")
    } else {
        player.inventoryFull()
    }
}

on<ObjectOption>({ obj.id == "potato" && option == "Pick" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("raw_potato")) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(30))
        player.message("You pick a potato.")
    } else {
        player.inventoryFull()
    }
}

on<ObjectOption>({ obj.id == "flax" && option == "Pick" }, Priority.HIGH) { player: Player ->
    if (player.inventory.add("flax")) {
        player.setAnimation("climb_down")
        obj.remove(TimeUnit.SECONDS.toTicks(5))
        player.message("You pick some flax.")
    } else {
        player.inventoryFull()
    }
}