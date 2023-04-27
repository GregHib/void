package world.gregs.voidps.world.map.varrock

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "gypsy_aris" && option == "Talk-to" }) { player: Player ->
    when (player["demon_slayer", "unstarted"]) {
        "unstarted" -> {
            npc<Talk>("Hello, young one.")
            npc<Talk>("""
                Cross my palm with silver and the future will be
                revealed to you.
            """)
            val choice = choice("""
                Okay, here you go.
                Who are you called 'young one'?
                No, I don't believe in that stuff.
                With silver?
            """)
            when (choice) {
                0 -> {
                    player<Talk>("Okay, here you go.")
                    npc<Talking>("""
                        Come closer and listen carefully to what the future
                        holds, as I peer into the swirling mists o the crystal
                        ball.
                    """)
                    npc<Talk>("I can see images forming. I can see you.")
                    npc<Unsure>("""
                        You are holding a very impressive-looking sword. I'm
                        sure I recognise it...
                    """)
                    npc<Unsure>("There is a big, dark shadow appearing now.")
                    npc<Afraid>("Aaargh!")
                    player<Unsure>("Are you all right?")
                    npc<Afraid>("It's Delrith! Delrith is coming!")
                    player<Uncertain>("Who's Delrith?")
                    npc<Sad>("Delrith...")
                    npc<Talk>("Delrith is a powerful demon.")
                    npc<Afraid>("""
                        Oh! I really hope he didn't see me looking at him
                        through my crystal ball!
                    """)
                    npc<Afraid>("""
                        He tried to destroy this city 150 years ago. He was
                        stopped just in time by the great hero Wally.
                    """)
                    npc<Afraid>("""
                        Using his magic sword Silverlight, Wally managed to
                        trap the demon in the stone circle just south
                        of this city.
                    """)
                    npc<Afraid>("""
                        Ye gods! Silverlight was the sword you were holding in
                        my vision! You are the one destined to stop the demon
                        this time.
                    """)
                    whatToDo()
                }
            }
        }
    }
}

suspend fun Interaction.whatToDo() {
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        Wally doesn't sound like a very heroic name.
    """)
    when (choice) {
        0 -> cityDestroyer()
        1 -> whereIsHe()
        2 -> notVeryHeroicName()
    }
}

suspend fun Interaction.howToDo() {
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        So, how did Wally kill Delrith?
    """)
    when (choice) {
        0 -> cityDestroyer()
        1 -> whereIsHe()
        2 -> howWallyWon()
    }
}

suspend fun Interaction.howWallyWon() {
    player<Talk>("So, how did Wally kill Delrith?")
    // Cutscene
    npc<Talk>("""
        Wally managed to arrive at the stone circle just as
        Delrith was summoned by a cult of chaos druids.
    """)
    npc<Angry>("wally", "Die, foul demon!")
    npc<Unsure>("wally", "Now, what was that incantation again?")
    npc<Angry>("wally", "Gabindo... Carlem... Camerinthum... Aber... Purchai!") // TODO Random order
    npc<Happy>("wally", "I am the greatest demon slayer E V E R!")
    npc<Talk>("""
        By reciting the correct magical incantation, and
        thrusting Silverlight into Delrith while he was newly
        summoned, Wally was able to imprison Delrith in the
        stone table at the centre of the circle.
    """)
    // End cutscene
    npc<Sad>("Delrith will come forth from the stone circle again.")
    npc<Sad>("""
        I would imagine an evil sorcerer is already beginning
        the rituals to summon Delrith as we speak.
    """)
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        What is the magical incantation?
        Where can I find Silverlight?
    """)
    when (choice) {
        0 -> cityDestroyer()
        1 -> whereIsHe()
        2 -> incantation()
        3 -> {
            player<Angry>("Where can I find Silverlight?")
            npc<Talk>("""
                Silverlight has been passed down by Wally's
                descendants. I believe it is currently in the care of one
                of the king's knights called Sir Prysin.
            """)
            npc<Happy>("""
                He shouldn't be too hard to find. He lives in the royal
                palace in this city. Tell him Gypsy Aris sent you.
            """)
            lastQuestions()
        }
    }
}

suspend fun Interaction.lastQuestions() {
    val choice = choice("""
        How am I meant to fight a demon who can destroy cities?
        Okay, where is he? I'll kill him for you.
        Wally doesn't sound like a very heroic name.
        What is the magical incantation?
        Okay, thanks I'll do my best to stop the demon.
    """)
    when (choice) {
        0 -> cityDestroyer()
        1 -> whereIsHe()
        2 -> notVeryHeroicName()
        3 -> incantation()
        4 -> {
            player<Angry>("Okay, thanks. I'll do my best to stop the demon.")
            npc<Happy>("Good luck, and may Guthix be with you!")
        }
    }
}

suspend fun Interaction.cityDestroyer() {
    player<Afraid>("""
        How am I meant to fight a demon who can destroy
        cities?
    """)
    npc<Talk>("""
        If you face Delrith while he is still weak from being
        summoned, and use the correct weapon, you will not
        find the task too arduous.
    """)
    npc<Talk>("""
        Do not fear. If you follow the path of the great hero
        Wally, then you are sure to defeat the demon.
    """)
    val choice = choice("""
        Okay, where is he? I'll kill him for you.
        Wally doesn't sound like a very heroic name.
        So how did Wally kill Delrith?
    """)
    when (choice) {
        0 -> whereIsHe()
        1 -> notVeryHeroicName()
        2 -> howWallyWon()
    }
}

suspend fun Interaction.whereIsHe() {
    player<Talk>("Okay, where is he? I'll kill him for you.")
    npc<Laugh>("Ah, the overconfidence of the young!")
    npc<Talk>("""
        Delrith can't be harmed by ordinary weapons. You
        must face him using the same weapon that Wally used.
    """)
    howToDo()
}

suspend fun Interaction.notVeryHeroicName() {
    player<Talk>("Wally doesn't sound like a very heroic name.")
    npc<Talk>("""
       Yes, I know. Maybe that is why history doesn't
       remember him. However, he was a great hero.
    """)
    npc<Talk>("""
       Who knows how much pain and suffering Delrith would
       have brought forth without Wally to stop him!
    """)
    npc<Talk>("It looks like you are needed to perform similar heroics.")
    howToDo()
}

suspend fun Interaction.incantation() {
    player<Talk>("What is the magical incantation?")
    npc<Talk>("Oh yes, let me think a second.")
    npc<Talking>("""
        Aright, I think I've got it now, it goes... Camerinthum...
        Aber... Purchai.,. Gabindo.,. Carlem. Have you got that?
    """)
    player<Talking>("I think so, yes.")
    lastQuestions()
}