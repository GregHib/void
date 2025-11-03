package content.entity.obj.ship

import content.entity.npc.shop.openShop
import content.entity.obj.ObjectTeleports
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.jingle
import content.quest.questCompleted
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile

class CharterShip : Script {

    val locations = listOf(
        "catherby",
        "brimhaven",
        "port_khazard",
        "port_sarim",
    )

    val ships: CharterShips by inject()
    val teles: ObjectTeleports by inject()

    init {
        interfaceRefresh("charter_ship_map") { id ->
            val currentLocation = get("charter_ship", "")
            val prices = ships.get(currentLocation)
            interfaces.sendVisibility(id, "mos_le_harmless", hasQuestRequirements("mos_le_harmless") && prices.containsKey("mos_le_harmless"))
            interfaces.sendVisibility(id, "shipyard", hasQuestRequirements("shipyard") && prices.containsKey("shipyard"))
            interfaces.sendVisibility(id, "port_tyras", hasQuestRequirements("port_tyras") && prices.containsKey("port_tyras"))
            interfaces.sendVisibility(id, "port_phasmatys", hasQuestRequirements("port_phasmatys") && prices.containsKey("port_phasmatys"))
            interfaces.sendVisibility(id, "oo_glog", hasQuestRequirements("oo_glog") && prices.containsKey("oo_glog"))
            interfaces.sendVisibility(id, "crandor", false)
            interfaces.sendVisibility(id, "musa_point", false)
            for (location in locations) {
                interfaces.sendVisibility(id, location, location != currentLocation && prices.containsKey(location))
            }
        }

        npcOperate("Talk-To", "trader_stan,trader_crewmember*") { (target) ->
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
                        charter(target)
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
                                    charter(target)
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

        npcOperate("Charter", "trader_stan,trader_crewmember*") { (target) ->
            set("charter_ship", location(target))
            open("charter_ship_map")
        }

        interfaceOption("Ok", "charter_ship_map:*") {
            val currentLocation = get("charter_ship", "")
            val component = it.component
            if (component == currentLocation) {
                return@interfaceOption
            }
            val price = ships.get(currentLocation, component) ?: return@interfaceOption
            if (!hasQuestRequirements(component)) {
                return@interfaceOption
            }
            val readablePrice = price.toDigitGroupString()
            strongQueue("charter_ship") {
                if (!inventory.contains("coins", price)) {
                    choice("Sailing to ${component.toTitleCase()} costs $readablePrice coins.") {
                        option("Choose again") {
                            open("charter_ship_map")
                        }
                        option("No")
                    }
                    return@strongQueue
                }
                statement("To sail to ${component.toTitleCase()} from here will cost you $readablePrice gold. Are you sure you want to pay that?")
                choice {
                    option("Ok") {
                        if (inventory.remove("coins", price)) {
                            jingle("sailing_theme_short")
                            open("fade_out")
                            delay(4)
                            val teleport = teles.get("${component}_gangplank_enter", "Cross").first()
                            tele(teleport.to)
                            open("fade_in")
                            delay(3)
                            message("You pay the fare and sail to ${component.toTitleCase()}.", ChatType.Filter)
                        }
                    }
                    option("Choose again") {
                        open("charter_ship_map")
                    }
                    option("No")
                }
            }
        }
    }

    fun ChoiceOption.trading() {
        option<Talk>("Yes, let's see what you're trading.") {
            openShop("trader_stans_trading_post")
        }
    }

    fun ChoiceOption.charter(target: NPC) {
        option<Talk>("Yes, I would like to charter a ship.") {
            npc<Talk>("Certainly sir. Where would you like to go?")
            set("charter_ship", location(target))
            open("charter_ship_map")
        }
    }

    fun Player.hasQuestRequirements(location: String): Boolean {
        return questCompleted(
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
