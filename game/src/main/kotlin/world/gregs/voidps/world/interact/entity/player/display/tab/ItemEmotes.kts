package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.awaitInterfaces
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.continueDialogue
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOptions
import world.gregs.voidps.world.interact.entity.player.toxin.curePoison
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned
import world.gregs.voidps.world.interact.entity.sound.playJingle

inventoryItem("Fly", "toy_kite") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playAnimation("emote_fly_kite")
}

inventoryItem("Emote", "reindeer_hat", "worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.setGraphic("emote_reindeer")
	player.setGraphic("emote_reindeer_2")
    player.playAnimation("emote_reindeer")
}

inventoryItem("Recite-prayer", "prayer_book", "inventory") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    if (player.poisoned) {
        val poisonDamage: Int = player["poison_damage"] ?: return@inventoryItem
        var points = (poisonDamage - 20) / 2
        var decrease = poisonDamage
        val prayer = player.levels.get(Skill.Prayer)
        if (points > prayer) {
            decrease = (prayer * 2) + 2
            points = prayer
        }
        if (points > 0) {
            player.levels.drain(Skill.Prayer, points)
            player["poison_damage"] = poisonDamage - decrease
            if (poisonDamage - decrease <= 10) {
                player.curePoison()
            }
        }
    }
    player.playAnimation("emote_recite_prayer")
}

playerOperate("Whack") {
     if (player.weapon.id == "rubber_chicken") {
         player.playSound("rubber_chicken_whack")
         player.playAnimation("rubber_chicken_whack")
    } else {
        //todo player.playSound("")
         player.playAnimation("easter_carrot_whack")
    }
}

inventoryItem("Dance", "rubber_chicken") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
	player.playJingle("easter_scape_scrambled")
    player.playAnimation("emote_chicken_dance")
}

inventoryItem("Spin", "spinning_plate", "inventory") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    val drop = random.nextBoolean()
    player.playAnimation("emote_spinning_plate")
    player.playAnimation("emote_spinning_plate_${if (drop) "drop" else "take"}")
    player.playAnimation("emote_${if (drop) "cry" else "cheer"}")
}

continueDialogue("snow_globe", "continue") { player ->
    player.close("snow_globe")
    player.continueDialogue()
}

inventoryItem("Shake", "snow_globe", "inventory") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.queue("snow_globe") {
        player.message("You shake the snow globe.")
        player.playAnimation("emote_shake_snow_globe")
        player.playJingle("harmony_snow_globe")
        player.open("snow_globe")
        awaitInterfaces()
        player.setGraphic("emote_snow_globe_flurry")
        player.playAnimation("emote_trample_snow")
        player.message("The snow globe fills your inventory with snow!")
        player.inventory.add("snowball_2007_christmas_event", player.inventory.spaces)
        player.clearAnimation()
        player.closeDialogue()
    }
}

inventoryOptions("Play", "Loop", "Walk", "Crazy", item = "yo_yo", inventory = "inventory") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.playAnimation("emote_yoyo_${option.lowercase()}")
}

inventoryItem("Spin","candy_cane","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playAnimation("emote_candy_cane_spin")
}

inventoryItem("Dance","salty_claws_hat","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playAnimation("emote_salty_claws_hat_dance")
}

inventoryItem("Celebrate", "tenth_anniversary_cake") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.setGraphic("10th_anniversary_cake")
    player.playAnimation("emote_10th_anniversary_cake")
}

inventoryItem("Brandish (2009)","golden_hammer","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playAnimation("emote_golden_hammer_brandish")
}

inventoryItem("Spin (2010)","golden_hammer","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.setGraphic("emote_golden_hammer_spin")
    player.playAnimation("emote_golden_hammer_spin")
}

inventoryOptions("Jump", "Walk", "Bow", "Dance", item = "*_marionette"  , inventory = "inventory") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.setGraphic("emote_${item.id}_${option.lowercase()}")
    player.playAnimation("emote_marionette_${option.lowercase()}")
}

inventoryItem("Sleuth","magnifying_glass","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playAnimation("emote_magnifying_glass_sleuth")
}

inventoryItem("Emote","chocatrice_cape","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.setGraphic("emote_chocatrice_cape")
    player.playAnimation("emote_chocatrice_cape")
}

inventoryItem("Juggle","squirrel_ears","worn_equipment") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.setGraphic("emote_squirrel_ears")
    player.playAnimation("emote_squirrel_ears")
}

inventoryItem("Play-with", "toy_horsey_*") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
  //  when (random.nextInt(0, 3)) {
   //     0 -> {
   //         player.forceChat = "Come on Dobbin, we can win the race!"
   //     }
    //    1 -> {
    //        player.forceChat = "Hi-ho Silver, and away!"
    //    }
    //    2 -> {
    //        player.forceChat = "Neaahhhyyy! Giddy-up horsey!"
    //    }
   // }
    player.forceChat = "Just say neigh to gambling!"
    player.playAnimation("emote_${item.id.lowercase()}")
}

inventoryOptions("Play-with", item = "eek") {
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.setGraphic("play_with_eek")
    player.playAnimation("play_with_eek")
}

inventoryItem("Summon Minion","squirrel_ears","worn_equipment") {
    //todo summon npc 9682 and 9681 if dismiss have to wait 30mins before able to summon again
}