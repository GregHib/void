package world.gregs.voidps.world.map.wizards_tower

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.inc
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playJingle
import world.gregs.voidps.world.interact.entity.sound.playSound

on<NPCOption>({ operate && npc.id == "sedridor" && option == "Talk-to" }) { player: Player ->
    when (player["rune_mysteries", "unstarted"]) {
        "unstarted" -> {
            npc<Cheerful>("""
                Welcome adventurer, to the world renowned Wizards'
                Tower, home to the Order of Wizards. How may I help
                you?
            """)
            player<Talking>("I'm just looking around.")
            npc<Uncertain>("""
                Well, take care adventurer. You stand on the ruins of
                the old destroyed Wizards' Tower. Strange and
                powerful magicks lurk here.
            """)
        }
        "started" -> started()
        "stage2" -> stage2()
        "stage3" -> stage3()
        "stage5" -> stage5()
        else -> completed()
    }
}

on<NPCOption>({ operate && npc.id == "sedridor" && option == "Teleport" }) { player: Player ->
    teleportEssenceMine()
}

suspend fun Interaction.started() {
    npc<Cheerful>("""
        Welcome adventurer, to the world renowned Wizards'
        Tower, home to the Order of Wizards. We are the
        oldest and most prestigious group of wizards around.
        Now, how may I help you?
    """)
    player<Unsure>("Are you Sedridor?")
    npc<Unsure>("Sedridor? What is it you want with him?")
    player<Talking>("""
        The Duke of Lumbridge sent me to find him. I have
        this talisman he found. He said Sedridor would be
        interested in it.
    """)
    npc<Talking>("""
        Did he now? Well hand it over then, and we'll see what
        all the hubbub is about.
    """)
    var choice = choice("""
        Okay, here you are.
        No, I'll only give it to Sedridor.
    """)
    when (choice) {
        1 -> okHere()
        2 -> {
            player<Uncertain>("No, I'll only give it to Sedridor.")
            npc<Cheerful>("""
                Well good news, for I am Sedridor! Now, hand it over
                and let me have a proper look at it, hmm?
            """)
            choice = choice("""
                Okay, here you are.
                No, I don't think you are Sedridor.
            """)
            when (choice) {
                1 -> okHere()
                2 -> {
                    player<Uncertain>("No, I don't think you are Sedridor.")
                    npc<Cheerful>("""
                        Hmm... Well, I admire your caution adventurer.
                        Perhaps I can prove myself? I will use my mental
                        powers to discover...
                    """)
                    npc<Cheerful>("Your name is... ${player.name}!")
                    player<Surprised>("You're right! How did you know that?")
                    npc<Cheerful>("""
                        Well I am the Archmage you know! You don't get to
                        my position without learning a few tricks along the way!
                    """)
                    npc<Unsure>("""
                        So now that I have proved myself to you, why don't
                        you hand over that talisman, hmm?
                    """)
                    okHere()
                }
            }
        }
    }
}

