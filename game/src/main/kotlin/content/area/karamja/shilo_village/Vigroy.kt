package content.area.karamja.shilo_village

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

class Vigroy : Script {
    init {
        npcOperate("Talk-to", "hajedy_shilo_village") {
            offerCartRide()
        }

        npcOperate("Pay-fare", "hajedy_shilo_village") {
            payFare()
        }

        objectOperate("Board", "travel_cart_shilo_village") {
            talkWith(NPCs.findBySpawn(HAJEDY_SPAWN, "hajedy_shilo_village"))
            offerCartRide()
        }

        objectOperate("Pay-fare", "travel_cart_shilo_village") {
            talkWith(NPCs.findBySpawn(HAJEDY_SPAWN, "hajedy_shilo_village"))
            payFare()
        }
    }
}

private val HAJEDY_SPAWN = Tile(2812, 3095) // placeholder, swap for Hajedy's actual spawn tile
private const val CART_FARE = 30 // adjust to actual RS cart fare
private const val ARRIVAL_X = 2762 // placeholder Brimhaven coords, swap for actual arrival tile
private const val ARRIVAL_Z = 3187

/**
 * Talk-to/Board: full dialogue asking whether the player wants to travel.
 */
private suspend fun Player.offerCartRide() {
    npc<Neutral>("I am offering a cart ride to Brimhaven, if you're interested. It will cost $CART_FARE coins. Is that okay?")
    choice {
        option<Neutral>("Yes please, I'd like to go to Brimhaven.") {
            attemptPayment()
        }
        option<Neutral>("No thanks.") {
            npc<Neutral>("Okay, Bwana, let me know if you change your mind.")
        }
    }
}

/**
 * Pay-fare: skips the dialogue and attempts payment immediately.
 */
private suspend fun Player.payFare() {
    attemptPayment()
}

private suspend fun Player.attemptPayment() {
    inventory.transaction {
        remove("coins", CART_FARE)
    }
    when (inventory.transaction.error) {
        TransactionError.None -> {
            npc<Neutral>("You hop into the cart and the driver urges the horses on.")
            npc<Neutral>("You take a taxing journey through the jungle to Brimhaven.")
            npc<Neutral>("You feel tired from the journey, but at least you didn't have to walk all that distance.")

            open("fade_out")
            delay(3)
            tele(ARRIVAL_X, ARRIVAL_Z)
            open("fade_in")
        }
        else -> {
            npc<Neutral>("Sorry, but it looks as if you don't have enough money. Come and see me when you have enough for the ride.")
        }
    }
}