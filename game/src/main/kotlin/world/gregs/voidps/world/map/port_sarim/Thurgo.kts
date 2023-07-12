package world.gregs.voidps.world.map.port_sarim

import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.*
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

on<NPCOption>({ operate && npc.id == "thurgo" && option == "Talk-to" }) { player: Player ->
    when (player["the_knights_sword", "unstarted"]) {
        "started", "stage2" -> menu()
        "stage3" -> menuSword()
        "stage4", "stage5"  -> menuAboutSword()
        "stage6" -> menuReplacementSword()
        else -> thatCape()
    }
}

suspend fun Interaction.menuReplacementSword() {
    choice {
        if (player.hasItem("blurite_sword")) {
            option("Thanks for making that sword for me!") {
                madeSword()
            }
        } else {
            option("Can you make that replacement sword now?") {
                replacementSword()
            }
        }
        if (player.hasItem("redberry_pie")) {
            option("Would you like a redberry pie?") {
                redberryPie()
            }
        }
        option("What is that cape you're wearing?") {
            thatCape()
        }
    }
}

suspend fun Interaction.madeSword() {
    player<Cheerful>("Thanks for making that sword for me!")
    npc<Cheerful>("You're welcome - thanks for the pie!", largeHead = true)
}

suspend fun Interaction.replacementSword() {
    player<Cheerful>("Can you make that replacement sword now?")
    npc<Unsure>("How are you doing finding those sword materials?", largeHead = true)
    if (player.inventory.contains("blurite_ore" to 1, "iron_bar" to 2)) {
        player<Talking>("I have them right here.")

        player.inventory.remove("iron_bar", 2)
        player.inventory.remove("blurite_ore")
        player.inventory.add("blurite_sword")
        item("""
            You give the blurite ore and iron bars to Thurgo.
            Thurgo makes you a sword.
        """, "blurite_sword", 600)
        player<Cheerful>("Thank you very much!")
        npc<Cheerful>("Just remember to call in with more pie some time!", largeHead = true)
        return
    }
    if (player.inventory.contains("blurite_ore")) {
        player<Sad>("I don't have two iron bars.")
        npc<Talking>("Better go get some then, huh?", largeHead = true)
        return
    }
    if (player.inventory.contains("iron_bar", 2)) {
        player<Sad>("I don't have any blurite ore yet.")
        npc<Talking>("""
            Better go get some then, huh? The only place I know
            to get it is under this cliff here, but it is guarded by a
            very powerful ice giant.
        """, largeHead = true)
        return
    }
    player<Sad>("I don't have any of them yet.")
    npc<Talking>("""
        Well, I need a blurite ore and two iron bars. The only
        place I know to get blurite is under this cliff here, but
        it is guarded by a very powerful ice giant.
    """, largeHead = true)
}

suspend fun Interaction.menuAboutSword() {
    choice {
        option("About that sword...") {
            aboutSword()
        }
        if (player.hasItem("redberry_pie")) {
            option("Would you like a redberry pie?") {
                redberryPie()
            }
        }
        option("What is that cape you're wearing?") {
            thatCape()
        }
    }
}

suspend fun Interaction.menuSword() {
    choice {
        option("Can you make a special sword for me?") {
            specialSword()
        }
        if (player.hasItem("redberry_pie")) {
            option("Would you like a redberry pie?") {
                redberryPie()
            }
        }
        option("What is that cape you're wearing?") {
            thatCape()
        }
    }
}

suspend fun Interaction.menu() {
    choice {
        option("Are you an Imcando dwarf? I need a special sword.") {
            imcandoDwarf()
        }
        if (player["the_knights_sword", "unstarted"] == "stage2" && player.hasItem("redberry_pie")) {
            option("Would you like a redberry pie?") {
                redberryPie()
            }
        }
        option("What is that cape you're wearing?") {
            thatCape()
        }
    }
}

