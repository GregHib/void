package content.minigame.pest_control

import content.entity.npc.shop.openShop
import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * The squires around the Void Knights' Outpost: shopkeepers, lander squires and the ship back to Port Sarim.
 */
class OutpostSquires : Script {

    init {
        npcOperate("Talk-to", "squire_rangeshop_pest_control") {
            trader("void_knight_archery_store")
        }

        npcOperate("Talk-to", "squire_runeshop_pest_control") {
            trader("void_knight_magic_store")
        }

        npcOperate("Talk-to", "squire_storemaster_pest_control") {
            trader("void_knight_general_store")
        }

        npcOperate("Talk-to", "squire_novice_pest_control,squire_intermediate_pest_control,squire_veteran_pest_control") { (target) ->
            val lander = target.id.removePrefix("squire_").removeSuffix("_pest_control")
            npc<Neutral>("This is the $lander lander. Board it by crossing the gangplank and wait for it to depart to the island.") // TODO proper message
        }

        npcOperate("Talk-to", "squire_ship_island_pest_control") {
            npc<Happy>("Hi, how can I help you?")
            choice {
                option<Quiz>("I'd like to go back to Port Sarim please.") {
                    npc<Neutral>("Ok, but please come back soon and help us.")
                    travel()
                }
                option<Neutral>("I'm fine thanks.")
            }
        }

        npcOperate("Travel", "squire_ship_island_pest_control") {
            travel()
        }
    }

    private suspend fun Player.trader(shop: String) {
        npc<Happy>("Hi, how can I help you?")
        choice {
            option("What do you have for sale?") {
                openShop(shop)
            }
            option<Neutral>("I'm fine thanks.")
        }
    }

    private suspend fun Player.travel() {
        boatTravel("ape_atoll_to_port_sarim", 10, Tile(3041, 3199, 1))
        statement("The ship arrives at Port Sarim.")
    }
}
