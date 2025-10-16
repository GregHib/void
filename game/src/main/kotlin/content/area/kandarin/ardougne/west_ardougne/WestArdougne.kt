package content.area.kandarin.ardougne.west_ardougne

import content.entity.obj.door.enterDoor
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Tile

@Script
class WestArdougne {

    val floorItems: FloorItems by inject()

    val visStages = setOf("freed_elena", "completed", "completed_with_spell")

    val rehnisonStages = setOf("returned_book", "spoken_to_ted", "spoken_to_milli", "need_clearance", "talk_to_bravek", "has_cure_paper", "gave_cure", "freed_elena", "completed", "completed_with_spell")

    val doorStages = setOf("talk_to_bravek", "has_cure_paper", "gave_cure", "freed_elena", "completed", "completed_with_spell")

    init {
        objectOperate("Search", "plaguekeybarrel") {
            if (player.quest("plague_city") != "freed_elena" || player.ownsItem("a_small_key")) {
                player.message("The barrel is empty.")
                return@objectOperate
            }
            if (!player.inventory.add("a_small_key")) {
                floorItems.add(player.tile, "a_small_key", disappearTicks = 300, owner = player)
            }
            item("a_small_key", 300, "You find a small key in the barrel.")
        }

        objectOperate("Open", "door_55_closed") {
            if (player.tile.x == 2540 || player.inventory.contains("a_small_key") || visStages.contains(player.quest("plague_city"))) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            statement("The door is locked.")
            npc<Surprised>("elenap_vis", "Hey get me out of here please!")
            player<Sad>("I would do but I don't have a key.")
            if (player["plaguecity_key_asked", false]) {
                player["plaguecity_key_asked"] = true
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

        objectOperate("Open", "plaguemanholeclosed") {
            target.replace("plaguemanholeopen")
            target.replace("plaguemanholecover", tile = Tile(2529, 3302))
        }

        objectOperate("Close", "plaguemanholecover") {
            target.replace("plaguemanholeclosed", tile = Tile(2529, 3303))
            target.remove()
        }

        objectOperate("Climb-down", "plaguemanholeopen") {
            player.anim("human_pickupfloor")
            statement("You climb down through the manhole.", clickToContinue = false)
            player.open("fade_out")
            delay(3)
            player.tele(2514, 9739)
            player.open("fade_in")
            statement("You climb down through the manhole.", clickToContinue = true)
        }

        objectOperate("Open", "door_rehnison_closed") {
            if (player.tile.y == 3329 || rehnisonStages.contains(player.quest("plague_city"))) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            npc<Angry>("ted_rehnison", "Go away. We don't want any.")
            if (player.holdsItem("book_turnip_growing_for_beginners")) {
                npc<Angry>("ted_rehnison", "Go away. We don't want any.")
                player<Neutral>("I'm a friend of Jethick's, I have come to return a book he borrowed.")
                npc<Neutral>("ted_rehnison", "Oh... Why didn't you say, come in then.")
                enterDoor(target, delay = 2)
                player.inventory.remove("book_turnip_growing_for_beginners")
                player["plague_city"] = "returned_book"
                item("book_turnip_growing_for_beginners", 600, "You hand the book to Ted as you enter.")
                npc<Happy>("ted_rehnison", "Thanks, I've been missing that.")
            }
        }

        objectOperate("Open", "door_57_closed") {
            if (player.tile.x == 2533 || doorStages.contains(player.quest("plague_city"))) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            if (player.quest("plague_city") == "grill_open") {
                npc<Angry>("bravek", "Go away, I'm busy! I'm... Umm... In a meeting!")
                return@objectOperate
            }
        }

        objectOperate("Open", "door_plague_city_closed") {
            if (player.tile.y == 3272) {
                enterDoor(target, delay = 2)
                return@objectOperate
            }
            statement("The door won't open. <br> You notice a black cross on the door.")
            npc<Neutral>("mourner_elena_guard_vis", " I'd stand away from there. That black cross means that house has been touched by the plague.")
            if (player.quest("plague_city") == "spoken_to_milli") {
                choice {
                    option<Surprised>("But I think a kidnap victim is in here.") {
                        npc<Neutral>("mourner_elena_guard_vis", "Sounds unlikely, even kidnappers wouldn't go in there. Even if someone is in there, they're probably dead by now.")
                        choice {
                            option<Neutral>("Good point.")
                            option<Neutral>("I want to check anyway.") {
                                npc<Neutral>("mourner_elena_guard_vis", "You don't have clearance to go in there.")
                                player<Quiz>("How do I get clearance?")
                                npc<Neutral>("mourner_elena_guard_vis", "Well you'd need to apply to the head mourner, or I suppose Bravek the city warder.")
                                player["plague_city"] = "need_clearance"
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

        objectOperate("Open", "ardougne_wall_door_2_closed", "ardougne_wall_door_closed") {
            player.message("You pull on the large wooden doors...")
            player.softQueue("ardougne_wall_door", 2) {
                player.message("...But they will not open.")
            }
            cancel()
        }
    }
}
