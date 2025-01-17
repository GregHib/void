package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playSound

npcOperate("Talk-to", "sir_prysin") {
    when (player.quest("demon_slayer")) {
        "key_hunt" -> {
            if (!player["demon_slayer_silverlight", false]) {
                keyProgressCheck()
                return@npcOperate
            }
            npc<Talk>("Have you sorted that demon out yet?")
            if (player.ownsItem("silverlight")) {
                player<Upset>("No, not yet.")
                npc<Talk>("Well get on with it. He'll be pretty powerful when he gets to full strength.")
                return@npcOperate
            }
            player<Upset>("Not yet. And I, um, lost Silverlight.")
            if (player.inventory.add("silverlight")) {
                npc<Angry>("Yes, I know, someone returned it to me. Take better care of it this time.")
            } else {
                npc<Angry>("Yes, I know, someone returned it to me. I'll keep it until you have free inventory space.")
            }
        }
        "completed" -> {
            npc<Neutral>("Hello. I've heard you stopped the demon, well done.")
            player<Neutral>("Yes, that's right.")
            npc<Neutral>("A good job well done then.")
            player<Neutral>("Thank you.")
        }
        else -> {
            npc<Talk>("Hello, who are you?")
            choice {
                mightyAdventurer()
                youTellMe()
                arisWantsToTalk()
            }
        }
    }
}

suspend fun PlayerChoice.arisWantsToTalk(): Unit = option(
    "Aris said I should come and talk to you.",
    { player.quest("demon_slayer") == "sir_prysin" }
) {
    player<Talk>("Aris said I should come and talk to you.")
    npc<Talk>("Aris? Is she still alive? I remember her from when I was pretty young. Well what do you need to talk to me about?")
    choice {
        option("I need to find Silverlight.") {
            findSilverlight()
        }
        option("Yes, she is still alive.") {
            player<Happy>("Yes she is still alive. She lives right outside the castle!")
            npc<Talk>("Oh, is that the same Aris? I would have thought she would have died by now. She was pretty old when I was a lad.")
            npc<Talk>("Anyway, what can I do for you?")
            findSilverlight()
        }
    }
}

suspend fun CharacterContext<Player>.findSilverlight() {
    player<Talk>("I need to find Silverlight.")
    npc<Talk>("What do you need to find that for?")
    player<Talk>("I need it to fight Delrith.")
    npc<Talk>("Delrith? I thought the world was rid of him, thanks to my great-grandfather.")
    choice {
        option("Well, Aris' crystal ball seems to think otherwise.") {
            player<Talk>("Well Aris' crystal ball seems to think otherwise.")
            npc<Talk>("Well if the ball says so, I'd better help you.")
            problemIs()
        }
        option("He's back and unfortunately I've got to deal with him.") {
            player<Upset>("He's back and unfortunately I've got to deal with him.")
            npc<Talk>("You don't look up to much. I suppose Silverlight may be good enough to carry you through though.")
            problemIs()
        }
    }
}

suspend fun CharacterContext<Player>.problemIs() {
    npc<Talk>("The problem is getting Silverlight.")
    player<Upset>("You mean you don't have it?")
    npc<Talk>("Oh I do have it, but it is so powerful that the king made me put it in a special box which needs three different keys to open it. That way it won't fall into the wrong hands.")
    choice {
        option("So give me the keys!") {
            player<Angry>("So give me the keys!")
            npc<Upset>("Um, well, it's not so easy.")
            theKeys()
        }
        option("And why is this a problem?") {
            player<Talk>("And why is this a problem?")
            theKeys()
        }
    }
}

suspend fun CharacterContext<Player>.theKeys() {
    npc<Talk>("I kept one of the keys. I gave the other two to other people for safe keeping.")
    npc<Talk>("One I gave to Rovin, the captain of the palace guard.")
    npc<Talk>("I gave the other to the wizard Traiborn.")
    player["demon_slayer"] = "key_hunt"
    choice {
        giveYourKey()
        wheresCaptainRovin()
        wheresWizard()
    }
}

suspend fun PlayerChoice.wheresWizard(): Unit = option("Where does the wizard live?") {
    player<Talk>("Where does the wizard live?")
    npc<Talk>("He is one of the wizards who lives in the tower on the little island just off the south coast. I believe his quarters are on the first floor of the tower.")
    choice {
        giveYourKey()
        wheresCaptainRovin()
        huntingTime()
    }
}

suspend fun PlayerChoice.wheresCaptainRovin(): Unit = option("Where can I find Captain Rovin?") {
    player<Talk>("Where can I find Captain Rovin?")
    npc<Talk>("Captain Rovin lives at the top of the guards' quarters in the north-west wing of this palace.")
    choice {
        giveYourKey()
        wheresWizard()
        huntingTime()
    }
}

