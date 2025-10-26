package content.area.kandarin.ardougne

import content.entity.player.bank.bank
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class Ardougne : Script {

    val mudpatch = listOf(
        Tile(2566, 3332, 0),
    )

    init {
        itemOnObjectOperate("bucket_of_water", "plague_mud") {
            if (quest("plague_city") == "about_digging") {
                anim("farming_pour_water")
                set("plague_city", "one_bucket_of_water")
                inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil.<br>The soil softens slightly.")
            } else if (quest("plague_city") == "one_bucket_of_water") {
                anim("farming_pour_water")
                set("plague_city", "two_bucket_of_water")
                inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil.<br>The soil softens slightly.")
            } else if (quest("plague_city") == "two_bucket_of_water") {
                anim("farming_pour_water")
                set("plague_city", "three_bucket_of_water")
                inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil.<br>The soil softens slightly.")
            } else if (quest("plague_city") == "three_bucket_of_water") {
                anim("farming_pour_water")
                set("plague_city", "four_bucket_of_water")
                inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil.<br>The soil is now soft enough to dig into..")
            } else if (quest("plague_city") == "four_bucket_of_water") {
                dig()
            } else {
                statement("You see no reason to do that at the moment.")
            }
        }

        itemOnObjectOperate("spade", "plague_mud") {
            if (quest("plague_city") == "four_bucket_of_water") {
                dig()
            } else {
                statement("You see no reason to do that at the moment.")
            }
        }

        itemOption("Dig", "spade") {
            val playerTile: Tile = tile
            anim("human_dig")
            if (mudpatch.contains(playerTile)) {
                if (quest("plague_city") == "four_bucket_of_water") {
                    dig()
                } else {
                    item("spade", 800, "You dig the soil... <br> The ground is rather hard.")
                }
            }
        }

        objectOperate("Open", "alrenas_cupboard_shut") { (target) ->
            message("You open the cupboard.")
            anim("human_pickupfloor")
            sound("cupboard_open")
            target.replace("alrenas_cupboard_open", ticks = TimeUnit.MINUTES.toTicks(1))
        }

        objectOperate("Search", "alrenas_cupboard_open") {
            if (quest("plague_city") != "unstarted" || quest("plague_city") != "started") {
                message("You search the wardrobe but you find nothing.")
            } else {
                if (inventory.contains("gas_mask")) {
                    message("You search the wardrobe but you find nothing.")
                } else if (bank.contains("gas_mask")) {
                    statement("The wardrobe is empty.")
                    player<Neutral>("I think I've still got one of Alrena's gas masks in my bank.")
                } else {
                    if (!inventory.add("gas_mask")) {
                        item("gas_mask", 300, "You find a protective mask but you don't have enough room to take it.")
                        return@objectOperate
                    }
                    item("gas_mask", 300, "You find a protective mask.")
                }
            }
        }
    }

    private suspend fun Player.dig() {
        open("fade_out")
        statement("You dig deep into the soft soil... Suddenly it crumbles away! You fall through into the sewer. Edmond follows you down the hole.", clickToContinue = false)
        delay(5)
        tele(Tile(2518, 9760, 0))
        set("plague_city", "sewer")
        set("plaguecity_dug_mud_pile", true)
        set("plaguecity_can_see_edmond_up_top", true)
        open("fade_in")
        statement("You dig deep into the soft soil... Suddenly it crumbles away! You fall through into the sewer. Edmond follows you down the hole.")
    }
}