suspend fun Interaction.okHere() {
    player<Talking>("Okay, here you are.")
    if (player.inventory.contains("air_talisman")) {
        player["rune_mysteries"] = "stage2"
        item("You hand the talisman to Sedridor.", "air_talisman", 600)
        player.inventory.remove("air_talisman")
        npc<Uncertain>("""
            Hmm... Doesn't seem to be anything too special. Just a
            normal air talisman by the looks of things. Still, looks
            can be deceiving. Let me take a closer look...
        """)
        player.playSound("enchant_emerald_ring")
        item("""
            Sedridor murmurs some sort of incantation and the
            talisman glows slightly.
        """, "air_talisman", 600)
        npc<Uncertain>("""
            How interesting... It would appear I spoke too soon.
           There's more to this talisman than meets the eye. In
           fact, it may well be the last piece of the puzzle.
        """)
        player<Unsure>("Puzzle?")
        npc<Cheerful>("""
            Indeed! The lost legacy of the first tower. This talisman
            may in fact be key to finding the forgotten essence
            mine!
        """)
        player<Uncertain>("""
            First tower? Forgotten essence mine? What are you on
            about?
        """)
        npc<Cheerful>("Ah, my apologies, adventurer. Allow me to fill you in.")
        val choice = choice("""
            Go ahead.
            Actually, I'm not interested.
        """)
        when (choice) {
            1 -> {
                player<Talking>("Go ahead.")
                npc<Cheerful>("""
                    As you are likely aware, when we cast spells, we do so
                    using the power of runes.
                """)
                npc<Cheerful>("""
                    These runes are crafted from a highly unique material,
                    and then imbued with magical power from various runic
                    altars. Different altars create different runes with
                    different magical effects.
                """)
                npc<Cheerful>("""
                    The process of imbuing runes is called runecrafting.
                    Legend has it that this was once a common art, but the
                    secrets of how to do it were lost until just under two
                    hundred years ago.
                """)
                npc<Cheerful>("""
                    The rediscovery of runecrafting had such a large
                    impact on the world, that it marked the dawn of the
                    Fifth Age. It also resulted in the birth of our order, and
                    the construction of the first Wizards' Tower.
                """)
                player<Unsure>("""
                    If it was the first tower, I'm guessing it doesn't exist
                    anymore? What happened?
                """)
                npc<Angry>("""
                    It was burnt down by traitorous members of our own
                    order. They followed the evil god of chaos, Zamorak,
                    and they wished to claim our magical discoveries in his
                    name.
                """)
                npc<Sad>("""
                    When the tower burnt down, much was lost, including
                    an important incantation. A spell that could be used to
                    teleport to a hidden essence mine.
                """)
                player<Unsure>("The essence mine you mentioned earlier, I assume?")
                npc<Talking>("""
                    Precicely. Rune essence is the material used to make
                    runes, but it is incredibly rare. That essence mine was
                    the only place it could be found that our order knew
                    of.
                """)
                npc<Sad>("""
                    Since the incantation was lost, we have struggled to
                    maintain our stocks of rune essence.
                """)
                npc<Talking>("""
                    There are seemingly those out there that still know
                    where to find some, but while they have been willing to
                    sell essence to us, they have refused to share knowledge
                    on how to find it ourselves.
                """)
                player<Unsure>("""
                    I'm starting to see why this is so important. So you
                    think this talisman can help you rediscover that
                    incantation?
                """)
                npc<Cheerful>("""
                    I do! All magic leaves traces, and from what I can tell,
                    this talisman was used heavily during the time of the
                    first tower.
                """)
                npc<Cheerful>("""
                    It would have been taken to the essence mine many
                    times, and the magical energies there will have left an
                    imprint on it. To think that it was hidden in Lumbridge
                    all this time!
                """)
                player<Unsure>("So what happens now?")
                npc<Cheerful>("""
                    It is critical I share this discovery with my associate,
                    Aubury, as soon as possible. He's not much of a wizard,
                    but he's an expert on runecrafting, and his insight will
                    be essential.
                """)
                discovery()
            }
            2 -> {
                player<Talking>("Actually, I'm not interested.")
                npc<Sad>("""
                    Oh... Well I guess the short of it is that this talisman
                    could be key to helping us rediscover an important
                    teleportation incantation.
                """)
                npc<Talking>("""
                    With it, we'll be able to access a hidden essence mine,
                    our lost source of rune essence.
                """)
                discovery()
            }
        }
    } else {
        npc<Talk>("...")
        player<Talk>("...")
        npc<Uncertain>("Well?")
        player<Uncertain>("I don't seem to have it with me.")
        npc<Uncertain>("""
            Hmm? You are a very odd person. Come back again
            when you have it.
        """)
    }
}

suspend fun Interaction.discovery() {
    npc<Cheerful>("""
        It is critical I share this discovery with my associate,
        Aubury, as soon as possible. He's not much of a wizard,
        but he's an expert on runecrafting, and his insight will
        be essential.
    """)
    npc<Unsure>("""
        Would you be willing to visit him for me? I would go
        myself, but I wish to study this talisman some more.
    """)
    val choice = choice("""
        Yes, certainly.
        No, I'm busy.
    """)
    when (choice) {
        1 -> yesCertainly()
        2 -> imBusy()
    }
}

suspend fun Interaction.stage2() {
    npc<Unsure>("""
        Hello again, adventurer. You have already done so
        much, but I would really appreciate it if you were to
        visit my associate, Aubury. Would you be willing to?
    """)
    val choice = choice("""
        Yes, certainly.
        No, I'm busy.
    """)
    when (choice) {
        1 -> yesCertainly()
        2 -> imBusy()
    }
}

