package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.npc.shop.OpenShop

on<NPCOption>({ operate && npc.id == "aubury" && option == "Talk-to" }) { player: Player ->
    if (player["rune_mysteries", "unstarted"] == "research_notes") {
        checkNotes()
        return@on
    }
    npc<Cheerful>("Do you want to buy some runes?")
    choice {
        skillcapes()
        openShop()
        packageForYou()
        option<Unsure>(
            "Anything useful in that package I gave you?",
            { player["rune_mysteries", "unstarted"] == "package_delivered" }
        ) {
            npc<Cheerful>("Well, let's have a look...")
            researchPackage()
        }
        noThanks()
        teleport()
    }
}

fun PlayerChoice.openShop(): Unit = option<Cheerful>("Yes please!") {
    player.events.emit(OpenShop("auburys_rune_shop"))
}

suspend fun PlayerChoice.noThanks(message: String = "Oh, it's a rune shop. No thank you, then."): Unit = option<Talking>(message) {
    npc<Cheerful>("""
        Well, if you find someone who does want runes, please
        send them my way.
    """)
}

fun PlayerChoice.teleport(): Unit = option(
    "Can you teleport me to the Rune Essence?",
    { player["rune_mysteries", "unstarted"] == "completed" }
) {
}

suspend fun PlayerChoice.packageForYou(): Unit = option<Talking>(
    "I've been sent here with a package for you.",
    { player["rune_mysteries", "unstarted"] == "research_package" }
) {
    npc<Uncertain>("A package? From who?")
    player<Talking>("From Sedridor at the Wizards' Tower.")
    npc<Surprised>("""
        From Sedridor? But... surely, he can't have? Please, let
        me have it. It must be extremely important for him to
        have sent a stranger.
    """)
    if (player.hasBanked("research_package_rune_mysteries")) {
        player["rune_mysteries"] = "package_delivered"
        player.inventory.remove("research_package_rune_mysteries")
        item("You hand the package to Aubury.", "research_package_rune_mysteries", 600)
        npc<Cheerful>("Now, let's have a look...")
        researchPackage()
    } else {
        player<Uncertain>("""
                    Uh... yeah... about that... I kind of don't have it with
                    me...
                """)
        npc<Surprised>("""
                    What kind of person says they have a delivery for me,
                    but not with them? Honestly.
                """)
        npc<Talking>("Come back when you have it.")
    }
}

suspend fun PlayerContext.researchPackage() {
    item("Aubury goes through the package of research notes.", "research_package_rune_mysteries", 600)
    npc<Surprised>("This... this is incredible.")
    npc<Cheerful>("""
        My gratitude to you adventurer for bringing me these
        research notes. Thanks to you, I think we finally have
        it.
    """)
    player<Unsure>("You mean the incantation?")
    npc<Cheerful>("""
        Well when we combine my own research with this latest
        discovery, I think we might just...
    """)
    npc<Talking>("""
        No, no, I'm getting ahead of myself. The signs are
        promising, but let's not jump to any conclusions just
        yet.
    """)
    npc<Unsure>("""
        Here, take these notes back to Sedridor. They should
        hopefully give him everything he needs.
    """)
    if (player.inventory.isFull()) {
        item("""
            Aubury tries to hand you some research notes, but you
            don't have enough room to take them.
        """, "research_notes_rune_mysteries", 600)
        return
    }
    player["rune_mysteries"] = "research_notes"
    player.inventory.add("research_notes_rune_mysteries")
    item("Aubury hands you some research notes.", "research_notes_rune_mysteries", 600)
}

suspend fun PlayerContext.checkNotes() {
    npc<Unsure>("Hello. Did you take those notes back to Sedridor?")
    if (player.inventory.contains("research_notes_rune_mysteries")) {
        player<Talking>("I'm still working on it.")
        npc<Talking>("""
            Don't take too long. He'll be eager to see if this is
            indeed the breakthrough we were hoping for.
        """)
        npc<Unsure>("Now, did you want to buy some runes?")
        choice {
            openShop()
            noThanks("No thank you.")
        }
    } else {
        player<Sad>("Sorry, but I lost them.")
        npc<Talking>("""
            Well, luckily I have duplicates. It's a good thing they
            are written in code. I wouldn't want the wrong kind of
            person to get access to the information contained within.
        """)
        if (player.inventory.isFull()) {
            item("""
                Aubury tries to hand you some research notes, but you
                don't have enough room to take them.
            """, "research_notes_rune_mysteries", 600)
            return
        }
        if (player.bank.contains("research_notes_rune_mysteries")) {
            player.bank.remove("research_notes_rune_mysteries")
        }
        player.inventory.add("research_notes_rune_mysteries")
        item("Aubury hands you some research notes.", "research_notes_rune_mysteries", 600)
    }
}

suspend fun PlayerChoice.skillcapes(): Unit = option("Can you tell me about your cape?") {
    npc<Cheerful>("""
        Certainly! Skillcapes are a symbol of achievement. Only
        people who have mastered a skill and reached level 99
        can get their hands on them and gain the benefits they
        carry.
    """)
    npc<Talking>("""
        The Cape of Runecrafting has been upgraded with each
        talisman, allowing you to access all Runecrafting altars.
        Is there anything else I can help you with?
    """)
    choice {
        option<Cheerful>("I'd like to view your store please.") {
            player.events.emit(OpenShop("runecrafting_skillcape_skillcape"))
        }
        noThanks("No thank you.")
    }
}