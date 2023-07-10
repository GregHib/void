package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && npc.id == "millie_miller" && option == "Talk-to" }) { player: Player ->
    npc<Cheerful>("""
        Hello Adventurer. Welcome to Mill Lane Mill. Can I
        help you?
    """)
    menu()
}

suspend fun PlayerContext.menu() {
    choice {
        option("I'm looking for extra fine flour.", { player["cooks_assistant", "unstarted"] == "started" && !player.hasItem("extra_fine_flour") }) {
            npc<Unsure>("What's wrong with ordinary flour?")
            player<Talk>("""
                    Well, I'm no expert chef, but apparently it makes better
                    cakes. This cake, you see, is for Duke Horacio.
                """)
            npc<Cheerful>("""
                    Really? How marvellous! Well, I can sure help you out
                    there. Go ahead and use the mill and I'll realign the
                    millstones to produce extra fine flour. Anything else?
                """)
            player["cooks_assistant_talked_to_millie"] = 1
            choice {
                millFlour()
                option<Cheerful>("I'm fine, thanks.")
            }
        }
        whoAreYou()
        whatIsThisPlace()
        millFlour()
        option<Cheerful>("I'm fine, thanks.")
    }
}

suspend fun PlayerChoice.whoAreYou(): Unit = option<Unsure>("Who are you?") {
    npc<Cheerful>("""
        I'm Miss Millicent Miller the Miller of Mill Lane Mill.Our 
        family have been milling flour for generations.
    """)
    player<Unsure>("Don't you ever get fed up with flour?")
    npc<Talk>("""
        It's a good business to be in. People will always need
        flour.
    """)
    menu()
}

suspend fun PlayerChoice.whatIsThisPlace(): Unit = option<Unsure>("What is this place?") {
    npc<Cheerful>("""
        This is Mill Lane Mill. source of the finest flour in Gielinor, 
        and home to the Miller family for many generations
    """)
    npc<Cheerful>("We take wheat from the field nearby and mill into flour.")
    menu()
}

suspend fun PlayerChoice.millFlour(): Unit = option<Unsure>("How do I mill flour?") {
    npc<Cheerful>("""
        Making flour is pretty easy. First of all you need to get 
        some wheat. You can pick some from wheat fields. There 
        is one just outside the Mill, but there are many others 
        scattered across the world.
    """)
    npc<Cheerful>("""
        feel free to pick from our field! There always
        seems to be plenty of wheat there.
    """)
    player<Unsure>("Then I bring my wheat here?")
    npc<Cheerful>("""
        Yes, or one of the other mills in Gielinor. They all work
        the same way.
    """)
    npc<Cheerful>("""
        Just take your wheat up two levels to the top floor of the
        mill and place some into the hopper.
    """)
    npc<Cheerful>("""
        Then you need to start the grinding process by pulling the
        lever near the hopper. You add more wheat, but each
        time you add wheat you'll have to pull the hopper lever
        again.
    """)
    player<Unsure>("So where does the flour go then?")
    npc<Cheerful>("""
        The flour appears in this room here, you'll need an empty
        pot to put the flour into. One pot will hold the flour made
        by one load of wheat
    """)
    npc<Cheerful>("That's all there is to it and you'll have a pot of flour.")
    player<Cheerful>("Great! Thanks for your help.")
    menu()
}