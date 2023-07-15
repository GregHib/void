package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Laugh
import world.gregs.voidps.world.interact.dialogue.Suspicious
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && target.id == "reldo" && option == "Talk-to" }) { player: Player ->
    npc<Talking>("Hello stranger.")
    choice {
        option("Do you have anything to trade?") {
            anythingToTrade()
        }
        option("What do you do?") {
            whatDoYouDo()
        }
        if (player["the_knights_sword", "unstarted"] == "started" || player["the_knights_sword", "unstarted"] == "stage2") {
            option("What do you know about the Imcando dwarves?") {
                aboutImcandoDwarves()
            }
        }
    }
}

suspend fun Interaction.anythingToTrade() {
    player<Unsure>("Do you have anything to trade?")
    npc<Talking>("Only knowledge.")
    player<Unsure>("How much do you want for that then?")
    npc<Laugh>("""
        No, sorry, that was just my little joke. I'm not the 
	    trading type.
    """)
    player<Talking>("Ah well.")
}

suspend fun Interaction.whatDoYouDo() {
    player<Unsure>("What do you do?")
    npc<Talking>("I am the palace librarian.")
    player<Talking>("Ah. That's why you're in the library then.")
    npc<Talking>("Yes.")
    npc<Talking>("""
        Although I would probably be in here even if I didn't
        work here. I like reading. Someday I hope to catalogue 
        all of the information stored in these books so all may
         read it.
    """)
}

suspend fun Interaction.aboutImcandoDwarves() {
    player<Unsure>("What do you know about the Imcando dwarves?")
    npc<Talking>("The Imcando dwarves, you say?")
    npc<Talking>("""
        Ah yes... for many hundreds of years they were the 
        world's most skilled smiths. They used secret smithing 
        knowledge passed down from generation to generation.
    """)
    npc<Talking>("""
        Unfortunately, about a century ago, the once thriving
        race was wiped out during the barbarian invasions of 
        that time.
    """)
    player<Unsure>("So are there any Imcando left at all?")
    npc<Talking>("""
        I believe a few of them survived, but with the bulk of 
        their population destroyed their numbers have dwindled 
        even further.
    """)
    npc<Talking>("""
        They tend to keep to themselves, and they tend not to 
        tell people they're descendants of the Imcando, which is 
        why people think the tribe is extinct. However...
    """)
    if (player["the_knights_sword", "unstarted"] == "started") {
        player["the_knights_sword"] = "stage2"
    }
    npc<Suspicious>("""
        ... you could try taking them some redberry pie. They 
        REALLY like redberry pie. I believe I remember a 
        couple living in Asgarnia near the cliffs on the 
        Asgarnian southern peninsula.
    """)
}