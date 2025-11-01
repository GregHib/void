package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.SuspendableContext

class Jethick : Script {

    init {
        npcOperate("Talk-to", "jethick") {
            when (quest("plague_city")) {
                "grill_open" -> grillOpen()
                "spoken_to_jethick" -> {
                    npc<Neutral>("Hello. We don't get many newcomers around here.")
                    looking()
                }
                else -> npc<Neutral>("Hello. We don't get many newcomers around here.")
            }
        }

        itemOnNPCOperate("*", "jethick") {
            if (item.id == "picture_plague_city") {
                player<Talk>("Hi, I'm looking for a woman from East Ardougne.")
                player.showPicture()
            } else {
                npc<Quiz>("Thanks, but I don't accept gifts.")
            }
        }
    }

    suspend fun Player.grillOpen() {
        if (get("plaguecity_picture_asked", false)) {
            set("plague_city", "spoken_to_jethick")
            spokenToJethick()
        } else {
            npc<Neutral>("Hello, I don't recognise you. We don't get many newcomers around here.")
            player<Quiz>("How come?")
            npc<Neutral>("The plague of course. Not many people want to come to a place like this and the few that do normally get stopped by the mourners.")
            npc<Neutral>("All you'll find here now are the dead and the dying. Even our own king has abandoned us.")
            player<Quiz>("Your king?")
            npc<Neutral>("Yes, King Tyras of West Ardougne. He's the brother of King Lathas, the ruler of East Ardougne.")
            player<Quiz>("So where is the king?")
            npc<Angry>("Well he's always been a bit of an explorer. He's led multiple expeditions into the uncharted lands to the west.")
            npc<Angry>("The plague first started when he came back from one of these expeditions. More than a few suspect that some of his men caught it out there and brought it back with them.")
            npc<Angry>("The king didn't care though. He just left on another expedition to the west. He hasn't been seen since. He left the city warder Bravek in charge but he's no better.")
            npc<Quiz>("Anyway, you clearly didn't come here to talk about kings. So tell me, what brings you to West Ardougne?")
            looking()
        }
    }

    private suspend fun Player.looking() {
        player<Happy>("I'm looking for a woman from East Ardougne called Elena.")
        npc<Uncertain>("East Ardougnian women are easier to find in East Ardougne. Not many would come to West Ardougne to find one. Although the name is familiar, what does she look like?")
        if (inventory.contains("picture_plague_city")) {
            showPicture()
        } else {
            player<Uncertain>("Um... brown hair... in her twenties...")
            if (!get("plaguecity_picture_asked", false)) {
                set("plaguecity_picture_asked", true)
            }
            npc<Uncertain>("Hmmm, that doesn't narrow it down a huge amount... I'll need to know more than that, or see a picture?")
        }
    }

    private suspend fun Player.showPicture() {
        item("picture_plague_city", 600, "You show Jethick the picture.")
        npc<Neutral>("Ah yes. She came over here to help the plague victims. I think she is staying over with the Rehnison family.")
        set("plague_city", "spoken_to_jethick")
        spokenToJethick()
    }

    suspend fun Player.spokenToJethick() {
        npc<Neutral>("They live in the small timbered building at the far north side of town. I've not seen her around here in a while, mind.")
        if (!ownsItem("book_turnip_growing_for_beginners")) {
            npc<Neutral>("I don't suppose you could run me a little errand while you're over there? I borrowed this book from them, can you return it?")
            choice {
                option<Happy>("Yes, I'll return it for you.") {
                    if (inventory.add("book_turnip_growing_for_beginners")) {
                        item("book_turnip_growing_for_beginners", 500, "Jethick gives you a book.")
                    } else {
                        item("book_turnip_growing_for_beginners", 500, "Jethick shows you the book, but you don't have room to take it.")
                    }
                }
                option<Happy>("No, I don't have time for that.")
            }
        }
    }
}
