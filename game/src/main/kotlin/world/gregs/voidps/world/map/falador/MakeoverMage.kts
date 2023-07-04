package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notEnough
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.BodyColour
import world.gregs.voidps.network.visual.update.player.BodyPart
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.effect.transform
import java.util.concurrent.TimeUnit
import kotlin.random.Random

val enums: EnumDefinitions by inject()
val npcs: NPCs by inject()

on<NPCOption>({ operate && npc.id.startsWith("makeover_mage") && option == "Talk-to" }) { player: Player ->
    npc<Happy>("""
        Hello there! I am known as the Makeover Mage! I have
        spent many years researching magicks that can change
        your physical appearance.
    """)
    npc<Happy>("""
        I call it a 'makeover'.
        Would you like me to perform my magicks on you?
    """)
    val choice = choice("""
        Tell me more about this 'makeover'.
        Sure, do it.
        No, thanks.
        Cool amulet! Can I have one?
        Can you make me a different colour?
    """)
    when (choice) {
        1 -> more()
        2 -> start()
        3 -> exit()
        4 -> amulet()
        5 -> colour()
    }
}

suspend fun Interaction.more() {
    player<Unsure>("Tell me more about this 'makeover'.")
    npc<Cheerful>("""
        Why, of course! Basically, and I will explain this so that
        you understand it correctly,
    """)
    npc<Cheerful>("""
        I use my secret magical technique to melt your body down
        into a puddle of its elements.
    """)
    npc<Cheerful>("""
        When I have broken down all components of your body, I
        then rebuild it into the form I am thinking of.
    """)
    npc<Uncertain>("Or, you know, something vaguely close enough, anyway.")
    player<Unsure>("Uh... that doesn't sound particularly safe to me.")
    npc<Cheerful>("""
        It's as safe as houses! Why, I have only had thirty-six
        major accidents this month!
    """)
    whatDoYouSay()
}

suspend fun Interaction.whatDoYouSay() {
    npc<Uncertain>("So, what do you say? Feel like a change?")
    val choice = choice("""
        Sure, do it.
        No, thanks
    """)
    if (choice == 1) {
        start()
    } else if (choice == 2) {
        exit()
    }
}

suspend fun Interaction.start() {
    player<Talk>("Sure, do it.")
    npc<Cheerful>("""
        You, of course, agree that if by some accident you are
        turned into a frog you have no rights for compensation or
        refund.
    """)
    startMakeover()
}

suspend fun Interaction.exit() {
    player<Angry>("No, thanks. I'm happy as I am.")
    npc<Sad>("Ehhh..suit yourself.")
}

suspend fun Interaction.amulet() {
    player<Happy>("Cool amulet! Can I have one?")
    val cost = 100
    npc<Talk>("""
        No problem, but please remember that the amulet I will
        sell you is only a copy of my own. It contains no magical
        powers and, as such, will only cost you $cost coins.
    """)
    if (!player.hasItem("coins", cost)) {
        player<Upset>("Oh, I don't have enough money for that.")
        return
    }
    val choice = choice("""
        Sure, here you go.
        No way! That's too expensive.
    """)
    if (choice == 1) {
        player<Cheerful>("Sure, here you go.")
        player.inventory.transaction {
            remove("coins", cost)
            add("yin_yang_amulet")
        }
        when (player.inventory.transaction.error) {
            TransactionError.None -> item("You receive an amulet in exchange for $cost coins", "yin_yang_amulet", 300)
            is TransactionError.Deficient -> player.notEnough("coins")
            is TransactionError.Full -> {
                npc<Unsure>("""
                    Um...you don't seem to have room to take the amulet.
                    Maybe you should buy it some other time.
                """)
                player<Talk>("Oh yeah, that's true.")
            }
            else -> {}
        }
        explain()
    } else if (choice == 2) {
        player<Surprised>("No way! That's too expensive.")
        npc<Talk>("""
            That's fair enough, my jewellery is not to everyone's
            taste. Now, would you like a makeover?
        """)
    }
}

