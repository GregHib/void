package content.entity.obj.ship

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player

val locations = listOf(
    "catherby",
    "brimhaven",
    "port_khazard",
    "port_sarim",
)

interfaceOpen("charter_ship_map") { player ->
    refresh(player)
}

fun refresh(player: Player) {
    player.interfaces.sendVisibility("charter_ship_map", "mos_le_harmless", player.questCompleted("mos_le_harmless"))
    player.interfaces.sendVisibility("charter_ship_map", "shipyard", player.questCompleted("the_grand_tree"))
    player.interfaces.sendVisibility("charter_ship_map", "port_tyras", player.questCompleted("regicide"))
    player.interfaces.sendVisibility("charter_ship_map", "port_phasmatys", player.questCompleted("priest_in_peril"))
    player.interfaces.sendVisibility("charter_ship_map", "oo_glog", player.questCompleted("as_a_first_resort"))
    player.interfaces.sendVisibility("charter_ship_map", "crandor", false)
    player.interfaces.sendVisibility("charter_ship_map", "karamja", false)

    val currentLocation = player["charter_ship", ""]
    for (location in locations) {
        player.interfaces.sendVisibility("charter_ship_map", location, location != currentLocation)
    }
}

npcOperate("Talk-To", "trader_stan", "trader_crewmember*") {
    npc<Quiz>("Can I help you?")
    choice {
        option("Yes, who are you?") {
            player<Quiz>("Yes, who are you?")
            npc<Happy>("${if (target.id == "trader_stan") "Why, I'm Trader Stan, owner and operator" else "I'm one of Trader Stan's crew; we are all part"} of the largest fleet of trading ships and chartered vessels to ever sail the seas!")
            if (target.id == "trader_stan") {
                npc<Talk>("If you want to get to a port in a hurry then you can charter one of my ships to take you there - if the price is right...")
            }
            player<Quiz>("So, where exactly can I go with your ships?")
            npc<Talk>("We run ships from Port Phasmatys over to Port Tyras, stopping at Port Sarim, Catherby, Brimhaven, Musa Point, the Shipyard and Port Khazard.")
            npc<Shifty>("We might dock at Mos Le'Harmless once in a while, as well, if you catch my meaning...")
            player<Talk>("Wow, that's a lot of ports. I take it you have some exotic stuff to trade?")
            npc<Happy>("We certainly do! ${if (target.id == "trader_stan") "I and my crewmen" else "We"} have access to items bought and sold from around the world. Would you like to take a look? Or would you like to charter a ship?")
            choice {
                trading()
                charter()
                if (target.id != "trader_stan") {
                    option("Isn't it tricky to sail about in those clothes?") {
                        player<Quiz>("Isn't it tricky to sail about in those clothes?")
                        npc<Surprised>("Tricky? Tricky!")
                        npc<Talk>("Do you have even the slightest idea how tricky it is to sail in this stuff?")
                        npc<Shifty>("Some of us tried tearing it and arguing that it was too fragile to wear when on a boat, but he just had it enchanted to re-stitch itself.")
                        player<Talk>("Wow, that's kind of harsh.")
                        npc<Upset>("Anyway, would you like to take a look at our exotic wares from around the world? Or would you like to charter a ship?")
                        choice {
                            trading()
                            charter()
                            option<Upset>("No thanks.")
                        }
                    }
                }
                option<Upset>("No thanks.")
            }
        }
        option("Yes, I would like to charter a ship.") {
            player<Talk>("Yes, I would like to charter a ship.")
            npc<Talk>("Certainly sir. Where would you like to go?")
        }
        option<Upset>("No thanks.")
    }
}

npcOperate("Charter", "trader_stan", "trader_crewmember*") {
    player.open("charter_ship_map")
}

fun ChoiceBuilder<NPCOption<Player>>.trading() {
    option<Talk>("Yes, let's see what you're trading.") {
        player.openShop("trader_stans_trading_post")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.charter() {
    option<Talk>("Yes, I would like to charter a ship.") {
        npc<Talk>("Certainly sir. Where would you like to go?")
        player.open("charter_ship_map")
    }
}