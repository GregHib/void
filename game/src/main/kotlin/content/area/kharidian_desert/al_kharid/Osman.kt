package content.area.kharidian_desert.al_kharid

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class Osman : Script {

    init {
        npcOperate("Talk-to", "osman_*") {
            when (quest("prince_ali_rescue")) {
                "completed" -> {
                    if (!questCompleted("contact")) {
                        npc<Talk>("Well done. A great rescue. I will remember you if I have anything dangerous to do.")
                        return@npcOperate
                    }
                    choice {
                        option("I'd like to talk about sq'irks.") {
                            sqirks()
                        }
                    }
                }
                "unstarted" -> {
                    npc<Shifty>("Hello. I am Osman. What can I assist you with?")
                    choice {
                        option<Talk>("You don't seem very tough. Who are you?") {
                            npc<Shifty>("I work for Al Kharid's Emir. That is all you need to know.")
                        }
                        option<Shifty>("Nothing. I'm just being nosy.") {
                            npc<Shifty>("That bothers me not. The secrets of Al Kharid protect themselves.")
                        }
                    }
                    return@npcOperate
                }
                "osman" -> {
                    player<Talk>("The chancellor trusts me. I have come for instructions.")
                    npc<Shifty>("Our prince is captive by the Lady Keli. We just need to make the rescue. There are two things we need you to do.")
                    set("prince_ali_rescue", "leela")
                    choice {
                        firstThing()
                        secondThing()
                        findThings()
                    }
                }
                "leela" -> {
                    if (inventory.contains("key_print")) {
                        if (!inventory.contains("bronze_bar")) {
                            player<Talk>("I have an imprint of the key.")
                            npc<Shifty>("Good. Bring me a bronze bar, and I'll get a copy made.")
                        } else {
                            npc<Shifty>("Well done; we can make the key now.")
                            inventory.remove("key_print", "bronze_bar")
                            set("prince_ali_rescue_key_made", true)
                            statement("Osman takes the key imprint and the bronze bar.")
                            npc<Shifty>("Pick the key up from Leela.")
                        }
                    } else if (get("prince_ali_rescue_key_given", false) && !ownsItem("bronze_key_prince_ali_rescue")) {
                        player<Talk>("I'm afraid I lost that key you gave me.")
                        npc<Uncertain>("Well that was foolish. I can sort you out with another, but it will cost you 15 coins.")
                        if (inventory.contains("coins", 15)) {
                            player<Talk>("Here, I have 15 coins.")
                        } else {
                            player<Sad>("I haven't got 15 coins with me.")
                            npc<Talk>("Then come back to me when you do.")
                            return@npcOperate
                        }
                        inventory.transaction {
                            remove("coins", 15)
                            add("bronze_key_prince_ali_rescue")
                        }
                        when (inventory.transaction.error) {
                            TransactionError.None -> item("bronze_key_prince_ali_rescue", 400, "Osman gives you a key.")
                            else -> statement("Osman tries to give you a key, but you don't have enough room for it.")
                        }
                    }
                    choice {
                        option<Talk>("Thank you. I will try to find the other items.")
                        option<Talk>("Can you tell me what I still need to get?") {
                            remainingItems()
                        }
                    }
                }
                "prince_ali_disguise" -> npc<Shifty>("The prince is safe and on his way home with Leela. You can pick up your payment from the chancellor.")
                else -> {
                    player<Quiz>("Can you tell me what I still need to get?")
                    remainingItems()
                }
            }
        }
    }

    private suspend fun Player.sqirks() {
        if (inventory.contains("winter_sqirkjuice") || inventory.contains("spring_sqirkjuice") || inventory.contains("autumn_sqirkjuice") || inventory.contains("summer_sqirkjuice")) {
            player<Talk>("I have some sq'irk juice for you.")
            var experience = 0.0
            inventory.transaction {
                experience += removeToLimit("winter_sqirkjuice", 28) * 350.0
                experience += removeToLimit("spring_sqirkjuice", 28) * 1350.0
                experience += removeToLimit("autumn_sqirkjuice", 28) * 2350.0
                experience += removeToLimit("summer_sqirkjuice", 28) * 3000.0
            }
            exp(Skill.Thieving, experience)
            statement("Osman imparts some Thieving advice to you (${experience.toDigitGroupString()} Thieving experience points) as a reward for the sq'irk juice.")
            npc<Happy>("Thank you very much $name. If you get some more sq'irks be sure to come back.")
            player<Happy>("I will. It's been a pleasure doing business with you.")
        }
        set("spoken_to_osman", true)
        choice {
            whereSqirks()
            howSqirks()
            whySqirks()
            whatSqirks()
            option<Talk>("I should go.")
        }
    }

    private fun ChoiceOption.whatSqirks() {
        option<Quiz>("What's so good about sq'irk juice?") {
            npc<Talk>("It is a sweet nectar for a thief or spy. It makes light fingers lighter, fleet feet flightier and comes in four different colours for those who are easily amused.")
            statement("Osman starts salivating at the thought of sq'irk juice.")
            player<Quiz>("It wouldn't have addictive properties, would it?")
            npc<Talk>("It only holds power over those with poor self-control. Something which I have an abundance of.")
            player<Talk>("I see.")
            choice {
                whereSqirks()
                howSqirks()
                whySqirks()
                option<Talk>("I should go.")
            }
        }
    }

    private fun ChoiceOption.whySqirks() {
        option<Quiz>("Is there a reward for getting these sq'irks?") {
            npc<Talk>("Of course. I'll train you in the art of Thieving for your troubles.")
            player<Quiz>("How much training will you give?")
            npc<Talk>("That depends on the quantity and ripeness of the sq'irks you put into the juice.")
            player<Talk>("That sounds fair enough.")
            choice {
                whereSqirks()
                howSqirks()
                whatSqirks()
                option<Talk>("I should go.")
            }
        }
    }

    private fun ChoiceOption.howSqirks() {
        option<Quiz>("How should I squeeze the fruit?") {
            npc<Talk>("Use a pestle and mortar. Make sure you have an empty glass with you to collect the juice.")
            choice {
                whereSqirks()
                whySqirks()
                whatSqirks()
                option<Talk>("I should go.")
            }
        }
    }

    private fun ChoiceOption.whereSqirks() {
        option<Quiz>("Where do I get sq'irks?") {
            npc<Talk>("There is a sorceress near the south eastern edge of Al Kharid who grows them. She used to be friends with Osman, but they fell out.")
            player<Quiz>("What happened?")
            npc<Talk>("I don't know. All I know is she won't give us any more fruit.")
            player<Quiz>("So all I have to do is ask her for some fruit for you?")
            npc<Talk>("I doubt it will be that easy. She is not renowned for her generosity and is very secretive about her garden's location.")
            player<Talk>("Oh come on, it should be easy enough to find.")
            npc<Talk>("Her garden has remained hidden even from Osman. I believe it must be hidden by magical means.")
            player<Quiz>("I see. And why can't you do this yourself?")
            npc<Talk>("My work requires that I remain here.")
            player<Talk>("Okay. This should be an interesting task.")
            choice {
                howSqirks()
                whySqirks()
                whatSqirks()
                option<Talk>("I should go.")
            }
        }
    }

    fun ChoiceOption.findThings() {
        option<Talk>("Okay, I better go find some things.") {
            npc<Shifty>("May good luck travel with you. Don't forget to find Leela. It can't be done without her help.")
        }
    }

    fun ChoiceOption.firstThing() {
        option<Talk>("What is the first thing I must do?") {
            npc<Shifty>("The prince is guarded by some stupid guards and a clever woman. The woman is our only way to get the prince out. Only she can walk freely about the area.")
            npc<Shifty>("I think you will need to tie her up. One coil of rope should do for that. Then, disguise the prince as her to get him out without suspicion.")
            player<Quiz>("How good must the disguise be?")
            npc<Shifty>("Only enough to fool the guards at a distance. Get a skirt like hers. Same colour, same style. We will only have a short time.")
            npc<Shifty>("Get a blonde wig, too. That is up to you to make or find. Something to colour the skin of the prince.")
            npc<Shifty>("My daughter and top spy, Leela, can help you. She has sent word that she has discovered where they are keeping the prince.")
            npc<Shifty>("It's near Draynor Village. She is lurking somewhere near there now.")
            choice {
                firstThing()
                secondThing()
                findThings()
            }
        }
    }

    fun ChoiceOption.secondThing(text: String = "What is the second thing you need?") {
        option<Quiz>(text) {
            npc<Shifty>("We need the key, or we need a copy made. If you can get some soft clay then you can copy the key...")
            npc<Shifty>("...If you can convince Lady Keli to show it to you for a moment. She is very boastful. I should not be too hard.")
            npc<Shifty>("Bring the imprint to me, with a bar of bronze.")
            choice {
                firstThing()
                secondThing("What exactly is the second thing you need?")
                findThings()
            }
        }
    }

    suspend fun Player.remainingItems() {
        if (inventory.contains("bronze_key_prince_ali_rescue")) {
            npc<Shifty>("The key you already have. Good.")
        } else if (get("prince_ali_rescue_key_made", false) && !get("prince_ali_rescue_key_given", false)) {
            npc<Shifty>("You can collect the key from Leela.")
        } else {
            npc<Shifty>("A print of the key in soft clay and a bronze bar. Then, collect the key from Leela.")
        }
        if (inventory.contains("wig_blonde")) {
            npc<Shifty>("The wig you have got; well done.")
        } else {
            npc<Shifty>("You need to make a blonde wig somehow. Leela may help.")
        }
        if (inventory.contains("pink_skirt")) {
            npc<Shifty>("You have the skirt. Good.")
        } else {
            npc<Shifty>("You will need a skirt that looks the same as Keli's.")
        }
        if (inventory.contains("skin_paint")) {
            npc<Shifty>("You have the skin paint; well done. I thought you would struggle to make that.")
        } else {
            npc<Shifty>("Something to make the prince's skin appear lighter.")
        }
        if (inventory.contains("rope")) {
            npc<Shifty>("Yes, you have the rope.")
        } else {
            npc<Shifty>("A rope with which to tie Keli up.")
        }
        npc<Shifty>("Once you have everything, go to Leela. She must be ready to get the prince away.")
    }
}