suspend fun Interaction.explain() {
    npc<Happy>("""
        I can alter your physical form if you wish. Would you like
        me to perform my magicks on you?
    """)
    val choice = choice("""
        Tell me more about this 'makeover'.
        Sure, do it.
        No, thanks
    """)
    when (choice) {
        1 -> more()
        2 -> start()
        3 -> exit()
    }
}

suspend fun Interaction.colour() {
    player<Happy>("Can you make me a different colour?")
    npc<Cheerful>("""
        Why, of course! I have a wide array of colours for you to
        choose from.
    """)
    whatDoYouSay()
}

on<NPCOption>({ operate && npc.id.startsWith("makeover_mage") && option == "Makeover" }) { player: Player ->
    startMakeover()
}

suspend fun Interaction.startMakeover() {
    openDressingRoom("skin_colour")
}

on<InterfaceClosed>({ id == "skin_colour" }) { player: Player ->
    player.softTimers.stop("dressing_room")
}

on<InterfaceOpened>({ id == "skin_colour" }) { player: Player ->
    player["makeover_female"] = !player.male
    player["makeover_colour_skin"] = player.body.getColour(BodyColour.Skin)
    player.interfaces.sendText(id, "confirm", "CONFIRM")
}

on<InterfaceOption>({ id == "skin_colour" && component == "female" }) { player: Player ->
    player["makeover_female"] = true
    player.sendVariable("makeover_colour_skin")
}

on<InterfaceOption>({ id == "skin_colour" && component == "male" }) { player: Player ->
    player["makeover_female"] = false
    player.sendVariable("makeover_colour_skin")
}

on<InterfaceOption>({ id == "skin_colour" && component.startsWith("colour_") }) { player: Player ->
    player["makeover_colour_skin"] = enums.get("character_skin").getInt(component.removePrefix("colour_").toInt())
}

on<InterfaceOption>({ id == "skin_colour" && component == "confirm" }) { player: Player ->
    val male = !player.get<Boolean>("makeover_female")
    val changed = player.body.getColour(BodyColour.Skin) != player["makeover_colour_skin"] || player.body.male != male
    player.body.setColour(BodyColour.Skin, player["makeover_colour_skin"])
    if (player.body.male != male) {
        swapSex(player, male)
    }
    player.flagAppearance()
    player.closeMenu()
    val mage = npcs[player.tile.regionLevel].first { it.id.startsWith("makeover_mage") }
    player.talkWith(mage)
    if (!changed) {
        npc<Unsure>("""
            That is no different from what you already have. I guess I
            shouldn't charge you if I'm not changing anything.
        """)
        return@on
    }
    when (Random.nextInt(0, 4)) {
        0 -> {
            npc<Cheerful>("""
                Two arms, two legs, one head; it seems that spell finally
                worked okay.
            """)
        }
        1 -> {
            npc<Amazed>("Whew! That was lucky.")
            player<Talk>("What was?")
            npc<Cheerful>("Nothing! It's all fine! You seem alive anyway.")
        }
        2 -> {
            npc<Unsure>("""
                Hmm, you didn't feel any unexpected growths on your
                head just then, did you?
            """)
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
    player.body.setLook(BodyPart.Hair, enums.getStruct(key, enums.get(key).randomInt(), "id"))
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

on<Registered>({ it.id.startsWith("makeover_mage") }) { npc: NPC ->
    npc.softTimers.start("makeover")
}

on<TimerStart>({ timer == "makeover" }) { _: NPC ->
    interval = TimeUnit.SECONDS.toTicks(250)
}

on<TimerTick>({ timer == "makeover" }) { npc: NPC ->
    val current: String = npc["transform_id", "makeover_mage_male"]
    val toFemale = current == "makeover_mage_male"
    npc.transform = if (toFemale) "makeover_mage_female" else "makeover_mage_male"
    npc.setGraphic("curse_hit", delay = 15)
    npc.setAnimation("bind_staff")
    npc.softQueue("transform", 1) {
        npc.forceChat = if (toFemale) "Ooh!" else "Aha!"
    }
}