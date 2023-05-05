import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement

on<NPCOption>({ npc.id == "bank_guard" && option == "Talk-to" }) { player: Player ->
    npc<Talk>("Yes?")
    when (player["draynor_bank_robbery", "unstarted"]) {
        "unstarted" -> unstarted()
        "idea" -> {
            player<Unsure>("Do you have any idea who robbed the bank yet?")
            doYouKnowWho()
        }
        "seen" -> {
            val choice = choice("""
                Can I see that recording again, please?
                Sorry, I don't want anything.
            """)
            when (choice) {
                1 -> replayRecording()
                2 -> dontWantAnything()
            }
        }
    }

}

suspend fun NPCOption.unstarted() {
    val choice = choice("""
        Can I deposit my stuff here?
        That wall doesn't look very good.
        Sorry, I don't want anything.
    """)
    when (choice) {
        1 -> canIDeposit()
        2 -> wallLookBad()
        3 -> dontWantAnything()
    }
}

suspend fun NPCOption.canIDeposit() {
    player<Cheerful>("Hello. Can I deposit my stuff here?")
    npc<Talk>("No. I'm a security guard, not a bank clerk.")
    val choice = choice("""
        That wall doesn't look very good.
        Alright, I'll stop bothering you now.
    """)
    when (choice) {
        1 -> wallLookBad()
        2 -> stopBothering()
    }
}

suspend fun NPCOption.wallLookBad() {
    player<Unsure>("That wall doesn't look very good.")
    npc<Talk>("No, it doesn't.")
    val choice = choice("""
        Are you going to tell me what happened?
        Alright, I'll stop bothering you now.
    """)
    when (choice) {
        1 -> whatHappened()
        2 -> stopBothering()
    }
}

suspend fun NPCOption.stopBothering() {
    player<Talk>("Alright, I'll stop bothering you now.")
    npc<Talk>("Good day, sir.")
}

suspend fun NPCOption.whatHappened() {
    player<Cheerful>("Are you going to tell me what happened?")
    npc<Talk>("I could do.")
    npc<Cheerful>("Ok, go on!")
    player<Talk>("""
        Someone smashed the wall when
        they were robbing the bank.
    """)
    player<Surprised>("Someone's robbed the bank?")
    npc<Talk>("Yes.")
    player<Surprised>("""
        But... was anyone hurt?
        Did they get anything valuable?
    """)
    npc<Talk>("""
        Yes, but we were able to get more staff and mend the
        wall easily enough.
    """)
    npc<Talk>("""
        The Bank has already replaced all the stolen items that
        belonged to customers.
    """)
    player<Surprised>("Oh, good... but the bank staff got hurt?")
    npc<Talk>("Yes, but the new ones are just as good.")
    player<Talk>("You're not very nice, are you?")
    npc<Talk>("No-one's expecting me to be nice.")
    player<Surprised>("Anyway... So, someone's robbed the bank?")
    npc<Talk>("Yes.")
    player<Unsure>("Do you know who did it?")
    player["draynor_bank_robbery"] = "idea"
    doYouKnowWho()
}

suspend fun NPCOption.whosRobber() {
    player<Talking>("So who was the robber?")
    npc<Talk>("I can't disclose that information.")
    val choice = choice("""
        Can I see the recording?
        Alright, I'll stop bothering you now.
    """)
    when (choice) {
        1 -> seeRecording()
        2 -> stopBothering()
    }
}

suspend fun NPCOption.seeRecording() {
    player<Talking>("Can I see the recording?")
    npc<Talk>("I suppose so. But it's quite long.")
    val choice = choice("""
        That's ok, show me the recording.
        Thanks, maybe another day.
    """)
    when (choice) {
        1 -> {
            player<Talking>("That's ok, show me the recording.")
            npc<Talk>("""
                Alright... The bank's magical playback device will feed the
                recorded images into your mind. Just shut your eyes.
            """)
            player["draynor_bank_robbery"] = "seen"
            playRecording()
        }
        2 -> anotherDay()
    }
}

suspend fun NPCOption.doYouKnowWho() {
    npc<Talk>("""
        We are fairly sure we know who the robber was. The
        security recording was damaged in the attack, but it still
        shows his face clearly enough.
    """)
    player<Unsure>("You've got a security recording?")
    npc<Talk>("""
        Yes. Our insurers insisted that we
        install a magical scrying orb.
    """)
    val choice = choice("""
        Can I see the recording?
        So who was the robber?
    """)
    when (choice) {
        1 -> seeRecording()
        2 -> whosRobber()
    }
}

suspend fun NPCOption.dontWantAnything() {
    player<Talk>("Sorry, I don't want anything.")
    npc<Talk>("Ok.")
}

suspend fun NPCOption.replayRecording() {
    player<Talk>("Can I see that recording again, please?")
    npc<Unsure>("I'd like you to pay me 50 gp first.")
    if (player.inventory.contains("coins", 50)) {
        player<Talking>("Ok, here's 50 gp.")
        player.inventory.remove("coins", 50)
        playRecording()
    } else {
        player<Upset>("I'm not carrying that much.")
        if (!player.bank.contains("coins", 50)) {
            npc<Talk>("Oh well, maybe another day.")
            return
        }
        npc<Unsure>("""
            As a bank employee, I suppose I could take the money
            directly from your bank account.
        """)
        val choice = choice("""
            Ok, you can take 50 gp from my bank account.
            Thanks, maybe another day.
        """)
        when (choice) {
            1 -> {
                player<Talking>("Ok, you can take 50 gp from my bank account.")
                player.bank.remove("coins", 50)
                playRecording()
            }
            2 -> anotherDay()
        }
    }
}

suspend fun NPCOption.anotherDay() {
    player<Talk>("Thanks, maybe another day.")
    npc<Talk>("Ok.")
}

suspend fun NPCOption.playRecording() {
    statement("You close your eyes and watch the recording...", clickToContinue = false)
}