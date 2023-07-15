package world.gregs.voidps.world.map.port_sarim

import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

on<NPCOption>({ operate && target.id == "thurgo" && option == "Talk-to" }) { player: Player ->
    when (player["the_knights_sword", "unstarted"]) {
        "started", "find_thurgo" -> menu()
        "happy_thurgo" -> menuSword()
        "picture", "cupboard" -> menuAboutSword()
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
        redberryPie()
        whatCape()
    }
}

suspend fun Interaction.madeSword() {
    player<Cheerful>("Thanks for making that sword for me!")
    npc<CheerfulOld>("You're welcome - thanks for the pie!")
}

suspend fun Interaction.replacementSword() {
    player<Cheerful>("Can you make that replacement sword now?")
    npc<UnsureOld>("How are you doing finding those sword materials?")
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
        npc("cheerful_old", "Just remember to call in with more pie some time!")
        return
    }
    if (player.inventory.contains("blurite_ore")) {
        player<Sad>("I don't have two iron bars.")
        npc<CheerfulOld>("Better go get some then, huh?")
        return
    }
    if (player.inventory.contains("iron_bar", 2)) {
        player<Sad>("I don't have any blurite ore yet.")
        npc("talking_old", """
            Better go get some then, huh? The only place I know
            to get it is under this cliff here, but it is guarded by a
            very powerful ice giant.
        """)
        return
    }
    player<Sad>("I don't have any of them yet.")
    npc<CheerfulOld>("""
        Well, I need a blurite ore and two iron bars. The only
        place I know to get blurite is under this cliff here, but
        it is guarded by a very powerful ice giant.
    """)
}

suspend fun Interaction.menuAboutSword() {
    choice {
        option("About that sword...") {
            aboutSword()
        }
        redberryPie()
        whatCape()
    }
}

suspend fun Interaction.menuSword() {
    choice {
        option("Can you make a special sword for me?") {
            specialSword()
        }
        redberryPie()
        whatCape()
    }
}

suspend fun Interaction.menu() {
    choice {
        option("Are you an Imcando dwarf? I need a special sword.") {
            imcandoDwarf()
        }
        redberryPie()
        whatCape()
    }
}

suspend fun Interaction.specialSword() {
    player<Cheerful>("Can you make a special sword for me?")
    npc("talking_old", """
        Well, after bringing me my favorite food I guess I
        should give it a go. What sort of sword is it?
    """)
    player<Talking>("""
        I need you to make a sword for one of Falador's
        knights. He had one which was passed down through five
        generations, but his squire has lost it.
    """)
    player<Unsure>("So we need an identical one to replace it.")
    npc<TalkingOld>("""
        A knight's sword eh? Well, I'd need to know exactly
        how it looked before I could make a new one.
    """)
    player["the_knights_sword"] = "picture"
    npc<TalkingOld>("""
        All the Faladian knights used to have swords with unique
        designs according to their position. Could you bring me
        a picture or something?
    """)
    player<Talking>("I'll go and ask his squire and see if I can find one.")
}

suspend fun Interaction.aboutSword() {
    player<Cheerful>("About that sword...")
    npc<UnsureOld>("Have you got a picture of the sword for me yet?")
    if (player.hasItem("portrait")) {
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
        npc("talking_old", """
            You'll need to get me some stuff to make this. I'll need
            two iron bars to make the sword, to start with. I'll also
            need an ore called blurite.
        """)
        npc<TalkingOld>("""
            Blurite is useless for making actual weapons, except
            crossbows, but I'll need some as decoration for the hilt.
        """)
        npc<TalkingOld>("""
            It is a fairly rare ore. The only place I know to get it
            is under this cliff here, but it is guarded by a very
            powerful ice giant.
        """)
        npc<TalkingOld>("""
            Most of the rocks in that cliff are pretty useless, and
            don't contain much of anything, but there's
            DEFINITELY some blurite in there.
        """)
        npc<TalkingOld>("""
            You'll need a little bit of mining experience to be able to
            find it.
        """)
        player<Talking>("Okay. I'll go and find them then.")
        return
    }
    player<Sad>("Sorry, not yet.")
    npc<TalkingOld>("Well, come back when you do.")
}

suspend fun Interaction.imcandoDwarf() {
    player<Cheerful>("Are you an Imcando dwarf? I need a special sword.")
    npc<AngryOld>("""
        I don't talk about that sort of thing anymore. 
        I'm getting old.
    """)
   choice {
       redberryPie()
       option<Sad>("I'll come back another time.")
   }
}

suspend fun PlayerChoice.redberryPie(): Unit = option<Unsure>(
    "Would you like a redberry pie?",
    { player.hasItem("redberry_pie") }
) {
    statement("You see Thurgo's eyes light up.")
    npc<CheerfulOld>("""
        I'd never say no to a redberry pie!
        We Imcando dwarves love them - they're GREAT!
    """)
    if (player["the_knights_sword", "unstarted"] == "find_thurgo") {
        player["the_knights_sword"] = "happy_thurgo"
    }
    player.inventory.remove("redberry_pie")
    statement("You hand over the pie Thurgo eats the pie. Thurgo pats his stomach.")
    npc<CheerfulOld>("""
        By Guthix! THAT was good pie! Anyone who makes pie
        like THAT has got to be alright!
    """)
}

suspend fun PlayerChoice.whatCape() = option("What is that cape you're wearing?") {
    thatCape()
}

suspend fun CharacterContext.thatCape() {
    player<Unsure>("What is that cape you're wearing?")
    npc<CheerfulOld>("""
        It's a Skillcape of Smithing. It shows that I'm a master
        blacksmith, but that's only to be expected - after all, my
        ancestors were the greatest blacksmiths in dwarven
        history.
    """)
    npc<CheerfulOld>("""
        If you ever achieve level 99 Smithing you'll be able to
        wear a cape like this, and receive more experience when
        smelting gold ore.
    """)
}

on<ItemOnNPC>({ operate && target.id == "thurgo" && item.id == "redberry_pie" }) { player: Player ->
    when (player["the_knights_sword", "unstarted"]) {
        "find_thurgo" -> menu()
        "happy_thurgo" -> menuSword()
        else -> player<Uncertain>("Why would I give him my pie?")
    }
}