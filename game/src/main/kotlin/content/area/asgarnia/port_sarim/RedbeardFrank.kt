package content.area.asgarnia.port_sarim

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questCompleted
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class RedbeardFrank : Script {

    init {
        npcOperate("Talk-to", "redbeard_frank_port_sarim") {
            npc<Laugh>("Arr, Matey!")
            when (questStage("pirates_treasure")) {
                0 -> unstartedMenu()
                1 -> broughtRum()
                else -> completedMenu()
            }
        }
    }

    private suspend fun Player.unstartedMenu() {
        choice {
            option<Shifty>("I'm in search of treasure.") {
                treasureOffer()
            }
            option<Laugh>("Arr!") {
                npc<Laugh>("Arr!")
            }
            option<Quiz>("Do you have anything for trade?") {
                nothingToTrade()
            }
        }
    }

    private suspend fun Player.completedMenu() {
        if (questStage("pirates_treasure") < 3 && !ownsItem("chest_key_pirates_treasure")) {
            player<Sad>("I seem to have lost my chest key...")
            npc<Neutral>("Arr, silly you. Fortunately I took the precaution to have another one made.")
            addOrDrop("chest_key_pirates_treasure")
            item(item = "chest_key_pirates_treasure", text = "Frank hands you a chest key")
        }
        choice {
            option<Laugh>("Arr!") {
                npc<Laugh>("Arr!")
            }
            option<Quiz>("Do you have anything for trade?") {
                nothingToTrade()
            }
        }
    }

    private suspend fun Player.nothingToTrade() {
        npc<Neutral>("Nothin' at the moment, but then again the Customs Agents are on the warpath right now.")
    }

    private suspend fun Player.treasureOffer() {
        npc<Shifty>("Arr, treasure you be after eh? Well I might be able to tell you where to find some... For a price...")
        player<Quiz>("What sort of price?")
        npc<Neutral>("Well for example if you can get me a bottle of rum... Not just any rum mind...")
        npc<Happy>("I'd like some rum made on Karamja Island. There's no rum like Karamja Rum!")
        if (questCompleted("rum_deal")) { // TODO
            player<Quiz>("Would some Braindeath 'Rum' do?")
            npc<Shock>("Gadzooks, that swill! Not a chance!")
            npc<Neutral>("It's Karamja Rum or no deal.")
        }
        choice {
            option<Neutral>("Ok, I will bring you some rum.") {
                set("pirates_treasure", "started")
                npc<Happy>("Yer a saint, although it'll take a miracle to get it off Karamja.")
                whyItsHard()
            }
            option<Neutral>("Not right now.") {
                npc<Neutral>("Fair enough. I'll still be here and thirsty whenever you feel like helpin' out.")
            }
        }
    }

    private suspend fun Player.whyItsHard() {
        player<Quiz>("What do you mean?")
        npc<Neutral>(
            "The Customs office has been clampin' down on the export of spirits. You seem like a " +
                "resourceful young ${if (male) "lad" else "lass"}, I'm sure ye'll be able to find a way to slip the " +
                "stuff past them.",
        )
        player<Neutral>("Well I'll give it a shot.")
        if (questStage("pirates_treasure") < 1) {
            npc<Happy>("Arr, that's the spirit!")
        }
    }

    private suspend fun Player.broughtRum() {
        npc<Quiz>("Have ye brought some rum for yer ol' mate Frank?")
        val plainRum = inventory.contains("karamjan_rum")
        val slicedRum = inventory.contains("karamjan_rum_sliced_banana")
        val bananaRum = inventory.contains("karamjan_rum_banana")
        if (!plainRum && !slicedRum && !bananaRum) {
            player<Sad>("No, not yet.")
            npc<Happy>("Not surprising, tis no easy task to get it off Karamja.")
            whyItsHard()
            return
        }
        player<Happy>("Yes, I've got some.")
        when {
            slicedRum -> rejectRum("karamjan_rum_sliced_banana", "Arr - I don't likes banana in me rum!")
            bananaRum -> rejectRum("karamjan_rum_banana", "Arr - this here rum's got a banana stuck in it!")
            else -> acceptRum()
        }
    }

    private suspend fun Player.rejectRum(item: String, complaint: String) {
        if (!inventory.remove(item)) {
            return
        }
        message("Frank happily takes the rum.")
        delay(4)
        npc<Neutral>(complaint)
        message("Frank hands you back the rum.")
        addOrDrop(item)
    }

    private suspend fun Player.acceptRum() {
        npc<Shifty>("Now a deal's a deal, I'll tell ye about the treasure. I used to serve under a pirate captain called One-Eyed Hector.")
        npc<Shifty>("Hector were very successful and became very rich. But about a year ago we were boarded by the Customs and Excise Agents.")
        npc<Sad>("Hector were killed along with many of the crew, I were one of the few to escape and I escaped with this.")
        if (!inventory.remove("karamjan_rum")) {
            return
        }
        set("pirates_treasure", "chest_key")
        addOrDrop("chest_key_pirates_treasure")
        item(item = "chest_key_pirates_treasure", text = "Frank happily takes the rum... and hands you a key.")
        npc<Shifty>("This be Hector's key. I believe it opens his chest in his old room in the Blue Moon Inn in Varrock.")
        npc<Neutral>("With any luck his treasure will be in there.")
        choice {
            option<Happy>("Ok thanks, I'll go and get it.")
            option<Quiz>("So why didn't you ever get it?") {
                npc<Sad>("I'm not allowed in the Blue Moon Inn. Apparently I'm a drunken trouble maker.")
            }
        }
    }
}
