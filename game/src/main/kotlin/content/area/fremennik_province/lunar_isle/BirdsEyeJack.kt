package content.area.fremennik_province.lunar_isle

import content.entity.player.bank.pin.openBank
import content.entity.player.bank.pin.openCollection
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male

/**
 * 'Bird's-Eye' Jack banks on Lunar Isle after Dream Mentor.
 * https://runescape.wiki/w/Transcript:%27Bird%27s-Eye%27_Jack
 */
class BirdsEyeJack : Script {

    init {
        npcApproach("Talk-to", "birds_eye_jack_2_2") {
            approachRange(2)
            choice("What would you like to talk about?") {
                option("Using the bank") {
                    bank()
                }
                if (questCompleted("while_guthix_sleeps")) {
                    option("Cyrisus") {
                        cyrisusDied()
                    }
                } else {
                    option("Cyrisus in the mine") {
                        cyrisusRecovered()
                    }
                }
            }
        }
    }

    private suspend fun Player.bank() {
        choice("What would you like to say?") {
            option("I'd like to access my bank account please.") {
                openBank()
            }
            option("I'd like to check my pin settings.") {
                open("bank_pin_settings")
            }
            option("I'd like to see my collections box.") {
                openCollection()
            }
            option<Quiz>("What is this place?") {
                npc<Neutral>("This is a branch of the Bank of ${Settings["server.name"]}. We have branches in many towns.")
                player<Quiz>("And what do you do?")
                npc<Neutral>("We will look after your items and money for you. Leave your valuables with us if you want to keep them safe.")
            }
        }
    }

    private suspend fun Player.cyrisusRecovered() {
        player<Neutral>("Hi.")
        npc<Quiz>("How's Cyrisus? Where is he?")
        player<Happy>("He's fine now. Back to full health.")
        npc<Neutral>("Good. I'm pleased.")
        player<Neutral>("No problem.")
        npc<Angry>("Don't think that this means I like you now!")
        player<Neutral>("Look at my face.")
        npc<Quiz>("Yes? What about it.")
        player<Neutral>("Does it look like I care?")
        npc<Neutral>("Not really.")
        player<Neutral>("Well, there you go.")
    }

    private suspend fun Player.cyrisusDied() {
        player<Sad>("I'm afraid I have some rather sad news, I still can't believe it myself.")
        npc<Happy>("What? That he's a more respected adventurer than you? Has more renown and better armour? Oh, yes, I bet that does make you sad! Ha, ha!")
        player<Sad>("If only that were the case, I'd wish him very well - but he's gone, he's passed away.")
        npc<Shock>("What?")
        player<Sad>("He bravely fought alongside Hazelmere, Duradel, Mazchna, Sloane, Ghommal and Harrallak against Lucien's evil. I was there, I saw the whole thing.")
        npc<Shock>("Oh my! That's...I can't believe it!")
        player<Sad>("Nor can I...")
        npc<Sad>("Well, it sounds like he went out fighting, which was probably his greatest wish. Don't dwell on it, ${if (male) "boy" else "girl"}, I'm sure he achieved a greater peace by fighting against the evil of this world.")
        player<Sad>("Hmmm, I guess you're right, but I'm still going to miss him.")
    }
}
