package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "mubariz") {
    npc<Happy>("""
        Welcome to the Duel Arena!
        What can I do for you?
    """)
    menu()
}

suspend fun CharacterContext<Player>.menu() {
    choice {
        place()
        duelling()
        options()
        advice()
        exit()
    }
}

suspend fun PlayerChoice.place(): Unit = option<Uncertain>("What is this place?") {
    npc<Neutral>("The Duel Arena has six arenas where you can fight other players in a controlled environment. We have our own dedicated hospital where we guarantee to put you back together, even if you lose.")
    npc<Neutral>("The Duel Arena has six arenas where you can fight other players in a controlled environment. We have our own dedicated hospital where we guarantee to put you back together, even if you lose.")
    npc<Neutral>("In between the arenas are walkways where you can watch the fights and challenge other players.")
    choice {
        looksOld()
        duelling()
        options()
        advice()
        exit()
    }
}

suspend fun CharacterContext<Player>.looksOld() {
    player<Uncertain>("It looks really old. Where did it come from?")
    npc<Neutral>("The archaeologists that are excavating the area east of Varrock have been working on this site as well. From these cliffs they uncovered this huge building. The experts think it may date back to the second age!")
    npc<Neutral>("Now that the archaeologists have moved out, a group of warriors, headed by myself, have bought the land and converted it to a set of arenas for duels. The best fighters from around the world come here to fight!")
    choice {
        challenge()
        duelling()
        options()
        advice()
        exit()
    }
}

suspend fun PlayerChoice.challenge(): Unit = option<Frustrated>("I challenge you!") {
    npc<Laugh>("Ho! Ho! Ho!")
    menu()
}

suspend fun CharacterContext<Player>.duelling() {
    player<Uncertain>("How do I challenge someone to a duel?")
    npc<Neutral>("When you go to the arena you'll go up an access ramp to the walkways that overlook the arenas. From the walkways you can watch the duels and challenge other players.")
    npc<Neutral>("You'll know you're in the right place as you'll have a Duel-with option when you right-click a player.")
    choice {
        challenge()
        place()
        options()
        advice()
        exit()
    }
}

suspend fun PlayerChoice.options(): Unit = option<Uncertain>("What kind of options are there?") {
    npc<Neutral>("You and your opponent can offer coins or platinum as a stake. If you win, you receive what your opponent staked minus some tax, but if you lose, your opponent will get whatever items you staked.")
    npc<Neutral>("You can choose to use rules to spice things up a bit. For instance if you both agree to use the 'No Magic' rule then neither player can use magic to attack the other player. The fight will be restricted to ranging and")
    npc<Neutral>("melee only.")
    npc<Neutral>("The rules are fairly self-evident with lots of different combinations for you to try out!")
    choice {
        place()
        duelling()
        advice()
        exit()
    }
}

suspend fun PlayerChoice.advice(): Unit = option<Quiz>("Do you have any advice for me?") {
    npc<Chuckle>("Win. And if you ever stop having fun, stop dueling.")
}

suspend fun PlayerChoice.exit(): Unit = option<RollEyes>("I'll be off.") {
    npc<Shifty>("See you in the arenas!")
}
