package content.area.misthalin.draynor_village

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

@Script
class Leela {

    val escapeKit = listOf(
        Item("bronze_key_prince_ali_rescue"),
        Item("pink_skirt"),
        Item("wig_blonde"),
        Item("paste"),
    )

    init {
        npcOperate("Talk-to", "leela") {
            when (player.quest("prince_ali_rescue")) {
                "leela" -> {
                    if (player["prince_ali_rescue_key_made", false] && !player["prince_ali_rescue_key_given", false]) {
                        npc<Talk>("My father sent this key for you. Be careful not to lose it.")
                        if (player.inventory.add("bronze_key_prince_ali_rescue")) {
                            statement("Leela gives you a copy of the key to the prince's door.")
                            player["prince_ali_rescue_key_given"] = true
                        } else {
                            statement("Leela tries to give you a key, but you don't have enough room for it.")
                            return@npcOperate
                        }
                    } else if (player["prince_ali_rescue_key_given", false] && !player.ownsItem("bronze_key_prince_ali_rescue")) {
                        npc<Quiz>("You're back. How are things going?")
                        if (lostKey()) {
                            return@npcOperate
                        }
                    }
                    if (player.inventory.contains(escapeKit)) {
                        npc<Shifty>("Okay now, you have all the basic equipment. What are your plans to stop the guard interfering?")
                        player["prince_ali_rescue"] = "guard"
                        guard(unsure = true)
                        return@npcOperate
                    }
                    intro()
                }
                "guard", "joe_beer" -> {
                    npc<Quiz>("You're back. How are things going with that guard?")
                    if (lostKey()) {
                        return@npcOperate
                    }
                    guard(unsure = false)
                }
                "joe_beers" -> {
                    npc<Quiz>("You're back. How are things going with that guard?")
                    if (lostKey()) {
                        return@npcOperate
                    }
                    player<Talk>("He's been dealt with.")
                    npc<Happy>("Great! I think that means we're ready. Go in and use some rope to tie Keli up. Once she's dealt with, use the key to free the Prince. Don't forget to give him his disguise so the guards outside don't spot him.")
                }
                "prince_ali_disguise", "completed" -> npc<Happy>("Thank you, Al-Kharid will forever owe you for your help. I think that if there is ever anything that needs to be done, you will be someone they can rely on.")
                else -> {
                    player<Happy>("What are you waiting here for?")
                    npc<Talk>("That is no concern of yours, adventurer.")
                }
            }
        }
    }

    suspend fun NPCOption<Player>.intro() {
        player<Happy>("I am here to help you free the prince.")
        npc<Talk>("Your employment is known to me. Now, do you know all that we need to make the break?")
        player["prince_ali_rescue_leela"] = true
        choice {
            disguise()
            key()
            guards()
            equipment()
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.key() {
        option<Talk>("I need to get the key made.") {
            npc<Talk>("Yes, that is most important. There is no way you can get the real key. It is on a chain around Keli's neck. Almost impossible to steal.")
            npc<Talk>("Get some soft clay and get her to show you the key somehow. Then take the print, with bronze, to my father.")
            choice {
                disguise()
                guards()
                equipment()
            }
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.guards() {
        option<Talk>("What can I do with the guards?") {
            npc<Talk>("Most of the guards will be easy. The disguise will get past them. The only guard who will be a problem will be the one at the door.")
            npc<Talk>("We can discuss this more when you have the rest of the escape kit.")
            choice {
                disguise()
                key()
                equipment()
            }
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.equipment() {
        option<Talk>("I will go and get the rest of the escape equipment.") {
            npc<Shifty>("Good, I shall await your return with everything.")
        }
    }

    fun ChoiceBuilder<NPCOption<Player>>.disguise() {
        option<Quiz>("I must make a disguise. What do you suggest?") {
            npc<Talk>("Only the lady Keli can wander about outside the jail. The guards will shoot to kill if they see the prince out, so we need a disguise good enough to fool them at a distance.")
            if (player.inventory.contains("wig_blonde")) {
                npc<Talk>("The wig you have got, well done.")
            } else {
                npc<Talk>("You need a wig, maybe made from wool. If you find someone who can work with wool ask them about it. There's a witch nearby who may be able to help you dye it.")
            }
            if (player.inventory.contains("pink_skirt")) {
                npc<Talk>("You have got the skirt, good.")
            } else {
                npc<Talk>("You will need to get a pink skirt, same as Keli's.")
            }
            if (player.inventory.contains("skin_paste")) {
                npc<Talk>("You have the skin paint, well done. I thought you would struggle to make that.")
            } else {
                npc<Talk>("We still need something to colour the Prince's skin lighter. There's a witch close to here. She knows about many things. She may know some way to make the skin lighter.")
            }
            if (player.inventory.contains("rope")) {
                npc<Shifty>("You have rope I see, to tie up Keli. That will be the most dangerous part of the plan.")
            } else {
                npc<Shifty>("You will still need some rope to tie up Keli, of course. I heard that there's a good rope maker around here.")
            }
            choice {
                key()
                guards()
                equipment()
            }
        }
    }

    suspend fun NPCOption<Player>.guard(unsure: Boolean) {
        choice {
            option<Talk>("I haven't spoken to him yet.") {
                npc<Talk>("Well, speaking to him may find a weakness he has. See if there's something that could stop him bothering us.")
                npc<Talk>("Good luck with the guard. When the guard is out you can tie up Keli.")
            }
            option("I was going to attack him.") {
                npc<Talk>("I don't think you should. If you do the rest of the gang and Keli would attack you. The door guard should be removed first, to make it easy.")
                npc<Talk>("Good luck with the guard. When the guard is out you can tie up Keli.")
            }
            option<Talk>("I hoped to get him drunk.") {
                npc<Talk>("Well, that's possible. These guards do like a drink. I would think it will take at least 3 beers to do it well. You would probably have to do it all at the same time too. The effects of the local beer wear off quickly.")
                npc<Talk>("Good luck with the guard. When the guard is out you can tie up Keli.")
            }
            option<Quiz>("Maybe I could bribe him to leave.") {
                npc<Talk>("You could try. I don't think the emir will pay anything towards it. And we did bribe one of their guards once.")
                npc<Talk>("Keli killed him in front of the other guards, as a deterrent. It would probably take a lot of gold.")
                npc<Talk>("Good luck with the guard. When the guard is out you can tie up Keli.")
            }
            if (unsure) {
                option<Uncertain>("I'm not sure yet.") {
                    npc<Talk>("You should try talking to him. He might give away some sort of weakness.")
                }
            }
        }
    }

    suspend fun NPCOption<Player>.lostKey(): Boolean {
        if (player.ownsItem("bronze_key_prince_ali_rescue")) {
            return false
        }
        player<Upset>("I'm afraid I lost that key you gave me.")
        npc<Uncertain>("Well that was foolish. I can sort you out with another, but it will cost you 15 coins.")
        if (player.inventory.contains("coins", 15)) {
            player<Talk>("Here, I have 15 coins.")
        } else {
            player<Sad>("I haven't got 15 coins with me.")
            npc<Talk>("Then come back to me when you do.")
            return true
        }
        player.inventory.transaction {
            remove("coins", 15)
            add("bronze_key_prince_ali_rescue")
        }
        when (player.inventory.transaction.error) {
            TransactionError.None -> item("bronze_key_prince_ali_rescue", 400, "Leela gives you a key.")
            else -> {
                statement("Leela tries to give you a key, but you don't have enough room for it.")
                return true
            }
        }
        return false
    }
}