suspend fun Interaction.stage3() {
    npc<Unsure>("""
        Hello again, adventurer. Did you take that package to
        Aubury?
    """)
    if (player.hasBanked("research_package_rune_mysteries")) {
        player<Talking>("Not yet.")
        npc<Talking>("""
            He runs a rune shop in the south east of Varrock.
            Please deliver it to him soon.
        """)
    } else {
        player<Sad>("I lost it. Could I have another?")
        npc<Talking>("Well it's a good job I have copies of everything.")
        if (player.inventory.isFull()) {
            item("""
                Sedridor tries to hand you a package, but you don't
                have enough room to take it.
            """, "research_package_rune_mysteries", 600)
            return
        }
        if (player.bank.contains("research_package_rune_mysteries")) {
            player.bank.remove("research_package_rune_mysteries")
        }
        player.inventory.add("research_package_rune_mysteries")
        item("Sedridor hands you a package.", "research_package_rune_mysteries", 600)
        npc<Cheerful>("Best of luck, ${player.name}.")
    }
}

suspend fun Interaction.stage5() {
    npc<Talking>("""
        Ah, ${player.name}. How goes your quest? Have you delivered
        my research to Aubury yet?
    """)
    player<Talking>("Yes, I have. He gave me some notes to give to you.")
    npc<Cheerful>("Wonderful! Let's have a look then.")
    if (player.hasItem("research_notes_rune_mysteries")) {
        item("You hand the notes to Sedridor.", "research_notes_rune_mysteries", 600)
        npc<Cheerful>("Alright, let's see what Aubury has for us...")
        npc<Surprised>("Yes, this is it! The lost incantation!")
        player<Unsure>("So you'll be able to access that essence mine now?")
        npc<Cheerful>("""
            That's right! Because of you, our order finally has a
            proper source of rune essence again! Thank you,
            friend.
        """)
        npc<Cheerful>("""
            If you ever want to access the essence mine yourself,
            just let me know. It's the least I can do.
        """)
        npc<Cheerful>("""
            I will also share the incantation with others, including
            Aubury. When I do, I'll let them know that you are to
            be given unlimited access to the mine.
        """)
        npc<Cheerful>("""
            Oh, and you can have this air talisman back as well. I
            have no further need of it, and I'm sure you will find
            it useful.
        """)
        npc<Cheerful>("""
            In case you didn't know, the talisman can be used to
            craft air runes. Just take it to the Air Altar south of
            Falador along with some rune essence.
        """)
        npc<Cheerful>("""
            Don't worry if you can't find the altar. The talisman
            can guide you there. You may find talismans for other
            altars as well while adventuring. They'll let you craft
            other types of rune.
        """)
        player<Cheerful>("Great! Thanks!")
        npc<Cheerful>("My pleasure!")
        player.inventory.remove("research_notes_rune_mysteries")
        questComplete()
    } else {
        player<Uncertain>("Err, you're not going to believe this...")
        npc<Uncertain>("What?")
        player<Uncertain>("I don't have them.")
        npc<Uncertain>("""
            Right... You're rather careless aren't you. I suggest
            you go and speak to Aubury once more. With luck he
            will have made copies.
        """)
    }
}

suspend fun Interaction.imBusy() {
    player<Talking>("No, I'm busy.")
    npc<Talking>("""
        As you wish adventurer. I will continue to study this
        talisman you have brought me. Return here if you find
        yourself with some spare time to help me.
    """)
}

suspend fun Interaction.yesCertainly() {
    player<Talking>("Yes, certainly.")
    player["rune_mysteries"] = "stage3"
    npc<Cheerful>("""
        He runs a rune shop in the south east of Varrock.
        Please, take this package of research notes to him. If all
        goes well, the secrets of the essence mine may soon be
        ours once more!
    """)
    if (player.inventory.isFull()) {
        item("""
            Sedridor tries to hand you a package, but you don't
            have enough room to take it.
        """, "research_package_rune_mysteries", 600)
        return
    }
    player.inventory.add("research_package_rune_mysteries")
    item("Sedridor hands you a package.", "research_package_rune_mysteries", 600)
    npc<Cheerful>("Best of luck, ${player.name}.")
}

