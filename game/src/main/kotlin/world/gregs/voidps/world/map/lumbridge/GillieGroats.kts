package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && npc.id == "gillie_groats" && option == "Talk-to" }) { player: Player ->
    npc<Cheerful>("Hello, I'm Gillie the Milkmaid. What can I do for you?")
    if (player["cooks_assistant", "unstarted"] == "started" && !player.hasItem("top_quality_milk")) {
        val choice = choice("""
            I'm after some Top-quality milk.
            Who are you?
            Can you tell me how to milk a cow?
            I'm fine, thanks.
        """)
        when (choice) {
            1 -> topQualityMilk()
            2 -> whoAreYou()
            3 -> howToMilkCow()
            4 -> player<Cheerful>("I'm fine, thanks.")
        }
    } else {
        val choice = choice("""
        Who are you?
        Can you tell me how to milk a cow?
        I'm fine, thanks.
    """)
        when (choice) {
            1 -> whoAreYou()
            2 -> howToMilkCow()
            3 -> player<Cheerful>("I'm fine, thanks.")
        }
    }
}

suspend fun Interaction.whoAreYou() {
    npc<Cheerful>("""
        My name's Gillie Groats. My father is a farmer and I
        milk the cows for him.
    """)
    player<Unsure>("Do you have any buckets of milk spare?")
    npc<Cheerful>("""
        I'm afraid not. We need all of our milk to sell to
        market, but you can milk the cow yourself if you need
        milk.
    """)
    player<Unsure>("Thanks.")
}

suspend fun Interaction.howToMilkCow() {
    player<Unsure>("So how do you get milk from a cow then?")
    npc<Cheerful>("""
        It's very easy. First you need an empty bucket to hold
        the milk.
    """)
    npc<Cheerful>("""
        Then find a dairy cow to milk - you can't milk just
        any cow.
    """)
    player<Unsure>("How do I find a dairy cow?")
    npc<Cheerful>("""
        They are easy to spot - they are dark brown and
        white, unlike beef cows, which are light brown and white.
        We also tether them to a post to stop them wandering
        around all over the place.
    """)
    npc<Cheerful>("There are a couple very near, in this field.")
    npc<Cheerful>("""
        Then just milk the cow and your bucket will fill with
        tasty, nutritious milk.
    """)
}


suspend fun Interaction.topQualityMilk() {
    npc<Talk>("Really? Is it for something special?")
    player<Cheerful>("""
        Most certainly! It's for the cook to make a cake foe Duke
        Horacio!
    """)
    npc<Talk>("""
        Wow, it's quite an honour that you'd pick my cows. I'd
        suggest you get some milk from my prized cow.
    """)
    player<Unsure>("Which one's that?")
    npc<Talk>("""
        She's on the east side of the field, over by the cliff. Be
        gentle!
    """)
}

