package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "mubariz" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("cheerful", """
            Welcome to the Duel Arena!
            What can I do for you?
        """)
        menu()
    }
}

suspend fun DialogueContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc("unsure", followUp)
    }
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

suspend fun DialogueContext.place() {
    player("uncertain", "What is this place?")
    npc("talking", """
        The Duel Arena has six arenas where you can fight
        other players in a controlled environment. We have our
        own dedicated hospital where we guarantee to put you
        back together, even if you lose.
    """)
    npc("talking", """
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

suspend fun DialogueContext.looksOld() {
    player("uncertain", "It looks really old. Where did it come from?")
    npc("talking", """
        The archaeologists that are excavating the area east of
        Varrock have been working on this site as well. From
        these cliffs they uncovered this huge building. The
        experts think it may date back to the second age!
    """)
    npc("talking", """
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

suspend fun DialogueContext.challenge() {
    player("angry", "I challenge you!")
    npc("laugh", "Ho! Ho! Ho!")
    menu()
}

suspend fun DialogueContext.duelling() {
    player("uncertain", "How do I challenge someone to a duel?")
    npc("talking", """
        When you go to the arena you'll go up an access ramp
        to the walkways that overlook the arenas. From the
        walkways you can watch the duels and challenge other
        players.
    """)
    npc("talking", """
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

suspend fun DialogueContext.options() {
    player("uncertain", "What kind of options are there?")
    npc("talking", """
        You and your opponent can offer coins or platinum as
        a stake. If you win, you receive what your opponent
        staked minus some tax, but if you lose, your opponent
        will get whatever items you staked.
    """)
    npc("talking", """
        You can choose to use rules to spice things up a bit.
        For instance if you both agree to use the 'No Magic'
        rule then neither player can use magic to attack the
        other player. The fight will be restricted to ranging and
    """)
    npc("talking", "melee only.")
    npc("talking", """
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

suspend fun DialogueContext.advice() {
    player("unsure", "Do you have any advice for me?")
    npc("laugh", "Win. And if you ever stop having fun, stop dueling.")
}

suspend fun DialogueContext.exit() {
    player("roll_eyes", "I'll be off.")
    npc("suspicious", "See you in the arenas!")
}