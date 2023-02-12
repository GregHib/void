package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Laugh
import world.gregs.voidps.world.interact.dialogue.Suspicious
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "mubariz" && option == "Talk-to" }) { player: Player ->
    npc<Cheerful>("""
        Welcome to the Duel Arena!
        What can I do for you?
    """)
    menu()
}

suspend fun Interaction.menu() {
    val choice = choice("""
        What is this place?
        How do I challenge someone to a duel?
        What kind of options are there?
        Do you have any advice for me?
        I'll be off.
    """)
    when (choice) {
        1 -> place()
        2 -> duelling()
        3 -> options()
        4 -> advice()
        5 -> exit()
    }
}

suspend fun Interaction.place() {
    player("uncertain", "What is this place?")
    npc<Talking>("""
        The Duel Arena has six arenas where you can fight
        other players in a controlled environment. We have our
        own dedicated hospital where we guarantee to put you
        back together, even if you lose.
    """)
    npc<Talking>("""
        The Duel Arena has six arenas where you can fight
        other players in a controlled environment. We have our
        own dedicated hospital where we guarantee to put you
        back together, even if you lose.
    """)
    npc<Talking>("""
        In between the arenas are walkways where you can
        watch the fights and challenge other players.
    """)
    val choice = choice("""
        It looks really old. Where did it come from?
        How do I challenge someone to a duel?
        What kind of options are there?
        Do you have any advice for me?
        I'll be off.
    """)
    when (choice) {
        1 -> looksOld()
        2 -> duelling()
        3 -> options()
        4 -> advice()
        5 -> exit()
    }
}

suspend fun Interaction.looksOld() {
    player("uncertain", "It looks really old. Where did it come from?")
    npc<Talking>("""
        The archaeologists that are excavating the area east of
        Varrock have been working on this site as well. From
        these cliffs they uncovered this huge building. The
        experts think it may date back to the second age!
    """)
    npc<Talking>("""
        Now that the archaeologists have moved out, a group of
        warriors, headed by myself, have bought the land and
        converted it to a set of arenas for duels. The best
        fighters from around the world come here to fight!
    """)
    val choice = choice("""
        I challenge you!
        How do I challenge someone to a duel?
        What kind of options are there?
        Do you have any advice for me?
        I'll be off.
    """)
    when (choice) {
        1 -> challenge()
        2 -> duelling()
        3 -> options()
        4 -> advice()
        5 -> exit()
    }
}

suspend fun Interaction.challenge() {
    player("angry", "I challenge you!")
    npc<Laugh>("Ho! Ho! Ho!")
    menu()
}

suspend fun Interaction.duelling() {
    player("uncertain", "How do I challenge someone to a duel?")
    npc<Talking>("""
        When you go to the arena you'll go up an access ramp
        to the walkways that overlook the arenas. From the
        walkways you can watch the duels and challenge other
        players.
    """)
    npc<Talking>("""
        You'll know you're in the right place as you'll have a
        Duel-with option when you right-click a player.
    """)
    val choice = choice("""
        I challenge you!
        What is this place?
        What kind of options are there?
        Do you have any advice for me?
        I'll be off.
    """)
    when (choice) {
        1 -> challenge()
        2 -> place()
        3 -> options()
        4 -> advice()
        5 -> exit()
    }
}

suspend fun Interaction.options() {
    player("uncertain", "What kind of options are there?")
    npc<Talking>("""
        You and your opponent can offer coins or platinum as
        a stake. If you win, you receive what your opponent
        staked minus some tax, but if you lose, your opponent
        will get whatever items you staked.
    """)
    npc<Talking>("""
        You can choose to use rules to spice things up a bit.
        For instance if you both agree to use the 'No Magic'
        rule then neither player can use magic to attack the
        other player. The fight will be restricted to ranging and
    """)
    npc<Talking>("melee only.")
    npc<Talking>("""
        The rules are fairly self-evident with lots of different
        combinations for you to try out!
    """)
    val choice = choice("""
        What is this place?
        How do I challenge someone to a duel?
        Do you have any advice for me?
        I'll be off.
    """)
    when (choice) {
        1 -> place()
        2 -> duelling()
        3 -> advice()
        4 -> exit()
    }
}

suspend fun Interaction.advice() {
    player("unsure", "Do you have any advice for me?")
    npc<Laugh>("Win. And if you ever stop having fun, stop dueling.")
}

suspend fun Interaction.exit() {
    player("roll_eyes", "I'll be off.")
    npc<Suspicious>("See you in the arenas!")
}