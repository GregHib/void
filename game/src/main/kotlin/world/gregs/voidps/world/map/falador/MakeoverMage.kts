package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.npcTimerStart
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.effect.transform
import java.util.concurrent.TimeUnit

val enums: EnumDefinitions by inject()
val npcs: NPCs by inject()

npcOperate("Talk-to", "makeover_mage*") {
    npc<Happy>("Hello there! I am known as the Makeover Mage! I have spent many years researching magicks that can change your physical appearance.")
    npc<Happy>("I call it a 'makeover'. Would you like me to perform my magicks on you?")
    choice {
        more()
        start()
        exit()
        amulet()
        colour()
    }
}

suspend fun PlayerChoice.more(): Unit = option<Unsure>("Tell me more about this 'makeover'.") {
    npc<Cheerful>("Why, of course! Basically, and I will explain this so that you understand it correctly,")
    npc<Cheerful>("I use my secret magical technique to melt your body down into a puddle of its elements.")
    npc<Cheerful>("When I have broken down all components of your body, I then rebuild it into the form I am thinking of.")
    npc<Uncertain>("Or, you know, something vaguely close enough, anyway.")
    player<Unsure>("Uh... that doesn't sound particularly safe to me.")
    npc<Cheerful>("It's as safe as houses! Why, I have only had thirty-six major accidents this month!")
    whatDoYouSay()
}

suspend fun CharacterContext.whatDoYouSay() {
    npc<Uncertain>("So, what do you say? Feel like a change?")
    choice {
        start()
        exit()
    }
}

suspend fun PlayerChoice.start(): Unit = option<Talk>("Sure, do it.") {
    npc<Cheerful>("You, of course, agree that if by some accident you are turned into a frog you have no rights for compensation or refund.")
    openDressingRoom("skin_colour")
}

suspend fun PlayerChoice.exit(): Unit = option("No, thanks.") {
    player<Angry>("No, thanks. I'm happy as I am.")
    npc<Sad>("Ehhh..suit yourself.")
}

suspend fun PlayerChoice.amulet(): Unit = option<Happy>("Cool amulet! Can I have one?") {
    val cost = 100
    npc<Talk>("No problem, but please remember that the amulet I will sell you is only a copy of my own. It contains no magical powers and, as such, will only cost you $cost coins.")
    if (!player.holdsItem("coins", cost)) {
        player<Upset>("Oh, I don't have enough money for that.")
        return@option
    }
    choice {
        option<Cheerful>("Sure, here you go.") {
            player.inventory.transaction {
                remove("coins", cost)
                add("yin_yang_amulet")
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> item("yin_yang_amulet", 300, "You receive an amulet in exchange for $cost coins")
                is TransactionError.Deficient -> player.notEnough("coins")
                is TransactionError.Full -> {
                    npc<Unsure>("Um...you don't seem to have room to take the amulet. Maybe you should buy it some other time.")
                    player<Talk>("Oh yeah, that's true.")
                }
                else -> {}
            }
            explain()
        }
        option<Surprised>("No way! That's too expensive.") {
            npc<Talk>("That's fair enough, my jewellery is not to everyone's taste. Now, would you like a makeover?")
        }
    }
}

suspend fun CharacterContext.explain() {
    npc<Happy>("I can alter your physical form if you wish. Would you like me to perform my magicks on you?")
    choice {
        more()
        start()
        exit()
    }
}

suspend fun PlayerChoice.colour(): Unit = option<Happy>("Can you make me a different colour?") {
    npc<Cheerful>("Why, of course! I have a wide array of colours for you to choose from.")
    whatDoYouSay()
}

npcOperate("Makeover", "makeover_mage*") {
    openDressingRoom("skin_colour")
}

interfaceClose("skin_colour") { player: Player ->
    player.softTimers.stop("dressing_room")
}

interfaceOpen("skin_colour") { player: Player ->
    player["makeover_female"] = !player.male
    player["makeover_colour_skin"] = player.body.getColour(BodyColour.Skin)
    player.interfaces.sendText(id, "confirm", "CONFIRM")
}