suspend fun Interaction.specialSword() {
    player<Cheerful>("Can you make a special sword for me?")
    npc<Talking>("""
        Well, after bringing me my favorite food I guess I
        should give it a go. What sort of sword is it?
    """, largeHead = true)
    player<Talking>("""
        I need you to make a sword for one of Falador's
        knights. He had one which was passed down through five
        generations, but his squire has lost it.
    """)
    player<Unsure>("So we need an identical one to replace it.")
    npc<Talking>("""
        A knight's sword eh? Well, I'd need to know exactly
        how it looked before I could make a new one.
    """, largeHead = true)
    player["the_knights_sword"] = "stage4"
    npc<Talking>("""
        All the Faladian knights used to have swords with unique
        designs according to their position. Could you bring me
        a picture or something?
    """, largeHead = true)
    player<Talking>("I'll go and ask his squire and see if I can find one.")
}

suspend fun Interaction.aboutSword() {
    player<Cheerful>("About that sword...")
    npc<Unsure>("Have you got a picture of the sword for me yet?", largeHead = true)
    if (player.hasItem("portrait")){
        player<Talking>("""
            I have found a picture of the sword I would like you to
            make.
        """)
        item("""
            You give the portrait to Thurgo.
            Thurgo studies the portrait.
        """, "portrait", 600)
        player["the_knights_sword"] = "stage6"
        player.inventory.remove("portrait")
        npc<Talking>("""
            You'll need to get me some stuff to make this. I'll need
            two iron bars to make the sword, to start with. I'll also
            need an ore called blurite.
        """, largeHead = true)
        npc<Talking>("""
            Blurite is useless for making actual weapons, except
            crossbows, but I'll need some as decoration for the hilt.
        """, largeHead = true)
        npc<Talking>("""
            It is a fairly rare ore. The only place I know to get it
            is under this cliff here, but it is guarded by a very
            powerful ice giant.
        """, largeHead = true)
        npc<Talking>("""
            Most of the rocks in that cliff are pretty useless, and
            don't contain much of anything, but there's
            DEFINITELY some blurite in there.
        """, largeHead = true)
        npc<Talking>("""
            You'll need a little bit of mining experience to be able to
            find it.
        """, largeHead = true)
        player<Talking>("Okay. I'll go and find them then.")
        return
    }
    player<Sad>("Sorry, not yet.")
    npc<Talking>("Well, come back when you do.", largeHead = true)
}

suspend fun Interaction.imcandoDwarf() {
    player<Cheerful>("Are you an Imcando dwarf? I need a special sword.")
    npc<Angry>("""
        I don't talk about that sort of thing anymore. 
        I'm getting old.
    """, largeHead = true)
    if (player["the_knights_sword", "unstarted"] == "stage2" && player.hasItem("redberry_pie")) {
        choice {
            option("Would you like a redberry pie?") {
                redberryPie()
            }
            option<Sad>("I'll come back another time.") {
            }
        }
    } else {
        player<Sad>("I'll come back another time.")
    }
}

suspend fun Interaction.redberryPie() {
    player<Unsure>("Would you like a redberry pie?")
    statement("You see Thurgo's eyes light up.")
    npc<Cheerful>("""
        I'd never say no to a redberry pie!
        We Imcando dwarves love them - they're GREAT!
    """, largeHead = true)
    if (player["the_knights_sword", "unstarted"] == "stage2") {
        player["the_knights_sword"] = "stage3"
    }
    player.inventory.remove("redberry_pie")
    statement("You hand over the pie Thurgo eats the pie. Thurgo pats his stomach.")
    npc<Cheerful>("""
        By Guthix! THAT was good pie! Anyone who makes pie
        like THAT has got to be alright!
    """, largeHead = true)
}

suspend fun Interaction.thatCape() {
    player<Unsure>("What is that cape you're wearing?")
    npc<Cheerful>("""
        It's a Skillcape of Smithing. It shows that I'm a master
        blacksmith, but that's only to be expected - after all, my
        ancestors were the greatest blacksmiths in dwarven
        history.
        """, largeHead = true)
    npc<Cheerful>("""
        If you ever achieve level 99 Smithing you'll be able to
        wear a cape like this, and receive more experience when
        smelting gold ore.
        """, largeHead = true)
}

on<ItemOnNPC>({ operate && npc.id == "thurgo" }) { player: Player ->
    if (item.id == "redberry_pie") {
        when (player["the_knights_sword", "unstarted"]) {
            "stage2" -> menu()
            "stage3" -> menuSword()
            else -> player<Uncertain>("Why would I give him my pie?")
        }
    }
}