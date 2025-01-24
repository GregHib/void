package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "gillie_groats") {
    npc<Happy>("Hello, I'm Gillie the Milkmaid. What can I do for you?")
    choice {
        option("I'm after some Top-quality milk.", { player.quest("cooks_assistant") == "started" && !player.holdsItem("top_quality_milk") }) {
            topQualityMilk()
        }
        option("Who are you?") {
            whoAreYou()
        }
        option("Can you tell me how to milk a cow?") {
            howToMilkCow()
        }
        option<Happy>("I'm fine, thanks.")
    }
}

suspend fun SuspendableContext<Player>.whoAreYou() {
    npc<Happy>("My name's Gillie Groats. My father is a farmer and I milk the cows for him.")
    player<Quiz>("Do you have any buckets of milk spare?")
    npc<Happy>("I'm afraid not. We need all of our milk to sell to market, but you can milk the cow yourself if you need milk.")
    player<Quiz>("Thanks.")
}

suspend fun SuspendableContext<Player>.howToMilkCow() {
    player<Quiz>("So how do you get milk from a cow then?")
    npc<Happy>("It's very easy. First you need an empty bucket to hold the milk.")
    npc<Happy>("Then find a dairy cow to milk - you can't milk just any cow.")
    player<Quiz>("How do I find a dairy cow?")
    npc<Happy>("They are easy to spot - they are dark brown and white, unlike beef cows, which are light brown and white. We also tether them to a post to stop them wandering around all over the place.")
    npc<Happy>("There are a couple very near, in this field.")
    npc<Happy>("Then just milk the cow and your bucket will fill with tasty, nutritious milk.")
}

suspend fun SuspendableContext<Player>.topQualityMilk() {
    npc<Talk>("Really? Is it for something special?")
    player<Happy>("Most certainly! It's for the cook to make a cake foe Duke Horacio!")
    npc<Talk>("Wow, it's quite an honour that you'd pick my cows. I'd suggest you get some milk from my prized cow.")
    player<Quiz>("Which one's that?")
    npc<Talk>("She's on the east side of the field, over by the cliff. Be gentle!")
}