interfaceOption("skin_colour", "female") {
    player["makeover_female"] = true
    player.sendVariable("makeover_colour_skin")
}

interfaceOption("skin_colour", "male") {
    player["makeover_female"] = false
    player.sendVariable("makeover_colour_skin")
}

interfaceOption("skin_colour", "colour_*") {
    player["makeover_colour_skin"] = enums.get("character_skin").getInt(component.removePrefix("colour_").toInt())
}

interfaceOption("skin_colour", "confirm") {
    val male = !player["makeover_female", false]
    val changed = player.body.getColour(BodyColour.Skin) != player["makeover_colour_skin", 0] || player.body.male != male
    player.body.setColour(BodyColour.Skin, player["makeover_colour_skin", 0])
    if (player.body.male != male) {
        swapSex(player, male)
    }
    player.flagAppearance()
    player.closeMenu()
    val mage = npcs[player.tile.regionLevel].first { it.id.startsWith("makeover_mage") }
    player.talkWith(mage)
    if (!changed) {
        npc<Unsure>("That is no different from what you already have. I guess I shouldn't charge you if I'm not changing anything.")
        return@interfaceOption
    }
    when (random.nextInt(0, 4)) {
        0 -> {
            npc<Cheerful>("Two arms, two legs, one head; it seems that spell finally worked okay.")
        }
        1 -> {
            npc<Amazed>("Whew! That was lucky.")
            player<Talk>("What was?")
            npc<Cheerful>("Nothing! It's all fine! You seem alive anyway.")
        }
        2 -> {
            npc<Unsure>("Hmm, you didn't feel any unexpected growths on your head just then, did you?")
            player<Unsure>("Er, no?")
            npc<Cheerful>("Good, good! I was worried for a second there.")
        }
        3 -> {
            npc<Amazed>("Woah!")
            player<Unsure>("What?")
            npc<Amazed>("You still look human!")
        }
    }
    player<Unsure>("Uh, thanks, I guess.")
}

fun swapSex(player: Player, male: Boolean) {
    player.body.male = male
    val key = "look_hair_${if (male) "male" else "female"}"
    player.body.setLook(BodyPart.Hair, enums.getStruct(key, enums.get(key).randomInt(), "body_look_id"))
    player.body.setLook(BodyPart.Beard, if (male) enums.get("look_beard_male").randomInt() else -1)
    swapLook(player, male, BodyPart.Arms, "arms")
    swapLook(player, male, BodyPart.Hands, "wrists")
    swapLook(player, male, BodyPart.Legs, "legs")
    swapLook(player, male, BodyPart.Chest, "top")
    swapLook(player, male, BodyPart.Feet, "shoes")
}

fun swapLook(player: Player, male: Boolean, bodyPart: BodyPart, name: String) {
    val old = enums.get("look_${name}_${if (male) "female" else "male"}")
    val new = enums.get("look_${name}_${if (male) "male" else "female"}")
    val key = old.getKey(player.body.getLook(bodyPart))
    player.body.setLook(bodyPart, new.getInt(key))
}

npcSpawn({ it.id.startsWith("makeover_mage") }) { npc: NPC ->
    npc.softTimers.start("makeover")
}

npcTimerStart({ timer == "makeover" }) { _: NPC ->
    interval = TimeUnit.SECONDS.toTicks(250)
}

npcTimerTick({ timer == "makeover" }) { npc: NPC ->
    val current: String = npc["transform_id", "makeover_mage_male"]
    val toFemale = current == "makeover_mage_male"
    npc.transform = if (toFemale) "makeover_mage_female" else "makeover_mage_male"
    npc.setGraphic("curse_hit", delay = 15)
    npc.setAnimation("bind_staff")
    npc.softQueue("transform", 1) {
        npc.forceChat = if (toFemale) "Ooh!" else "Aha!"
    }
}