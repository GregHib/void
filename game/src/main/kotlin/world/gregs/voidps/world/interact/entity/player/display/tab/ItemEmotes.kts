package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.player.playerOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOptions
import world.gregs.voidps.world.interact.entity.player.toxin.curePoison
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playSound

inventoryItem("Fly", "toy_kite") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animate("emote_fly_kite")
}

inventoryItem("Emote", "reindeer_hat", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_reindeer")
    player.gfx("emote_reindeer_2")
    player.animate("emote_reindeer")
}

inventoryItem("Recite-prayer", "prayer_book", "inventory") {
    if (player.contains("delay")) {
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
    player.animate("emote_recite_prayer")
}

playerOperate("Whack") {
    if (player.weapon.id == "rubber_chicken") {
        player.playSound("rubber_chicken_whack")
        player.animate("rubber_chicken_whack")
    } else {
        //todo player.playSound("")
        player.animate("easter_carrot_whack")
    }
}

inventoryItem("Dance", "rubber_chicken") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playJingle("easter_scape_scrambled")
    player.animate("emote_chicken_dance")
}

inventoryItem("Spin", "spinning_plate", "inventory") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    val drop = random.nextBoolean()
    player.animate("emote_spinning_plate")
    player.animate("emote_spinning_plate_${if (drop) "drop" else "take"}")
    player.animate("emote_${if (drop) "cry" else "cheer"}")
}

continueDialogue("snow_globe", "continue") { player ->
    player.close("snow_globe")
}

interfaceClose("snow_globe") { player ->
    player.queue("snow_globe_close") {
        player.gfx("emote_snow_globe_flurry")
        val ticks = player.setAnimation("emote_trample_snow")
        pause(ticks)
        player.message("The snow globe fills your inventory with snow!")
        player.inventory.add("snowball_2007_christmas_event", player.inventory.spaces)
        player.clearAnimation()
        player.closeDialogue()
    }
}

inventoryItem("Shake", "snow_globe", "inventory") {
    if (player.contains("delay") || player.menu != null) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.queue("snow_globe") {
        player.message("You shake the snow globe.")
        player.animate("emote_shake_snow_globe")
        player.playJingle("harmony_snow_globe")
        player.open("snow_globe")
    }
}

inventoryOptions("Play", "Loop", "Walk", "Crazy", item = "yo_yo", inventory = "inventory") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.animate("emote_yoyo_${option.lowercase()}")
}

inventoryItem("Spin", "candy_cane", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animate("emote_candy_cane_spin")
}

inventoryItem("Dance", "salty_claws_hat", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animate("emote_salty_claws_hat_dance")
}

inventoryItem("Celebrate", "tenth_anniversary_cake") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("10th_anniversary_cake")
    player.animate("emote_10th_anniversary_cake")
}

inventoryItem("Brandish (2009)", "golden_hammer", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animate("emote_golden_hammer_brandish")
}

inventoryItem("Spin (2010)", "golden_hammer", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_golden_hammer_spin")
    player.animate("emote_golden_hammer_spin")
}

inventoryOptions("Jump", "Walk", "Bow", "Dance", item = "*_marionette", inventory = "inventory") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.gfx("emote_${item.id}_${option.lowercase()}")
    player.animate("emote_marionette_${option.lowercase()}")
}

inventoryItem("Sleuth", "magnifying_glass", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animate("emote_magnifying_glass_sleuth")
}

inventoryItem("Emote", "chocatrice_cape", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_chocatrice_cape")
    player.animate("emote_chocatrice_cape")
}

inventoryItem("Juggle", "squirrel_ears", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_squirrel_ears")
    player.animate("emote_squirrel_ears")
}

inventoryItem("Play-with", "toy_horsey_*") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.say(when (random.nextInt(0, 3)) {
        0 -> "Come on Dobbin, we can win the race!"
        1 -> "Hi-ho Silver, and away!"
        else -> "Neaahhhyyy! Giddy-up horsey!"
    })
//    player.say("Just say neigh to gambling!")
    player.animate("emote_${item.id}")
}

inventoryOptions("Play-with", item = "eek") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.gfx("play_with_eek")
    player.animate("play_with_eek")
}

inventoryItem("Summon Minion", "squirrel_ears", "worn_equipment") {
    //todo summon npc 9682 and 9681 if dismiss have to wait 30mins before able to summon again
}