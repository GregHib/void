package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.effect.transform
import world.gregs.voidps.world.interact.entity.sound.playSound

val items: FloorItems by inject()

on<NPCOption>({ npc.id == "sir_prysin" && option == "Talk-to" }) { player: Player ->
    npc<Talk>("Hello, who are you?")
    when (player["demon_slayer", "unstarted"]) {
        "sir_prysin" -> arisChoice()
        "key_hunt" -> keyProgressCheck()
        "kill_demon" -> {
            npc<Talk>("Have you sorted that demon out yet?")
            if (player.hasBanked("silverlight")) {
                player<Upset>("No, not yet.")
                npc<Talk>("""
                    Well get on with it. He'll be pretty powerful when he
                    gets to full strength.
                """)
                return@on
            }
            player<Upset>("Not yet. And I, um, lost Silverlight.")
            if (player.inventory.add("silverlight")) {
                npc<Furious>("""
                    Yes, I know, someone returned it to me. Take better
                    care of it this time.
                """)
            } else {
                npc<Furious>("""
                    Yes, I know, someone returned it to me. I'll keep it
                    until you have free inventory space.")
                """)
            }
        }
        else -> regularChoice()
    }
}

suspend fun NPCOption.arisWantsToTalk() {
    player<Talk>("Aris said I should come and talk to you.")
    npc<Talk>("""
        Aris? Is she still alive? I remember her from when I
        was pretty young. Well what do you need to talk to me
        about?
    """)
    val choice = choice("""
        I need to find Silverlight.
        Yes, she is still alive.
    """)
    when (choice) {
        1 -> findSilverlight()
        2 -> {
            player<Cheerful>("Yes she is still alive. She lives right outside the castle!")
            npc<Talk>("""
                Oh, is that the same Aris? I would have thought she
                would have died by now. She was pretty old when I
                was a lad.
            """)
            npc<Talk>("Anyway, what can I do for you?")
            findSilverlight()
        }
    }
}

suspend fun NPCOption.findSilverlight() {
    player<Talk>("I need to find Silverlight.")
    npc<Talk>("What do you need to find that for?")
    player<Talk>("I need it to fight Delrith.")
    npc<Talk>("""
        Delrith? I thought the world was rid of him, thanks to
        my great-grandfather.
    """)
    val choice = choice("""
        Well, Aris' crystal ball seems to think otherwise.
        He's back and unfortunately I've got to deal with him.
    """)
    when (choice) {
        1 -> {
            player<Talk>("Well Aris' crystal ball seems to think otherwise.")
            npc<Talk>("Well if the ball says so, I'd better help you.")
            problemIs()
        }
        2 -> {
            player<Upset>("He's back and unfortunately I've got to deal with him.")
            npc<Talk>("""
                You don't look up to much. I suppose Silverlight may be
                good enough to carry you through though.
            """)
            problemIs()
        }
    }
}

suspend fun NPCOption.problemIs() {
    npc<Talk>("The problem is getting Silverlight.")
    player<Upset>("You mean you don't have it?")
    npc<Talk>("""
        Oh I do have it, but it is so powerful that the king
        made me put it in a special box which needs three
        different keys to open it. That way it won't fall into the
        wrong hands.
    """)
    val choice = choice("""
        So give me the keys!
        And why is this a problem?
    """)
    when (choice) {
        1 -> {
            player<Furious>("So give me the keys!")
            npc<Upset>("Um, well, it's not so easy.")
            theKeys()
        }
        2 -> {
            player<Talk>("And why is this a problem?")
            theKeys()
        }
    }
}

suspend fun NPCOption.theKeys() {
    npc<Talk>("""
        I kept one of the keys. I gave the other two to other
        people for safe keeping.
    """)
    npc<Talk>("One I gave to Rovin, the captain of the palace guard.")
    npc<Talk>("I gave the other to the wizard Traiborn.")
    player["demon_slayer"] = "key_hunt"
    val choice = choice("""
        Can you give me your key?
        Where can I find Captain Rovin?
        Where does the wizard live?
    """)
    when (choice) {
        1 -> giveYourKey()
        2 -> wheresCaptainRovin()
        3 -> wheresWizard()
    }
}

suspend fun NPCOption.wheresWizard() {
    player<Talk>("Where does the wizard live?")
    npc<Talk>("""
        He is one of the wizards who lives in the tower on the
        little island just off the south coast. I believe his
        quarters are on the first floor of the tower.
    """)
    val choice = choice("""
        Can you give me your key?
        Where can I find Captain Rovin?
        Well I'd better go key hunting.
    """)
    when (choice) {
        1 -> giveYourKey()
        2 -> wheresCaptainRovin()
        3 -> huntingTime()
    }
}

suspend fun NPCOption.wheresCaptainRovin() {
    player<Talk>("Where can I find Captain Rovin?")
    npc<Talk>("""
        Captain Rovin lives at the top of the guards' quarters in
        the north-west wing of this palace.
    """)
    val choice = choice("""
        Can you give me your key?
        Where does the wizard live?
        Well I'd better go key hunting.
    """)
    when (choice) {
        1 -> giveYourKey()
        2 -> wheresWizard()
        3 -> huntingTime()
    }
}

