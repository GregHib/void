package content.entity.obj.ship

import content.entity.npc.shop.openShop
import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.jingle
import content.quest.questCompleted
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.event.Script
@Script
class CharterShip {

    val locations = listOf(
        "catherby",
        "brimhaven",
        "port_khazard",
        "port_sarim",
    )
    
    val ships: CharterShips by inject()
    val teles: ObjectTeleports by inject()
    
    init {
        interfaceRefresh("charter_ship_map") { player ->
            val currentLocation = player["charter_ship", ""]
            val prices = ships.get(currentLocation)
            player.interfaces.sendVisibility(id, "mos_le_harmless", hasQuestRequirements(player, "mos_le_harmless") && prices.containsKey("mos_le_harmless"))
            player.interfaces.sendVisibility(id, "shipyard", hasQuestRequirements(player, "shipyard") && prices.containsKey("shipyard"))
            player.interfaces.sendVisibility(id, "port_tyras", hasQuestRequirements(player, "port_tyras") && prices.containsKey("port_tyras"))
            player.interfaces.sendVisibility(id, "port_phasmatys", hasQuestRequirements(player, "port_phasmatys") && prices.containsKey("port_phasmatys"))
            player.interfaces.sendVisibility(id, "oo_glog", hasQuestRequirements(player, "oo_glog") && prices.containsKey("oo_glog"))
            player.interfaces.sendVisibility(id, "crandor", false)
            player.interfaces.sendVisibility(id, "musa_point", false)
        
            for (location in locations) {
                player.interfaces.sendVisibility(id, location, location != currentLocation && prices.containsKey(location))
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
            player["charter_ship"] = location(target)
            player.open("charter_ship_map")
        }

        interfaceOption("Ok", "*", "charter_ship_map") {
            val currentLocation = player["charter_ship", ""]
            if (component == currentLocation) {
                return@interfaceOption
            }
            val price = ships.get(currentLocation, component) ?: return@interfaceOption
            if (!hasQuestRequirements(player, component)) {
                return@interfaceOption
            }
            val readablePrice = price.toDigitGroupString()
            player.strongQueue("charter_ship") {
                if (!player.inventory.contains("coins", price)) {
                    choice("Sailing to ${component.toTitleCase()} costs $readablePrice coins.") {
                        option("Choose again") {
                            player.open("charter_ship_map")
                        }
                        option("No")
                    }
                    return@strongQueue
                }
                statement("To sail to ${component.toTitleCase()} from here will cost you $readablePrice gold. Are you sure you want to pay that?")
                choice {
                    option("Ok") {
                        if (player.inventory.remove("coins", price)) {
                            player.jingle("sailing_theme_short")
                            player.open("fade_out")
                            delay(4)
                            val teleport = teles.get("${component}_gangplank_enter", "Cross").first()
                            player.tele(teleport.to)
                            player.open("fade_in")
                            delay(3)
                            player.message("You pay the fare and sail to ${component.toTitleCase()}.", ChatType.Filter)
                        }
                    }
                    option("Choose again") {
                        player.open("charter_ship_map")
                    }
                    option("No")
                }
            }
        }

    }

    fun ChoiceBuilder<NPCOption<Player>>.trading() {
        option<Talk>("Yes, let's see what you're trading.") {
            player.openShop("trader_stans_trading_post")
        }
    }
    
    fun ChoiceBuilder<NPCOption<Player>>.charter() {
        option<Talk>("Yes, I would like to charter a ship.") {
            npc<Talk>("Certainly sir. Where would you like to go?")
            player["charter_ship"] = location(target)
            player.open("charter_ship_map")
        }
    }
    
    fun hasQuestRequirements(player: Player, location: String): Boolean {
        return player.questCompleted(
            when (location) {
                "mos_le_harmless" -> "mos_le_harmless"
                "shipyard" -> "the_grand_tree"
                "port_tyras" -> "regicide"
                "port_phasmatys" -> "priest_in_peril"
                "oo_glog" -> "as_a_first_resort"
                else -> return true
            },
        )
    }
    
    fun location(npc: NPC) = when (npc["spawn_tile", Tile.EMPTY]) {
        Tile(3033, 3192), Tile(3039, 3193), Tile(3042, 3192) -> "port_sarim"
        Tile(2759, 3239), Tile(2760, 3239) -> "brimhaven"
        Tile(2144, 3122), Tile(2145, 3122) -> "tyras_camp"
        Tile(3001, 3033), Tile(3001, 3034) -> "shipyard"
        Tile(2673, 3144), Tile(2675, 3144) -> "port_khazard"
        Tile(3671, 2930), Tile(3672, 2930) -> "mos_le_harmless"
        Tile(3701, 3502), Tile(3701, 3503) -> "port_phasmatys"
        Tile(2794, 3414), Tile(2795, 3414) -> "catherby"
        Tile(2619, 2856), Tile(2621, 2857) -> "oo_glog"
        Tile(2954, 3157), Tile(2954, 3156) -> "musa_point"
        else -> ""
    }
}
