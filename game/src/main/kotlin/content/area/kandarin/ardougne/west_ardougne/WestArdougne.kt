package content.area.kandarin.ardougne.west_ardougne

import content.entity.obj.door.enterDoor
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Tile

class WestArdougne : Script {

    val floorItems: FloorItems by inject()

    val doorStages = setOf("returned_book", "spoken_to_ted", "spoken_to_milli", "need_clearance")

    init {
        objectOperate("Search", "plague_key_barrel") {
            if (quest("plague_city") == "freed_elena" || questCompleted("plague_city") || ownsItem("a_small_key")) {
                message("The barrel is empty.")
                return@objectOperate
            }
            if (!inventory.add("a_small_key")) {
                floorItems.add(tile, "a_small_key", disappearTicks = 300, owner = this)
            }
            item("a_small_key", 300, "You find a small key in the barrel.")
        }

        objectOperate("Open", "door_elena_prison_closed") { (target) ->
            if (tile.x == 2540 || inventory.contains("a_small_key")) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            statement("The door is locked.")
            npc<Surprised>("elenap_vis", "Hey get me out of here please!")
            player<Sad>("I would do but I don't have a key.")
            if (get("plaguecity_key_asked", false)) {
                set("plaguecity_key_asked", true)
            }
            npc<Uncertain>("elenap_vis", "I think there may be one around somewhere. I'm sure I heard them stashing it somewhere.")
            choice {
                option<Quiz>("Have you caught the plague?") {
                    npc<Neutral>("elenap_vis", "No, I have none of the symptoms.")
                    player<Uncertain>("Strange, I was told this house was plague infected.")
                    npc<Neutral>("elenap_vis", "I suppose that was a cover up by the kidnappers.")
                }
                option<Neutral>("Okay, I'll look for it.")
            }
        }

        objectOperate("Open", "plague_manhole_closed") { (target) ->
            target.replace("plague_manhole_open")
            target.replace("plague_manhole_cover", tile = Tile(2529, 3302))
        }

        objectOperate("Close", "plague_manhole_cover") { (target) ->
            target.replace("plague_manhole_closed", tile = Tile(2529, 3303))
            target.remove()
        }

        objectOperate("Climb-down", "plague_manhole_open") {
            anim("human_pickupfloor")
            statement("You climb down through the manhole.", clickToContinue = false)
            open("fade_out")
            delay(3)
            tele(2514, 9739)
            open("fade_in")
            statement("You climb down through the manhole.", clickToContinue = true)
        }

        objectOperate("Open", "door_rehnison_closed") { (target) ->
            if (tile.y == 3329 || quest("plague_city") != "spoken_to_jethick") {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            npc<Angry>("ted_rehnison", "Go away. We don't want any.")
            if (holdsItem("book_turnip_growing_for_beginners")) {
                player<Neutral>("I'm a friend of Jethick's, I have come to return a book he borrowed.")
                npc<Neutral>("ted_rehnison", "Oh... Why didn't you say, come in then.")
                enterDoor(target, delay = 2)
                inventory.remove("book_turnip_growing_for_beginners")
                set("plague_city", "returned_book")
                item("book_turnip_growing_for_beginners", 600, "You hand the book to Ted as you enter.")
                npc<Happy>("ted_rehnison", "Thanks, I've been missing that.")
            }
        }

        objectOperate("Open", "door_civic_office_closed") { (target) ->
            if (tile.x == 2533 || !doorStages.contains(quest("plague_city"))) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            if (quest("plague_city") == "grill_open") {
                npc<Angry>("bravek", "Go away, I'm busy! I'm... Umm... In a meeting!")
                return@objectOperate
            }
        }

        objectOperate("Open", "door_plague_city_closed") { (target) ->
            if (tile.y == 3272) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            statement("The door won't open. <br> You notice a black cross on the door.")
            npc<Neutral>("mourner_elena_guard_vis", " I'd stand away from there. That black cross means that house has been touched by the plague.")
            if (quest("plague_city") == "spoken_to_milli") {
                choice {
                    option<Surprised>("But I think a kidnap victim is in here.") {
                        npc<Neutral>("mourner_elena_guard_vis", "Sounds unlikely, even kidnappers wouldn't go in there. Even if someone is in there, they're probably dead by now.")
                        choice {
                            option<Neutral>("Good point.")
                            option<Neutral>("I want to check anyway.") {
                                npc<Neutral>("mourner_elena_guard_vis", "You don't have clearance to go in there.")
                                player<Quiz>("How do I get clearance?")
                                npc<Neutral>("mourner_elena_guard_vis", "Well you'd need to apply to the head mourner, or I suppose Bravek the city warder.")
                                set("plague_city", "need_clearance")
                                npc<Neutral>("mourner_elena_guard_vis", "I wouldn't get your hopes up though.")
                            }
                        }
                    }
                    option<Angry>("I haven't got the plague though...") {
                        npc<Neutral>("mourner_elena_guard_vis", "Can't risk you being a carrier. That protective clothing you have isn't regulation issue. It won't meet safety standards.")
                    }
                    option<Neutral>("I'm looking for a woman named Elena.") {
                        npc<Neutral>("mourner_elena_guard_vis", "Ah yes, I've heard of her. A healer I believe. She must be mad coming over here voluntarily.")
                        npc<Neutral>("mourner_elena_guard_vis", "I hear rumours she has probably caught the plague now. Very tragic, a stupid waste of life.")
                    }
                }
            }
        }

        objectOperate("Open", "ardougne_wall_door_2_closed,ardougne_wall_door_closed") {
            message("You pull on the large wooden doors...")
            softQueue("ardougne_wall_door", 2) {
                message("...But they will not open.")
            }
//            cancel() FIXME
        }
    }
}
