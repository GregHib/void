package content.area.karamja.brimhaven

import content.area.karamja.shilo_village.Vigroy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class Hajedy : Script {
    init {
        npcOperate("Talk-to", "hajedy_brimhaven") {
            offerCartRide()
        }

        npcOperate("Pay-fare", "hajedy_brimhaven") {
            Vigroy.payFare(this, Tile(2834, 2951))
        }

        objectOperate("Board", "travel_cart_brimhaven") {
            talkWith(NPCs.findBySpawn(Tile(2779, 3211), "hajedy_brimhaven"))
            offerCartRide()
        }

        objectOperate("Pay-fare", "travel_cart_brimhaven") {
            talkWith(NPCs.findBySpawn(Tile(2779, 3211), "hajedy_brimhaven"))
            Vigroy.payFare(this, Tile(2834, 2951))
        }
    }

    private suspend fun Player.offerCartRide() {
        npc<Neutral>("I am offering a cart ride to Shilo Village, if you're interested. It will cost ${Vigroy.CART_FARE} coins. Is that okay?")
        choice {
            option<Neutral>("Yes please, I'd like to go to Shilo Village.") {
                Vigroy.attemptPayment(this, "Shilo Village", Tile(2834, 2951))
            }
            option<Neutral>("No thanks.") {
                npc<Neutral>("Okay, Bwana, let me know if you change your mind.")
            }
        }
    }
}
