package content.area.kandarin.ardougne

import content.entity.player.bank.bank
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.player.inv.inventoryItem
import content.entity.sound.sound
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

@Script
class Ardougne {

    val mudpatch = listOf(
        Tile(2566, 3332, 0),
    )

    init {
        itemOnObjectOperate("bucket_of_water", "plaguemudpatch2") {
            if (player.quest("plague_city") == "about_digging") {
                player.anim("farming_pour_water")
                player["plague_city"] = "one_bucket_of_water"
                player.inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil. <br> The soil softens slightly.")
            } else if (player.quest("plague_city") == "one_bucket_of_water") {
                player.anim("farming_pour_water")
                player["plague_city"] = "two_bucket_of_water"
                player.inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil. <br> The soil softens slightly.")
            } else if (player.quest("plague_city") == "two_bucket_of_water") {
                player.anim("farming_pour_water")
                player["plague_city"] = "three_bucket_of_water"
                player.inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil. <br> The soil softens slightly.")
            } else if (player.quest("plague_city") == "three_bucket_of_water") {
                player.anim("farming_pour_water")
                player["plague_city"] = "four_bucket_of_water"
                player.inventory.replace("bucket_of_water", "bucket")
                statement("You pour water onto the soil. <br> The soil is now soft enough to dig into..")
            } else if (player.quest("plague_city") == "four_bucket_of_water") {
                dig()
            } else {
                statement("You see no reason to do that at the moment.")
            }
        }

        itemOnObjectOperate("spade", "plaguemudpatch2") {
            if (player.quest("plague_city") == "four_bucket_of_water") {
                dig()
            } else {
                statement("You see no reason to do that at the moment.")
            }
        }

        inventoryItem("Dig", "spade") {
            val playerTile: Tile = player.tile
            player.anim("dig_with_spade")
            if (mudpatch.contains(playerTile)) {
                if (player.quest("plague_city") == "four_bucket_of_water") {
                    dig()
                } else {
                    item("spade", 800, "You dig the soil... <br> The ground is rather hard.")
                }
            }
        }

        objectOperate("Open", "alrenascupboardshut") {
            player.message("You open the cupboard.")
            player.anim("human_pickupfloor")
            player.sound("cupboard_open")
            target.replace("alrenascupboardopen", ticks = TimeUnit.MINUTES.toTicks(1))
        }

        objectOperate("Search", "alrenascupboardopen") {
            if (player.quest("plague_city") != "unstarted" || player.quest("plague_city") != "started") {
                player.message("You search the wardrobe but you find nothing.")
            } else {
                if (player.inventory.contains("gas_mask")) {
                    player.message("You search the wardrobe but you find nothing.")
                } else if (player.bank.contains("gas_mask")) {
                    statement("The wardrobe is empty.")
                    player<Neutral>("I think I've still got one of Alrena's gas masks in my bank.")
                } else {
                    if (!player.inventory.add("gas_mask")) {
                        item("gas_mask", 300, "You find a protective mask but you don't have enough room to take it.")
                        return@objectOperate
                    }
                    item("gas_mask", 300, "You find a protective mask.")
                }
            }
        }
    }

    private suspend fun SuspendableContext<Player>.dig() {
        player.open("fade_out")
        statement("You dig deep into the soft soil... Suddenly it crumbles away! You fall through into the sewer. Edmond follows you down the hole.", clickToContinue = false)
        delay(5)
        player.tele(Tile(2518, 9760, 0))
        player["plague_city"] = "sewer"
        player["plaguecity_dug_mud_pile"] = true
        player["plaguecity_can_see_edmond_up_top"] = true
        player.open("fade_in")
        statement("You dig deep into the soft soil... Suddenly it crumbles away! You fall through into the sewer. Edmond follows you down the hole.")
    }
}
