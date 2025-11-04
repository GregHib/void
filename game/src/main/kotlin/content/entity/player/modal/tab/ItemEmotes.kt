package content.entity.player.modal.tab

import content.entity.effect.toxin.curePoison
import content.entity.effect.toxin.poisoned
import content.entity.sound.jingle
import content.entity.sound.sound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random

class ItemEmotes : Script {

    init {
        itemOption("Fly", "toy_kite", "*") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            animDelay("emote_fly_kite")
        }

        itemOption("Emote", "reindeer_hat", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            gfx("emote_reindeer")
            gfx("emote_reindeer_2")
            animDelay("emote_reindeer")
        }

        itemOption("Recite-prayer", "prayer_book") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            if (poisoned) {
                val poisonDamage: Int = get("poison_damage") ?: return@itemOption
                var points = (poisonDamage - 20) / 2
                var decrease = poisonDamage
                val prayer = levels.get(Skill.Prayer)
                if (points > prayer) {
                    decrease = (prayer * 2) + 2
                    points = prayer
                }
                if (points > 0) {
                    levels.drain(Skill.Prayer, points)
                    set("poison_damage", poisonDamage - decrease)
                    if (poisonDamage - decrease <= 10) {
                        curePoison()
                    }
                }
            }
            animDelay("emote_recite_prayer")
        }

        playerOperate("Whack") {
            if (weapon.id == "rubber_chicken") {
                sound("rubber_chicken_whack")
                animDelay("rubber_chicken_whack")
            } else {
                // todo player.playSound("")
                animDelay("easter_carrot_whack")
            }
        }

        itemOption("Dance", "rubber_chicken", "*") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            jingle("easter_scape_scrambled")
            animDelay("emote_chicken_dance")
        }

        itemOption("Spin", "spinning_plate") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            val drop = random.nextBoolean()
            animDelay("emote_spinning_plate")
            animDelay("emote_spinning_plate_${if (drop) "drop" else "take"}")
            animDelay("emote_${if (drop) "cry" else "cheer"}")
        }

        continueDialogue("snow_globe", "continue") { player ->
            player.close("snow_globe")
        }

        interfaceClose("snow_globe") {
            queue("snow_globe_close") {
                gfx("emote_snow_globe_flurry")
                val ticks = anim("emote_trample_snow")
                pause(ticks)
                message("The snow globe fills your inventory with snow!")
                inventory.add("snowball_2007_christmas_event", inventory.spaces)
                clearAnim()
                closeDialogue()
            }
        }

        itemOption("Shake", "snow_globe") {
            if (contains("delay") || menu != null) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            queue("snow_globe") {
                message("You shake the snow globe.")
                animDelay("emote_shake_snow_globe")
                jingle("harmony_snow_globe")
                open("snow_globe")
            }
        }

        for (option in listOf("Play", "Loop", "Walk", "Crazy")) {
            itemOption(option, "yo_yo") {
                yoyo(this, option)
            }
        }

        itemOption("Spin", "candy_cane", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            animDelay("emote_candy_cane_spin")
        }

        itemOption("Dance", "salty_claws_hat", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            animDelay("emote_salty_claws_hat_dance")
        }

        itemOption("Celebrate", "tenth_anniversary_cake") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            gfx("10th_anniversary_cake")
            animDelay("emote_10th_anniversary_cake")
        }

        itemOption("Brandish (2009)", "golden_hammer", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            animDelay("emote_golden_hammer_brandish")
        }

        itemOption("Spin (2010)", "golden_hammer", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            gfx("emote_golden_hammer_spin")
            animDelay("emote_golden_hammer_spin")
        }

        for (option in listOf("Jump", "Walk", "Bow", "Dance")) {
            itemOption(option, "*_marionette") {
                marionette(this, it.item, option)
            }
        }

        itemOption("Sleuth", "magnifying_glass", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            animDelay("emote_magnifying_glass_sleuth")
        }

        itemOption("Emote", "chocatrice_cape", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            gfx("emote_chocatrice_cape")
            animDelay("emote_chocatrice_cape")
        }

        itemOption("Juggle", "squirrel_ears", "worn_equipment") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            gfx("emote_squirrel_ears")
            animDelay("emote_squirrel_ears")
        }

        itemOption("Summon Minion", "squirrel_ears") {
            // todo summon npc 9682 and 9681 if dismiss have to wait 30mins before able to summon again
        }

        itemOption("Play-with", "toy_horsey_*") { (item) ->
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            say(
                when (random.nextInt(0, 3)) {
                    0 -> "Come on Dobbin, we can win the race!"
                    1 -> "Hi-ho Silver, and away!"
                    else -> "Neaahhhyyy! Giddy-up horsey!"
                },
            )
            //    say("Just say neigh to gambling!")
            animDelay("emote_${item.id}")
        }

        itemOption("Play-with", "eek", "*") {
            if (contains("delay")) {
                message("Please wait till you've finished performing your current emote.")
                return@itemOption
            }
            gfx("play_with_eek")
            animDelay("play_with_eek")
        }
    }

    suspend fun yoyo(player: Player, option: String) {
        if (player.contains("delay")) {
            player.message("Please wait till you've finished performing your current emote.")
            return
        }
        player.animDelay("emote_yoyo_${option.lowercase()}")
    }

    suspend fun marionette(player: Player, item: Item, option: String) {
        if (player.contains("delay")) {
            player.message("Please wait till you've finished performing your current emote.")
            return
        }
        player.gfx("emote_${item.id}_${option.lowercase()}")
        player.animDelay("emote_marionette_${option.lowercase()}")
    }
}
