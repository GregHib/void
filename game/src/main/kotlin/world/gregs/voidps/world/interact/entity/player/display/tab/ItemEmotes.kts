package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue.ContinueDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.awaitInterfaces
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.continueDialogue
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption
import world.gregs.voidps.world.interact.entity.player.toxin.curePoison
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned
import world.gregs.voidps.world.interact.entity.sound.playJingle

on<InventoryOption>({ item.id == "toy_kite" && option == "Fly" }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    player.playAnimation("emote_fly_kite")
}

on<InventoryOption>({ inventory == "worn_equipment" && item.id == "reindeer_hat" && option == "Emote" }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    player.setGraphic("emote_reindeer")
    player.playAnimation("emote_reindeer")
}

on<InventoryOption>({ inventory == "inventory" && item.id == "prayer_book" && option == "Recite-prayer" }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    if (player.poisoned) {
        val poisonDamage: Int = player["poison_damage"] ?: return@on
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

on<InventoryOption>({ item.id == "rubber_chicken" && option == "Dance" }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    player.playAnimation("emote_chicken_dance")
}

on<InventoryOption>({ inventory == "inventory" && item.id == "spinning_plate" && option == "Spin" }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    val drop = random.nextBoolean()
    player.playAnimation("emote_spinning_plate")
    player.playAnimation("emote_spinning_plate_${if (drop) "drop" else "take"}")
    player.playAnimation("emote_${if (drop) "cry" else "cheer"}")
}

on<ContinueDialogue>({ id == "snow_globe" && component == "continue" }) { player: Player ->
    player.close("snow_globe")
    player.continueDialogue()
}

on<InventoryOption>({ inventory == "inventory" && item.id == "snow_globe" && option == "Shake" }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
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

on<InventoryOption>({ inventory == "inventory" && item.id == "yo_yo" && (option == "Play" || option == "Loop" || option == "Walk" || option == "Crazy") }) { player: Player ->
    if (player.hasClock("emote_delay")) {
        player.message("Please wait till you've finished performing your current emote.")
        return@on
    }
    player.playAnimation("emote_yoyo_${option.lowercase()}")
}