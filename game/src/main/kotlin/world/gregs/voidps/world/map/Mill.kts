package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

on<ObjectOption>({ operate && obj.id == "hopper_controls" && option == "Operate" }) { player: Player ->
    if (player["flour_bin", 0] == 30) {
        player.message("The flour bin downstairs is full, I should empty it first.")
        return@on
    }
    arriveDelay()
    if (player["hopper_bin", 0] != 1) {
        player.setAnimation("pull_hopper_controls")
        player.playSound("lever")
        obj.animate("3572")// todo find right anim
        player.message("You operate the empty hopper. Nothing interesting happens.")
        return@on
    }
    player.setAnimation("pull_hopper_controls")
    player.playSound("lever")
    obj.animate("3568")// todo find right anim
    player["hopper_bin"] = 0
    player.inc("flour_bin")
    if (player["flour_bin", 0] == 30) {
        player.message("The flour bin downstairs is now full.")
    } else {
        player.message("You operate the hopper. The grain slides down the chute.")
    }
}

on<ItemOnObject>({ operate && obj.id == "hopper" && item.id == "grain" }) { player: Player ->
    arriveDelay()
    if (player["cooks_assistant", "unstarted"] != "started") {
        player.setAnimation("fill_hopper")
        player.inventory.remove("grain")
        player["hopper_bin"] = 1
        player.message("You put the grain in the hopper. You should now pull the lever nearby to operate the hopper.")
        return@on
    }
    if (player["cooks_assistant_talked_to_millie", 0] == 0) {
        player<Talk>("""
            Hmm. I should probably ask that lady downstairs how I can
            make extra fine flour.
        """)
        return@on
    }
    if (player.hasItem("extra_fine_flour")) {
        player.message("It'd be best to take the extra fine flour you already have to the cook first.")
        return@on
    }
    if (player.bank.contains("extra_fine_flour")) {
        player.message("It'd be best to take the extra fine flour you already have in your bank to the cook first.")
        return@on
    }
    if (player["hopper_bin", 0] == 1) {
        player.message("There is already grain in the hopper.")
    } else {
        player.setAnimation("fill_hopper")
        player.inventory.remove("grain")
        player["hopper_bin"] = 1
        player.message("You put the grain in the hopper. You should now pull the lever nearby to operate the hopper.")
    }
}

on<ObjectOption>({ operate && obj.id == "flour_bin_3" && option == "Take-flour" }) { player: Player ->
    if (!player.hasItem("empty_pot")) {
        player.message("You need an empty pot to hold the flour in.")
        return@on
    }
    arriveDelay()
    if (player["cooks_assistant", "unstarted"] == "started" && player["cooks_assistant_talked_to_millie", 0] == 1) {
        player.inventory.remove("empty_pot")
        if (player.hasItem("extra_fine_flour") || player.bank.contains("extra_fine_flour")) {
            player.inventory.add("pot_of_flour")
            player.message("You fill a pot with flour from the bin.")
        } else {
            player.inventory.add("extra_fine_flour")
            player.message("You fill a pot with the extra fine flour from the bin.")
        }
        player.dec("flour_bin")
    } else {
        player.inventory.remove("empty_pot")
        player.inventory.add("pot_of_flour")
        player.dec("flour_bin")
        player.message("You fill a pot with flour from the bin.")
    }
}

on<Registered> { player: Player ->
    player.sendVariable("flour_bin")
}