suspend fun NPCOption<Player>.keyProgressCheck() {
    npc<Talk>("So how are you doing with getting the keys?")
    val rovin = player.holdsItem("silverlight_key_captain_rovin")
    val prysin = player.holdsItem("silverlight_key_sir_prysin")
    val traiborn = player.holdsItem("silverlight_key_wizard_traiborn")
    when {
        rovin && prysin && traiborn -> {
            giveSilverlight()
            return
        }
        prysin && (rovin || traiborn) -> {
            player<Talk>("I've got the key from ${if (traiborn) "Wizard Traiborn" else "Captain Rovin"} and the one that you dropped down the drain.")
        }
        traiborn && rovin -> player<Talk>("I've got the keys from Wizard Traiborn and Captain Rovin.")
        rovin -> player<Talk>("I've got the key from Captain Rovin.")
        traiborn -> player<Talk>("I've got the key from Wizard Traiborn.")
        prysin -> player<Talk>("I've got the key which you dropped down the drain.")
        else -> player<Upset>("I haven't found any of them yet.")
    }
    choice {
        remindMe()
        stillLooking()
    }
}

suspend fun PlayerChoice.giveYourKey(): Unit = option("Can you give me your key?") {
    player<Talk>("Can you give me your key?")
    npc<Upset>("Um.... ah....")
    npc<Upset>("Well there's a problem there as well.")
    npc<Upset>("I managed to drop the key in the drain just outside the palace kitchen. It is just inside and I can't reach it.")
    choice {
        drain()
        wheresCaptainRovin()
        wheresWizard()
    }
}

suspend fun PlayerChoice.drain(): Unit = option("So what does the drain lead to?") {
    player<Talk>("So what does the drain connect to?")
    npc<Talk>("It is the drain for the drainpipe running from the sink in the kitchen down to the palace sewers.")
    choice {
        wheresCaptainRovin()
        wheresWizard()
        huntingTime()
    }
}

suspend fun PlayerChoice.huntingTime(): Unit = option("Well I'd better go key hunting.") {
    player<Talk>("Well I'd better go key hunting.")
    npc<Talk>("Ok, goodbye.")
}

suspend fun PlayerChoice.mightyAdventurer(): Unit = option("I am a mighty adventurer. Who are you?") {
    player<Talk>("I am a mighty adventurer, who are you?")
    npc<Talk>("I am Sir Prysin. A bold and famous knight of the realm.")
}

suspend fun PlayerChoice.youTellMe(): Unit = option("I'm not sure, I was hoping you could tell me.") {
    player<Uncertain>("I was hoping you could tell me.")
    npc<Talk>("Well I've never met you before.")
}

suspend fun PlayerChoice.remindMe(): Unit = option("Can you remind me where all the keys were again?") {
    player<Talk>("Can you remind me where all the keys were again?")
    theKeys()
}

suspend fun PlayerChoice.stillLooking(): Unit = option("I'm still looking.") {
    player<Talk>("I'm still looking.")
    npc<Talk>("Ok, tell me when you've got them all.")
}

val objects: GameObjects by inject()
val cupboardTile = Tile(3204, 3469)

suspend fun NPCOption<Player>.giveSilverlight() {
    player<Neutral>("I've got all three keys!")
    npc<Neutral>("Excellent! Now I can give you Silverlight.")
    player.inventory.remove("silverlight_key_wizard_traiborn", "silverlight_key_captain_rovin", "silverlight_key_sir_prysin")
    val tile = Tile(3204, 3470)
    target.mode = PauseMode
    target.clearWatch()
    target.steps.clear()
    delay(1)
    target.tele(tile, clearMode = false)
    player.tele(tile.addY(1))
    val cupboard = objects[cupboardTile, "silverlight_sword_case_closed"]!!
    delay(1)
    target.face(cupboard)
    player.face(target)
    cupboard.animate("silverlight_sword_case_open")
    target.setAnimation("silverlight_unlock_sword_case")
    player.playSound("cupboard_open", delay = 19)
    delay(3)
    player.playSound("cupboard_open")
    delay(2)
    player.playSound("cupboard_open", delay = 10)
    delay(2)
    target.setAnimation("silverlight_open_sword_case")
    cupboard.animate("silverlight_sword_removed")
    delay(8)
    player["demon_slayer_silverlight_case"] = "open"
    player.playSound("casket_open")
    target.setAnimation("silverlight_remove_sword")
    delay()
    player["demon_slayer_sir_prysin_sword"] = true
    player["demon_slayer_silverlight_case"] = "empty"
    delay(2)
    target.face(player)
    delay(2)
    target.setAnimation("silverlight_hand_over")
    player.setAnimation("silverlight_take")
    delay()
    player["demon_slayer_silverlight"] = true
    player["demon_slayer_sir_prysin_sword"] = false
    player.inventory.add("silverlight")
    item("silverlight", 600, "Sir Prysin hands you a very shiny sword.")
    player.setAnimation("silverlight_showoff")
    player.setGraphic("silverlight_sparkle")
    player.playSound("equip_silverlight")
    delay()
    target.face(Direction.NONE)
    delay()
    npc<Talk>("That sword belonged to my great-grandfather. Make sure you treat it with respect!")
    npc<Neutral>("Now go kill that demon!")
}