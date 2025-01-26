package content.entity.player.modal.tab

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.playerOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random
import content.skill.melee.weapon.weapon
import content.entity.player.inv.inventoryItem
import content.entity.player.inv.inventoryOptions
import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.entity.sound.playJingle
import content.entity.sound.playSound

inventoryItem("Fly", "toy_kite") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animDelay("emote_fly_kite")
}

inventoryItem("Emote", "reindeer_hat", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_reindeer")
    player.gfx("emote_reindeer_2")
    player.animDelay("emote_reindeer")
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
    player.animDelay("emote_recite_prayer")
}

playerOperate("Whack") {
    if (player.weapon.id == "rubber_chicken") {
        player.playSound("rubber_chicken_whack")
        player.animDelay("rubber_chicken_whack")
    } else {
        //todo player.playSound("")
        player.animDelay("easter_carrot_whack")
    }
}

inventoryItem("Dance", "rubber_chicken") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.playJingle("easter_scape_scrambled")
    player.animDelay("emote_chicken_dance")
}

inventoryItem("Spin", "spinning_plate", "inventory") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    val drop = random.nextBoolean()
    player.animDelay("emote_spinning_plate")
    player.animDelay("emote_spinning_plate_${if (drop) "drop" else "take"}")
    player.animDelay("emote_${if (drop) "cry" else "cheer"}")
}

continueDialogue("snow_globe", "continue") { player ->
    player.close("snow_globe")
}

interfaceClose("snow_globe") { player ->
    player.queue("snow_globe_close") {
        player.gfx("emote_snow_globe_flurry")
        val ticks = player.anim("emote_trample_snow")
        pause(ticks)
        player.message("The snow globe fills your inventory with snow!")
        player.inventory.add("snowball_2007_christmas_event", player.inventory.spaces)
        player.clearAnim()
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
        player.animDelay("emote_shake_snow_globe")
        player.playJingle("harmony_snow_globe")
        player.open("snow_globe")
    }
}

inventoryOptions("Play", "Loop", "Walk", "Crazy", item = "yo_yo", inventory = "inventory") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.animDelay("emote_yoyo_${option.lowercase()}")
}

inventoryItem("Spin", "candy_cane", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animDelay("emote_candy_cane_spin")
}

inventoryItem("Dance", "salty_claws_hat", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animDelay("emote_salty_claws_hat_dance")
}

inventoryItem("Celebrate", "tenth_anniversary_cake") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("10th_anniversary_cake")
    player.animDelay("emote_10th_anniversary_cake")
}

inventoryItem("Brandish (2009)", "golden_hammer", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animDelay("emote_golden_hammer_brandish")
}

inventoryItem("Spin (2010)", "golden_hammer", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_golden_hammer_spin")
    player.animDelay("emote_golden_hammer_spin")
}

inventoryOptions("Jump", "Walk", "Bow", "Dance", item = "*_marionette", inventory = "inventory") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.gfx("emote_${item.id}_${option.lowercase()}")
    player.animDelay("emote_marionette_${option.lowercase()}")
}

inventoryItem("Sleuth", "magnifying_glass", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.animDelay("emote_magnifying_glass_sleuth")
}

inventoryItem("Emote", "chocatrice_cape", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_chocatrice_cape")
    player.animDelay("emote_chocatrice_cape")
}

inventoryItem("Juggle", "squirrel_ears", "worn_equipment") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryItem
    }
    player.gfx("emote_squirrel_ears")
    player.animDelay("emote_squirrel_ears")
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
    player.animDelay("emote_${item.id}")
}

inventoryOptions("Play-with", item = "eek") {
    if (player.contains("delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@inventoryOptions
    }
    player.gfx("play_with_eek")
    player.animDelay("play_with_eek")
}

inventoryItem("Summon Minion", "squirrel_ears", "worn_equipment") {
    //todo summon npc 9682 and 9681 if dismiss have to wait 30mins before able to summon again
}