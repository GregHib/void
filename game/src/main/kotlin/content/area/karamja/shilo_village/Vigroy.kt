package content.area.karamja.shilo_village

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Vigroy : Script {
    init {
        npcOperate("Talk-to", "vigroy_shilo_village") {
            offerCartRide()
        }

        npcOperate("Pay-fare", "vigroy_shilo_village") {
            payFare(this, Tile(2778, 3210))
        }

        objectOperate("Board", "travel_cart_shilo_village") {
            talkWith(NPCs.findBySpawn(Tile(2834, 2954), "vigroy_shilo_village"))
            offerCartRide()
        }

        objectOperate("Pay-fare", "travel_cart_shilo_village") {
            talkWith(NPCs.findBySpawn(Tile(2834, 2954), "vigroy_shilo_village"))
            payFare(this, Tile(2778, 3210))
        }
    }

    /**
     * Talk-to/Board: full dialogue asking whether the player wants to travel.
     */
    private suspend fun Player.offerCartRide() {
        npc<Neutral>("I am offering a cart ride to Brimhaven, if you're interested. It will cost $CART_FARE coins. Is that okay?")
        choice {
            option<Neutral>("Yes please, I'd like to go to Brimhaven.") {
                attemptPayment(this, "Brimhaven", Tile(2778, 3210))
            }
            option<Neutral>("No thanks.") {
                npc<Neutral>("Okay, Bwana, let me know if you change your mind.")
            }
        }
    }

    companion object {

        const val CART_FARE = 10

        suspend fun payFare(player: Player, tile: Tile) {
            if (!player.inventory.remove("coins", CART_FARE)) {
                player.npc<Neutral>("Sorry, but it looks as if you don't have enough money. Come and see me when you have enough for the ride.")
                return
            }
            player.open("fade_out")
            player.delay(3)
            player.tele(tile)
            player.open("fade_in")
            player.message("You feel tired from the journey, but at least you didn't have to walk all that distance.", ChatType.Filter)
        }

        suspend fun attemptPayment(player: Player, location: String, tile: Tile) {
            if (!player.inventory.remove("coins", CART_FARE)) {
                player.npc<Neutral>("Sorry, but it looks as if you don't have enough money. Come and see me when you have enough for the ride.")
                return
            }
            player.statement("You hop into the cart and the driver urges the horses on. You take a taxing journey through the jungle to $location.", clickToContinue = false)
            player.open("fade_out")
            player.delay(3)
            player.tele(tile)
            player.open("fade_in")
            player.statement("You hop into the cart and the driver urges the horses on. You take a taxing journey through the jungle to $location. You feel tired from the journey, but at least you didn't have to walk all that distance.")
        }
    }
}