suspend fun NPCOption.keyProgressCheck() {
    npc<Talk>("So how are you doing with getting the keys?")
    val rovin = player.hasItem("silverlight_key_captain_rovin")
    val prysin = player.hasItem("silverlight_key_sir_prysin")
    val traiborn = player.hasItem("silverlight_key_wizard_traiborn")
    when {
        rovin && prysin && traiborn -> {
            giveSilverlight()
            return
        }
        prysin && (rovin || traiborn) -> {
            player<Talk>("""
                I've got the key from ${if (traiborn) "Wizard Traiborn" else "Captain Rovin"} and the one that
                you dropped down the drain.
            """)
        }
        traiborn && rovin -> player<Talk>("""
            I've got the keys from Wizard Traiborn and Captain
            Rovin.
        """)
        rovin -> player<Talk>("I've got the key from Captain Rovin.")
        traiborn -> player<Talk>("I've got the key from Wizard Traiborn.")
        prysin -> player<Talk>("I've got the key which you dropped down the drain.")
        else -> player<Upset>("I haven't found any of them yet.")
    }
    val choice = choice("""
        Can you remind me where all the keys were again?
        I'm still looking.
    """)
    when (choice) {
        1 -> remindMe()
        2 -> stillLooking()
    }
}

suspend fun NPCOption.giveYourKey() {
    player<Talk>("Can you give me your key?")
    npc<Upset>("Um.... ah....")
    npc<Upset>("Well there's a problem there as well.")
    npc<Upset>("""
        I managed to drop the key in the drain just outside the
        palace kitchen. It is just inside and I can't reach it.
    """)
    val choice = choice("""
        So what does the drain lead to?
        Where can I find Captain Rovin?
        Where does the wizard live?
    """)
    when (choice) {
        1 -> drain()
        2 -> wheresCaptainRovin()
        3 -> wheresWizard()
    }
}

suspend fun NPCOption.drain() {
    player<Talk>("So what does the drain connect to?")
    npc<Talk>("""
        It is the drain for the drainpipe running from the sink
        in the kitchen down to the palace sewers.
    """)
    val choice = choice("""
        Where can I find Captain Rovin?
        Where does the wizard live?
        Well I'd better go key hunting.
    """)
    when (choice) {
        1 -> wheresCaptainRovin()
        2 -> wheresWizard()
        3 -> huntingTime()
    }
}

suspend fun NPCOption.huntingTime() {
    player<Talk>("Well I'd better go key hunting.")
    npc<Talk>("Ok, goodbye.")
}

suspend fun NPCOption.mightyAdventurer() {
    player<Talk>("I am a mighty adventurer, who are you?")
    npc<Talk>("""
        I am Sir Prysin. A bold and famous knight of the
        realm.
    """)
}

suspend fun NPCOption.youTellMe() {
    player<Uncertain>("I was hoping you could tell me.")
    npc<Talk>("Well I've never met you before.")
}

suspend fun NPCOption.remindMe() {
    player<Talk>("Can you remind me where all the keys were again?")
    theKeys()
}

suspend fun NPCOption.stillLooking() {
    player<Talk>("I'm still looking.")
    npc<Talk>("Ok, tell me when you've got them all.")
}

suspend fun NPCOption.giveSilverlight() {
    player<Talking>("I've got all three keys!")
    npc<Talking>("Excellent! Now I can give you Silverlight.")
    player["demon_slayer"] = "kill_demon"
    val tile = Tile(3204, 3470)
    npc.walkTo(tile)
    pause(2)
    npc.tele(tile)
    npc.face(Direction.SOUTH)
    pause()
    npc.setAnimation("4597", 19)
    player.playSound("cupboard_open", delay = 19)
    pause()
    player["demon_slayer_silverlight_case"] = "open"
    player.playSound("casket_open")
    pause()
    npc.setAnimation("4598")
    npc.transform("sir_prysin_silverlight")
    player["demon_slayer_silverlight_case"] = "empty"
    pause()
    player["demon_slayer_silverlight_case"] = "closed"
    npc.setAnimation("4606")
    npc.face(player)
    pause()
    npc.setAnimation("4607")
    pause()
    player.inventory.add("silverlight")
    if (player.inventory.transaction.error != TransactionError.None) {
        items.add("silverlight", 1, player.tile)
    }
    item("Sir Prysin hands you a very shiny sword.", "silverlight", 400)
    player.setAnimation("4604")
    player.setGraphic("778")
    npc.setAnimation("4608")
    player.playSound("equip_silverlight")
    npc.transform("sir_prysin")
    pause()
    npc.face(Direction.NONE)
    pause()
    npc<Talk>("""
        That sword belonged to my great-grandfather. Make
        sure you treat it with respect!
    """)
    npc<Talking>("Now go kill that demon!")
}

suspend fun NPCOption.regularChoice() {
    val choice = choice("""
        I am a mighty adventurer. Who are you?
        I'm not sure, I was hoping you could tell me.
    """)
    when (choice) {
        1 -> mightyAdventurer()
        2 -> youTellMe()
    }
}

suspend fun NPCOption.arisChoice() {
    val choice = choice("""
        I am a mighty adventurer. Who are you?
        I'm not sure, I was hoping you could tell me.
        Aris said I should come and talk to you.
    """)
    when (choice) {
        1 -> mightyAdventurer()
        2 -> youTellMe()
        3 -> arisWantsToTalk()
    }
}