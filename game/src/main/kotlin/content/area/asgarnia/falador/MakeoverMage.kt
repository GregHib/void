package content.area.asgarnia.falador

import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class MakeoverMage : Script {

    init {
        npcSpawn("makeover_mage*") {
            softTimers.start("makeover")
        }

        npcOperate("Talk-to", "makeover_mage*") {
            npc<Pleased>("Hello there! I am known as the Makeover Mage! I have spent many years researching magicks that can change your physical appearance.")
            npc<Pleased>("I call it a 'makeover'. Would you like me to perform my magicks on you?")
            choice {
                more()
                start()
                exit()
                amulet()
                colour()
            }
        }

        npcOperate("Makeover", "makeover_mage*") {
            openDressingRoom("skin_colour")
        }

        interfaceClosed("skin_colour") {
            softTimers.stop("dressing_room")
        }

        interfaceOpened("skin_colour") { id ->
            set("makeover_female", !male)
            set("makeover_colour_skin", body.getColour(BodyColour.Skin))
            interfaces.sendText(id, "confirm", "CONFIRM")
        }

        interfaceOption("Select Female", "skin_colour:female") {
            set("makeover_female", true)
            sendVariable("makeover_colour_skin")
        }

        interfaceOption("Select Male", "skin_colour:male") {
            set("makeover_female", false)
            sendVariable("makeover_colour_skin")
        }

        interfaceOption(id = "skin_colour:colour_*") {
            set("makeover_colour_skin", EnumDefinitions.get("character_skin").getInt(it.component.removePrefix("colour_").toInt()))
        }

        interfaceOption("Confirm", "skin_colour:confirm") {
            val male = !get("makeover_female", false)
            val changed = body.getColour(BodyColour.Skin) != get("makeover_colour_skin", 0) || body.male != male
            body.setColour(BodyColour.Skin, get("makeover_colour_skin", 0))
            if (body.male != male) {
                swapSex(this, male)
            }
            flagAppearance()
            closeMenu()
            val mage = NPCs.find(tile.regionLevel) { it.id.startsWith("makeover_mage") }
            talkWith(mage)
            if (!changed) {
                npc<Quiz>("That is no different from what you already have. I guess I shouldn't charge you if I'm not changing anything.")
                return@interfaceOption
            }
            when (random.nextInt(0, 4)) {
                0 -> {
                    npc<Happy>("Two arms, two legs, one head; it seems that spell finally worked okay.")
                }
                1 -> {
                    npc<Amazed>("Whew! That was lucky.")
                    player<Neutral>("What was?")
                    npc<Happy>("Nothing! It's all fine! You seem alive anyway.")
                }
                2 -> {
                    npc<Quiz>("Hmm, you didn't feel any unexpected growths on your head just then, did you?")
                    player<Quiz>("Er, no?")
                    npc<Happy>("Good, good! I was worried for a second there.")
                }
                3 -> {
                    npc<Amazed>("Woah!")
                    player<Quiz>("What?")
                    npc<Amazed>("You still look human!")
                }
            }
            player<Quiz>("Uh, thanks, I guess.")
        }

        npcTimerStart("makeover") { TimeUnit.SECONDS.toTicks(250) }
        npcTimerTick("makeover", ::makeover)
    }

    fun makeover(npc: NPC): Int {
        val current: String = npc["transform_id", "makeover_mage_male"]
        val toFemale = current == "makeover_mage_male"
        npc.transform(if (toFemale) "makeover_mage_female" else "makeover_mage_male")
        npc.gfx("curse_impact", delay = 15)
        npc.anim("bind_staff")
        npc.softQueue("transform", 1) {
            npc.say(if (toFemale) "Ooh!" else "Aha!")
        }
        return Timer.CONTINUE
    }

    fun ChoiceOption.more(): Unit = option<Quiz>("Tell me more about this 'makeover'.") {
        npc<Happy>("Why, of course! Basically, and I will explain this so that you understand it correctly,")
        npc<Happy>("I use my secret magical technique to melt your body down into a puddle of its elements.")
        npc<Happy>("When I have broken down all components of your body, I then rebuild it into the form I am thinking of.")
        npc<Confused>("Or, you know, something vaguely close enough, anyway.")
        player<Quiz>("Uh... that doesn't sound particularly safe to me.")
        npc<Happy>("It's as safe as houses! Why, I have only had thirty-six major accidents this month!")
        whatDoYouSay()
    }

    suspend fun Player.whatDoYouSay() {
        npc<Confused>("So, what do you say? Feel like a change?")
        choice {
            start()
            exit()
        }
    }

    fun ChoiceOption.start(): Unit = option<Neutral>("Sure, do it.") {
        npc<Happy>("You, of course, agree that if by some accident you are turned into a frog you have no rights for compensation or refund.")
        openDressingRoom("skin_colour")
    }

    fun ChoiceOption.exit(): Unit = option("No, thanks.") {
        player<Frustrated>("No, thanks. I'm happy as I am.")
        npc<Disheartened>("Ehhh..suit yourself.")
    }

    fun ChoiceOption.amulet(): Unit = option<Pleased>("Cool amulet! Can I have one?") {
        val cost = 100
        npc<Neutral>("No problem, but please remember that the amulet I will sell you is only a copy of my own. It contains no magical powers and, as such, will only cost you $cost coins.")
        if (!carriesItem("coins", cost)) {
            player<Sad>("Oh, I don't have enough money for that.")
            return@option
        }
        choice {
            option<Happy>("Sure, here you go.") {
                inventory.transaction {
                    remove("coins", cost)
                    add("yin_yang_amulet")
                }
                when (inventory.transaction.error) {
                    TransactionError.None -> item("yin_yang_amulet", 300, "You receive an amulet in exchange for $cost coins")
                    is TransactionError.Deficient -> notEnough("coins")
                    is TransactionError.Full -> {
                        npc<Quiz>("Um...you don't seem to have room to take the amulet. Maybe you should buy it some other time.")
                        player<Neutral>("Oh yeah, that's true.")
                    }
                    else -> {}
                }
                explain()
            }
            option<Shock>("No way! That's too expensive.") {
                npc<Neutral>("That's fair enough, my jewellery is not to everyone's taste. Now, would you like a makeover?")
            }
        }
    }

    suspend fun Player.explain() {
        npc<Pleased>("I can alter your physical form if you wish. Would you like me to perform my magicks on you?")
        choice {
            more()
            start()
            exit()
        }
    }

    fun ChoiceOption.colour(): Unit = option<Pleased>("Can you make me a different colour?") {
        npc<Happy>("Why, of course! I have a wide array of colours for you to choose from.")
        whatDoYouSay()
    }

    fun swapSex(player: Player, male: Boolean) {
        player.body.male = male
        val key = "look_hair_${if (male) "male" else "female"}"
        player.body.setLook(BodyPart.Hair, EnumDefinitions.getStruct(key, random.nextInt(EnumDefinitions.get(key).length), "body_look_id"))
        player.body.setLook(BodyPart.Beard, if (male) EnumDefinitions.get("look_beard_male").randomInt() else -1)
        swapLook(player, male, BodyPart.Arms, "arms")
        swapLook(player, male, BodyPart.Hands, "wrists")
        swapLook(player, male, BodyPart.Legs, "legs")
        swapLook(player, male, BodyPart.Chest, "top")
        swapLook(player, male, BodyPart.Feet, "shoes")
    }

    fun swapLook(player: Player, male: Boolean, bodyPart: BodyPart, name: String) {
        val old = EnumDefinitions.get("look_${name}_${if (male) "female" else "male"}")
        val new = EnumDefinitions.get("look_${name}_${if (male) "male" else "female"}")
        val key = old.getKey(player.body.getLook(bodyPart))
        player.body.setLook(bodyPart, new.getInt(key))
    }
}