suspend fun Interaction.completed() {
    player<Talking>("Hello there.")
    npc<Cheerful>("Hello again, ${player.name}. What can I do for you?")
    val choice = choice("""
        Can you teleport me to the Rune Essence Mine?
        Who else knows the teleport to the Rune Essence Mine?
        Could you tell me about the old Wizards' Tower?
        Nothing thanks, I'm just looking around.
    """)
    when (choice) {
        1 -> {
            player<Unsure>("Can you teleport me to the Rune Essence Mine?")
            teleportEssenceMine()
        }
        2 -> whoElseKnows()
        3 -> oldWizardsTower()
        4 -> {
            player<Talking>("Nothing thanks, I'm just looking around.")
            npc<Cheerful>("""
                Well, take care. You stand on the ruins of the old
                destroyed Wizards' Tower. Strange and powerful
                magicks lurk here.
            """)
        }
    }
}

suspend fun Interaction.teleportEssenceMine() {
    //npc.forceChat = "Seventior disthiae molenko!"
    //2910 4830
}

suspend fun Interaction.whoElseKnows() {
    player<Unsure>("""
        Who else knows the teleport to the Rune Essence
        Mine?
    """)
    npc<Cheerful>("""
        Apart from myself, there's also Aubury in Varrock,
        Wizard Cromperty in East Ardougne, Brimstail in the
        Tree Gnome Stronghold and Wizard Distentor in
        Yanille's Wizards' Guild.
    """)
    val choice = choice("""
        Can you teleport me to the Rune Essence Mine?
        Could you tell me about the old Wizards' Tower?
        Thanks for the information.
    """)
    when (choice) {
        1 -> {
            player<Unsure>("Can you teleport me to the Rune Essence Mine?")
            teleportEssenceMine()
        }
        2 -> oldWizardsTower()
        3 -> thanksForInformation()
    }
}

suspend fun Interaction.oldWizardsTower() {
    player<Unsure>("Could you tell me about the old Wizards' Tower?")
    npc<Cheerful>("""
        Of course. The first Wizards' Tower was built at the
        same time the Order of Wizards was founded. It was
        at the dawn of the Fifth Age, when the secrets of
        runecrafting were rediscovered.
    """)
    npc<Cheerful>("""
        For years, the tower was a hub of magical research.
        Wizards of all races and religions were welcomed into
        our order.
    """)
    npc<Sad>("""
        Alas, that openness is what ultimately led to disaster.
        The wizards who served Zamorak, the evil god of chaos,
        tried to claim our magical discoveries in his name.
    """)
    npc<Sad>("""
        They failed, but in retaliation, they burnt the entire
        tower to the ground. Years of work was destroyed.
    """)
    npc<Talking>("""
        The tower was soon rebuilt of course, but even now we
        are still trying to regain knowledge that was lost.
    """)
    npc<Talking>("""
        That's why I spend my time down here, in fact. This
        basement is all that is left of the old tower, and I believe
        there are still some secrets to discover here.
    """)
    npc<Cheerful>("""
        Of course, one secret I am no longer looking for is the
        teleportation incantation to the Rune Essence Mine.
        We have you to thank for that.
    """)
    val choice = choice("""
        Can you teleport me to the Rune Essence Mine?
        Who else knows the teleport to the Rune Essence Mine?
        Thanks for the information.
    """)
    when (choice) {
        1 -> {
            player<Unsure>("Can you teleport me to the Rune Essence Mine?")
            teleportEssenceMine()
        }
        2 -> whoElseKnows()
        3 -> thanksForInformation()
    }
}

suspend fun Interaction.thanksForInformation() {
    player<Cheerful>("Thanks for the information.")
    npc<Cheerful>("My pleasure.")
}

fun Interaction.questComplete() {
    player["rune_mysteries"] = "completed"
    player.playJingle("quest_complete_1")
    if (player.inventory.isFull()) {
        player.bank.add("air_talisman")
        player.message("The air talisman has been sent to your bank.")
    } else {
        player.inventory.add("air_talisman")
    }
    player.inc("quest_points")
    player.message("Congratulations, you've completed a quest: <col=081190>Rune Mysteries</col>")
    player.refreshQuestJournal()
    val lines = listOf(
        "1 Quest Point",
        "An Air Talisman",
        "Rune Essence Mine Access"
    )
    player.sendQuestComplete("Rune Mysteries", lines, Item("air_talisman"))
}