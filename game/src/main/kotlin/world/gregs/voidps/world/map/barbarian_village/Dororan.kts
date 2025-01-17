package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

itemOnItem("chisel", "ring_from_jeffery") { player: Player ->
    if (player.quest("gunnars_ground") == "jeffery_ring") {
        player.message("Nothing interesting happens.")
    } else {
        player.softQueue("engraving") {
            item("dororans_engraved_ring", 400, "You engrave 'Gudrun the Fair, Gudrun the Fiery' onto the ring.")
            player.setAnimation("engrave")
            player.experience.add(Skill.Crafting, 125.0)
            player.inventory.replace("ring_from_jeffery", "dororans_engraved_ring")
            player["gunnars_ground"] = "engraved_ring"
        }
    }
}

npcOperate("Talk-to", "dororan") {
    when (player.quest("gunnars_ground")) {
        "started" -> started()
        "love_poem", "jeffery_ring" -> lovePoem()
        "engrave" -> {
            npc<Neutral>("Is it done? Have you created a work of magnificent beauty?")
            engraveMenu()
        }
        "engraved_ring" -> engravedRing()
        "show_gudrun" -> showGudrun()
        "meet_chieftain", "tell_gudrun" -> meetChieftain()
        "tell_dororan" -> aboutRing()
        "write_poem" -> writePoem()
        "more_poem" -> morePoem()
        "one_more_poem" -> oneMore()
        "poem_done" -> poemDone()
        "poem", "recital" -> poem()
        "completed" -> {
        }
        else -> unstarted()
    }
}

suspend fun SuspendableContext<Player>.poem() {
    if (!player.ownsItem("gunnars_ground")) {
        player<Sad>("Er, I lost the poem.")
        npc<Talk>("Luckily for you, I wrote a second draft.")
        if (player.inventory.isFull()) {
            statement("You don't have room for the poem. Speak to Dororan again when you have room.")
            return
        }
        player.inventory.add("gunnars_ground")
        player.setAnimation("pocket_item")
        item("gunnars_ground", 600, "Dororan gives you another poem.")
        npc<Talk>("Try not to lose this one.")
        return
    }
    npc<Quiz>("My poem is terrible, isn't it? The Chieftain will probably have me killed.")
    choice {
        option<Talk>("Everything will work out.")
        option<Talk>("I expect so.")
    }
}

suspend fun SuspendableContext<Player>.poemDone() {
    npc<Laugh>("At last! It's done! It's finished! My finest work! Thank you so much for your help!")
    player<Pleased>("Are you ready to present it to Chieftain?!")
    npc<Surprised>("What? No! I'm a writer, not a performer.")
    npc<Talk>("I think the Chieftain would respond best to one of his people. Perhaps you could ask Gudrun to recite it to hew father?")
    if (player.inventory.isFull()) {
        statement("You don't have room for the poem. Speak to Dororan again when you have room.")
        return
    }
    player["gunnars_ground"] = "poem"
    player.inventory.add("gunnars_ground")
    player.setAnimation("pocket_item")
    item("gunnars_ground", 400, "Dororan hands you the poem.")
    choice {
        option<Talk>("I'll get right on it.")
        option<Talk>("This had better be the last time.")
    }
}

suspend fun SuspendableContext<Player>.sweptToWar() {
    npc<Pleased>("'Who then, in face of madness, <blue>swept to war.</col>'")
    npc<Happy>("That's it! That's brilliant!")
    player["gunnars_ground"] = "poem_done"
    poemDone()
}

suspend fun SuspendableContext<Player>.oneMore() {
    npc<Pleased>("It's coming together. We're nearly done! One more to go!")
    npc<Quiz>("This one is tricky, though. It's a phrase I need. Someone did something.")
    phraseMenu()
}

suspend fun SuspendableContext<Player>.phraseMenu() {
    choice {
        option<Talk>("Threw the ball.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Ate a tasty pie.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Schemed intently.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Went for a walk.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.threeSyllablesMenu() {
    choice {
        option<Talk>("Picked a rose.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Made a raft.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Learned to soar.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Cleaned the floor.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option("More words") {
            threeSyllablesMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.threeSyllablesMenu2() {
    choice {
        option<Talk>("Heard a song.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Picked a flight.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Swept to war.") {
            sweptToWar()
        }
        option<Talk>("Tamed a shrew.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option("More words") {
            threeSyllablesMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.fightMenu() {
    choice {
        option<Talk>("Picked a fight.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Started a war.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Marched to battle.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Settled the score.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option("More words") {
            fightMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.fightMenu2() {
    choice {
        option<Talk>("Swept to war.") {
            sweptToWar()
        }
        option<Talk>("Loosed a mighty roar.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Initiated a battle.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Commenced fisticuffs.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option("More words") {
            fightMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.loreMenu() {
    choice {
        option<Talk>("Started a war.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Cleaned the floor.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Loosed a mighty roar.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Shut the door.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option("More words") {
            loreMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.loreMenu2() {
    choice {
        option<Talk>("Learned to soar") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Settled the score.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Swept to war.") {
            sweptToWar()
        }
        option<Talk>("Counted to flour.") {
            npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option("More words") {
            loreMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.threat() {
    npc<Pleased>("'But long is gone the author of that <blue>threat.</col>'")
    npc<Happy>("Perfect! Yes!")
    player["gunnars_ground"] = "one_more_poem"
    oneMore()
}

suspend fun SuspendableContext<Player>.morePoem() {
    npc<Sad>("The poem still isn't finished, though. I have another missing word. Give me another one; anything, to get me started.")
    choice {
        option<Talk>("Stockade.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option<Talk>("Longsword.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option<Talk>("Dungeoneering.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Grass.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.syllablePoemMenu() {
    choice {
        option<Talk>("Storm.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Wet.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option<Talk>("Hat.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Length.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option("More words.") {
            syllablePoemMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.syllablePoemMenu2() {
    choice {
        option<Talk>("Debt.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option<Talk>("Threat.") {
            threat()
        }
        option<Talk>("Axe.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Risk.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option("More words.") {
            syllablePoemMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.rhymePoemMenu() {
    choice {
        option<Talk>("Debt.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option<Talk>("Sweat.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option<Talk>("Upset.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option<Talk>("Brunette.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option("More words.") {
            rhymePoemMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.rhymePoemMenu2() {
    choice {
        option<Talk>("Threat.") {
            threat()
        }
        option<Talk>("Regret.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option<Talk>("Set.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option<Talk>("Wet.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'danger'.")
            dangerPoemMenu()
        }
        option("More words.") {
            rhymePoemMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.dangerPoemMenu() {
    choice {
        option<Talk>("Risk.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Crisis.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option<Talk>("Peril.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option<Talk>("Menace.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option("More words.") {
            dangerPoemMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.dangerPoemMenu2() {
    choice {
        option<Talk>("Upset.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllablePoemMenu()
        }
        option<Talk>("Storm.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Hazard.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
            rhymePoemMenu()
        }
        option<Talk>("Threat.") {
            threat()
        }
        option("More words.") {
            dangerPoemMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.aboutRing() {
    npc<Quiz>("Did you give Gudrun the ring? What did she think? Did it capture her heart?")
    player<Talk>("There's a problem.")
    npc<Cry>("It's because I'm a dwarf, isn't it? Or because I'm a poet? I knew it! I'm completely worthless!")
    choice {
        option<Talk>("No, she liked the ring.") {
            npc<Amazed>("Oh! Then what's the problem?")
            mostCruel()
        }
        option<Talk>("Would you be quiet for a moment?") {
            npc<Upset>("Sorry!")
            mostCruel()
        }
    }
}

suspend fun SuspendableContext<Player>.mostCruel() {
    player<Talk>("Gudrun's father won't let her be with someone from outside the village.")
    npc<Amazed>("Most cruel is fate! Most cruel! Why not?")
    player<Talk>("He's obsessed with the stories of his ancestors. He says his people are still at war.")
    npc<Upset>("This village has stood for a hundred years!")
    player<Talk>("I heard him arguing with one of the others. He says he honours his ancestors this way.")
    npc<Quiz>("Really? Interesting.")
    choice {
        option<Talk>("Do you know a lot about the village's history?") {
            npc<Talk>("Not really. I talked with Hunding, who guards this tower here.")
            anyIdea()
        }
        option<Talk>("What are we going to do?") {
            anyIdea()
        }
    }
}

suspend fun SuspendableContext<Player>.anyIdea() {
    npc<Amazed>("An idea occurs to me, but it is hubris of the greatest magnitude.")
    player<Quiz>("What is it?")
    npc<Talk>("What if I wrote a poem? Forged a sweeping, historical epic? Crafted a tale to touch the chieftain's soul?")
    player<Quiz>("Will that work?")
    npc<Pleased>("To win the heart of my beloved from her father's iron grasp? It is worth it just to try!")
    player.open("fade_out")
    delay(5)
    player.open("fade_in")
    delay(1)
    player["gunnars_ground"] = "write_poem"
    writePoem()
}

suspend fun SuspendableContext<Player>.writePoem() {
    npc<Talk>("'Even the bloodiest rose must settle.' Mixed metaphor. Whats settles? Detritus. That's hardly flattering.")
    npc<Talk>("'Even the rolliest boulder...'")
    player<Talk>("How is the poem going?")
    npc<Cry>("I'm stuck! I'm a worthless wordsmith! My work is pointless! My life is pointless!")
    choice {
        option<Talk>("I'm sure that's not true.") {
            stuckOnWord()
        }
        option<Talk>("What's the problem?") {
            stuckOnWord()
        }
    }
}

suspend fun SuspendableContext<Player>.stuckOnWord() {
    npc<Cry>("I'm stuck on a word. By the colossus of King Alvis! I can't find the words!")
    player<Talk>("Maybe I can help. What sort of word?")
    npc<Upset>("I don't know! I'm not some kind of word scientist. I just feel it out as I go.")
    npc<Talk>("Maybe you could suggest some words to get me started. Then I can tell you more.")
    player<Talk>("Alright, How about, uh...")
    poemMenu()
}

suspend fun SuspendableContext<Player>.poemMenu() {
    choice {
        option<Happy>("Cucumber.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Happy>("Monkey.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Happy>("Saradomin.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Barbarian.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.syllableMenu() {
    choice {
        option<Happy>("Ham.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Fey.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Happy>("Jaunt.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Grass.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option("More words") {
            syllableMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.syllableMenu2() {
    choice {
        option<Happy>("Roam.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Fish.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Stray.") {
            stray()
        }
        option<Happy>("Hay.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option("More words") {
            syllableMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.wordsMenu() {
    choice {
        option<Happy>("Deviate.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Happy>("Roam.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Veer.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Traipse.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option("More words") {
            wordsMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.wordsMenu2() {
    choice {
        option<Happy>("Meander.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Happy>("Astray.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Happy>("Jaunt.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Happy>("Stray.") {
            stray()
        }
        option("More words") {
            wordsMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.rhymeMenu() {
    choice {
        option<Happy>("Lay.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Happy>("Beret.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Happy>("May.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Happy>("Hay.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option("More words") {
            rhymeMenu2()
        }
    }
}

suspend fun SuspendableContext<Player>.rhymeMenu2() {
    choice {
        option<Happy>("Stray.") {
            stray()
        }
        option<Happy>("Dismay.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Happy>("Tray.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Happy>("Fey.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option("More words") {
            rhymeMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.stray() {
    npc<Talk>("'And from his righteous purpose never <blue>stray.</col>'")
    npc<Pleased>("That fits! It fits perfectly. Right meaning, right length, right rhyme. Well done!")
    player["gunnars_ground"] = "more_poem"
    morePoem()
}

suspend fun SuspendableContext<Player>.meetChieftain() {
    npc<Quiz>("Did you give Gudrun the ring? What did she think?")
    player<Talk>("She liked it, but there's a problem. I'm dealing with it.")
    npc<Amazed>("Oh no!")
}

suspend fun SuspendableContext<Player>.showGudrun() {
    if (!player.ownsItem("dororans_engraved_ring")) {
        npc<Surprised>("I know. I found it on the ground.")
        if (!giveRing()) {
            return
        }
        npc<Happy>("Please try not to lose it again. It's very precious.")
        return
    }
    npc<Talk>("Please take the ring to Gudrun for me.")
    choice {
        option<Talk>("Where is she?") {
            npc<Talk>("Inside the barbarian village.")
        }
        option<Talk>("I'm on it.")
    }
}

suspend fun SuspendableContext<Player>.engravedRing() {
    npc<Neutral>("Is it done? Have you created a work of magnificent beauty?")
    if (!player.ownsItem("dororans_engraved_ring")) {
        player<Sad>("I did engrave it. but I seem to have lost it.")
        npc<Happy>("Is this it? I found it on the ground. You've done a great job on it.")
        if (!giveRing()) {
            return
        }
        npc<Happy>("Please try not to lose it again! Now, will you do one more thing for me?")
        oneMoreThing()
        return
    }
    choice {
        option<Talk>("It's come out perfectly.") {
            item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
            npc<Happy>("You're right! It's perfect!")
            npc<Happy>("Will you do one more thing for me?")
            oneMoreThing()
        }
        option<Talk>("How does this look?") {
            item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
            npc<Happy>("Brilliant! That's perfect")
            npc<Happy>("Will you do one more thing for me?")
            oneMoreThing()
        }
        option<Talk>("It's a complete disaster.") {
            item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
            npc<Happy>("I don't know what you mean: it's perfect!")
            npc<Happy>("Will you do one more thing for me?")
            oneMoreThing()
        }
    }
}

suspend fun SuspendableContext<Player>.oneMoreThing() {
    choice {
        option<Talk>("Of course.") {
            veryWell()
        }
        option<Talk>("What now?") {
            veryWell()
        }
    }
}

suspend fun SuspendableContext<Player>.veryWell() {
    npc<Sad>("I fear she will only judge this poor book by its cover. Would you take the ring to Gudrun for me?")
    choice {
        option<Talk>("Very well.") {
            whereIsShe()
        }
        option<Talk>("I hope this is going somewhere.") {
            whereIsShe()
        }
    }
}

suspend fun SuspendableContext<Player>.whereIsShe() {
    npc<Talk>("Please don't tell her I'm a dwarf just yet.")
    player["gunnars_ground"] = "show_gudrun"
    choice {
        option<Talk>("Where is she?") {
            npc<Talk>("Inside the barbarian village.")
        }
        option<Talk>("I'm on it.")
    }
}

suspend fun SuspendableContext<Player>.unstarted() {
    npc<Upset>("'My heart with burdens heavy does it lie.'")
    npc<Upset>("'For never did I...'")
    npc<Upset>("Um...")
    choice {
        option<Happy>("'...ever learn to fly?'") {
            poet()
        }
        option<Happy>("'...eat redberry pie?'") {
            poet()
        }
        option<Happy>("'...get the evil eye?'") {
            poet()
        }
    }
}

suspend fun SuspendableContext<Player>.started() {
    npc<Talk>("I need a ring of purest gold. Then we can engrave it with the words of my heart.")
    player.refreshQuestJournal()
    npc<Pleased>("Oh! I know the perfect place to get a gold ring.")
    npc<Quiz>("Edgeville's metalsmith, jeffery, labours like myself under the weight of unrequited love.")
    npc<Pleased>("Perhaps, if you took one of my love poems to jeffery, he would trade it for a gold ring.")
    if (player.inventory.isFull()) {
        statement("You don't have room for the poem. Speak to Dororan again when you have room.")
        return
    }
    player["gunnars_ground"] = "love_poem"
    player.inventory.add("love_poem")
    player.setAnimation("pocket_item")
    item("love_poem", 600, "Dororan gives you a poem.")
    choice {
        option<Neutral>("I have some questions.") {
            npc<Pleased>("By all means.")
            lovePoemMenu()
        }
        option<Neutral>("I'll return with a ring from Jeffery.")
    }
}

suspend fun SuspendableContext<Player>.somethingElse() {
    choice {
        option<Neutral>("I want to ask about something else.") {
            npc<Pleased>("By all means.")
            lovePoemMenu()
        }
        option<Neutral>("I'll return with a ring from Jeffery.")
    }
}

suspend fun SuspendableContext<Player>.lovePoemMenu() {
    choice {
        option<Neutral>("Does it have to be a ring from Jeffery?") {
            npc<Talk>("Yes! Jeffery's rings are timeless works of incomparable romantic splendour.")
            somethingElse()
        }
        option<Neutral>("Where is Edgeville?") {
            npc<Sad>("North of here, beyond a ruined fortress. It used to be a bustling den of cutthroats but it's quite quiet these days.")
            somethingElse()
        }
        option<Neutral>("Why can't you go yourself?") {
            npc<Sad>("Some time ago, Jeffery asked me for advice in acting on his affections. I gave him the best advice that I could.")
            npc<Talk>("Things didn't work out very well for him. One thing let to another and now he no longer wishes to speak to me.")
            somethingElse()
        }
        option<Neutral>("Why can't you give a poem directly to Gudrun?") {
            npc<Talk>("These love poems are written in the Misthalinian style. A noble barbarian maiden would be insulted, not flattered.")
            somethingElse()
        }
        option<Neutral>("You want me to trick her into thinking you made the ring?") {
            npc<Talk>("Oh no, nothing like that! I have the words, I just need your help with the tools.")
            somethingElse()
        }
    }
}

suspend fun SuspendableContext<Player>.lovePoem() {
    npc<Neutral>("'I await in eagerness for a loop of lustrous grandeur.' No, that just sounds ridiculous. Have you brought me a ring from Jeffery?'")
    if (!player.ownsItem("ring_from_jeffery") && player.quest("gunnars_ground") == "jeffery_ring") {
        player<Happy>("I did get a ring from jeffery, but I seem to have lost it.")
        npc<Surprised>("How careless!")
        npc<Quiz>("Is it this one? I found it on the ground.")
        if (player.inventory.isFull()) {
            statement("You don't have room for the ring. Speak to Dororan again when you have room.")
            return
        }
        player.inventory.add("ring_from_jeffery")
        player.setAnimation("pocket_item")
        // player.playSound("") // TODO
        item("ring_from_jeffery", 600, "Dororan gives you back the ring.")
        engrave()
        return
    }
    if (player.holdsItem("ring_from_jeffery")) {
        player<Happy>("I have one right here.")
        item("ring_from_jeffery", 600, "You show Dororan the ring from Jeffery.")
        npc<Happy>("Thank you! That's exactly what I need!")
        engrave()
        return
    }
    choice {
        if (!player.ownsItem("love_poem") && player.quest("gunnars_ground") == "love_poem") {
            option<Neutral>("I lost the poem I was supposed to take to Jeffer.") {
                npc<Upset>("I'll give you another one.")
                if (player.inventory.isFull()) {
                    statement("You don't have room for the poem. Speak to Dororan again when you have room.")
                    return@option
                }
                player.inventory.add("love_poem")
                player.setAnimation("pocket_item")
                item("love_poem", 600, "Dororan gives you another poem.")
                npc<Talk>("Try to be more careful with this one.")
                return@option
            }
        }
        option<Neutral>("Where would I find one?") {
            npc<Talk>("Go north to Jeffery in Edgeville and trade the poem I gave you for a gold ring.")
            choice {
                option<Neutral>("I have some questions.") {
                    npc<Pleased>("By all means.")
                    lovePoemMenu()
                }
                option<Neutral>("I'll return with a ring from Jeffery.") {
                }
            }
        }
        option<Neutral>("I'll return with a ring from Jeffery.")
    }
}

suspend fun SuspendableContext<Player>.engrave() {
    npc<Talk>("Now, would you engrave something on it for me?")
    choice {
        option<Neutral>("What do you want me to engrave?") {
            engraveSomething()
        }
        option<Neutral>("It had better be something impressive.") {
            engraveSomething()
        }
    }
}

suspend fun SuspendableContext<Player>.engraveSomething() {
    npc<Pleased>("I've given this some thought.")
    npc<Happy>("'Gudrun the Fair, Gudrun the Fiery.'")
    choice {
        option<Neutral>("How do I engrave that?") {
            npc<Talk>("Just use a chisel on the gold ring.")
            player["gunnars_ground"] = "engrave"
            engraveMenu()
        }
        option<Neutral>("That sounds simple enough.") {
            npc<Talk>("Just use a chisel on the gold ring.")
            player["gunnars_ground"] = "engrave"
            engraveMenu()
        }
    }
}

suspend fun SuspendableContext<Player>.engraveMenu() {
    choice {
        option<Neutral>("Do you have a chisel I can use?") {
            haveChisel()
        }
        option<Neutral>("Isn't a chisel a bit clumsy for that?") {
            chiselBitClumsy()
        }
        option<Talk>("Not yet.")
    }
}

suspend fun SuspendableContext<Player>.haveChisel() {
    npc<Happy>("Yes, here you go.")
    if (player.inventory.isFull()) {
        statement("You don't have room for the chisel. Speak to Dororan again when you have room.")
    } else {
        player.inventory.add("chisel")
        player.setAnimation("pocket_item")
        item("chisel", 600, "Dororan gives you a chisel.")
    }
    choice {
        option<Neutral>("Isn't a chisel a bit clumsy for that?") {
            chiselBitClumsy()
        }
        option<Talk>("Okay.")
    }
}

suspend fun SuspendableContext<Player>.chiselBitClumsy() {
    npc<Happy>("I've seen jewelcrafters use them for all sorts of precise work.")
    choice {
        option<Neutral>("Do you have a chisel I can use?") {
            haveChisel()
        }
        option<Talk>("Okay.")
    }
}

suspend fun SuspendableContext<Player>.poet() {
    npc<Happy>("You're a poet too?")
    choice {
        option<Happy>("Yes.") {
            npc<Upset>("Ah! Then I'm sure you can identify with the arduous state of my life.")
            identify()
        }
        option<Talk>("Maybe a bit.") {
            npc<Upset>("Oh. Then maybe you can identify with the arduous state of my life.")
            identify()
        }
        option<Talk>("No.") {
            npc<Upset>("oh. How can I expect you to identify with the arduous state of my life?")
            identify()
        }
    }
}

suspend fun SuspendableContext<Player>.identify() {
    npc<Cry>("My heart is stricken with that most audacious of maladies!")
    choice {
        option<Neutral>("Angina?") {
            love()
        }
        option<Neutral>("Hypertension?") {
            love()
        }
        option<Neutral>("Coclearabsidosis?") {
            love()
        }
    }
}

suspend fun SuspendableContext<Player>.love() {
    npc<Amazed>("Love!")
    npc<Upset>("The walls of my heart are besieged by love's armies, and those walls begin to tumble!")
    npc<Upset>("In the barbarian village lives the fairest maiden I have witnessed in all my life.")
    choice {
        option<Neutral>("What's so special about her?") {
            npc<Pleased>("I wouldn't know where to start! Her fiery spirit? Her proud bearing? Her winsome form?")
            choice {
                option<Neutral>("But why is this making you sad?") {
                    getToThePoint()
                }
                option<Neutral>("What do you actually need?") {
                    getToThePoint()
                }
            }
        }
        option<Neutral>("Get to the point.") {
            getToThePoint()
        }
    }
}

suspend fun SuspendableContext<Player>.getToThePoint() {
    npc<Sad>("The people of this village value strength, stature and riches. I have none of these things.")
    npc<Upset>("My people are indomitable warriors, dripping with gold and precious gems, but not I.")
    npc<Sad>("I am not built for combat, and poetry has proven a life of poverty!")
    choice {
        option<Neutral>("There must be something you can do.") {
            helpMe()
        }
        option<Neutral>("Not to mention low stature.") {
            npc<Cry>("You see!")
            helpMe()
        }
    }
}

suspend fun SuspendableContext<Player>.helpMe() {
    npc<Quiz>("If Gudrun could ever love a dwarf, surely she would need to see my artisanry.")
    npc<Talk>("Will you help me? I am no crafter of metal.")
    if (player.levels.get(Skill.Crafting) < 5) {
        statement("You need a Crafting level of at least 5 to start this quest.")
    } else {
        choice("Start Gunnar's Ground quest?") {
            option("Yes.") {
                player["gunnars_ground"] = "started"
                started()
            }
            option("No.") {
            }
        }
    }
}

npcOperate("Talk-to", "dororan_after_quest") {
    if (!player.questComplete("gunnars_ground")) {
        return@npcOperate
    }
    if (player["dororan_ruby_bracelet", 0] != 1) {
        npc<Happy>("Come in, my friend, come in! There is another matter I could use your assistance with.")
    } else if (player["dororan_dragonstone_necklace", 0] != 1) {
        npc<Pleased>("I have another piece of jewellery to engrave.")
    } else if (player["dororan_onyx_amulet", 0] != 1) {
        npc<Pleased>("I have one last piece of jewellery to engrave.")
    } else {
        npc<Pleased>("Thanks so much for everything you've done for us!")
        npc<Pleased>("What can I do for you?")
    }
    if (player["dororan_ruby_bracelet", 0] != 1 || player["dororan_dragonstone_necklace", 0] != 1 || player["dororan_onyx_amulet", 0] != 1) {
        choice {
            if (player["dororan_ruby_bracelet", 0] != 1) {
                option<Neutral>("What is it?") {
                    npc<Pleased>("I have some more jewellery for Gudrun and I need your help to engrave them.")
                    choice {
                        option<Neutral>("What's the first piece?") {
                            npc<Pleased>("A magnificent ruby bracelet.")
                            npc<Happy>("'With beauty blessed.'")
                            choice {
                                option("Engrave the bracelet.") {
                                    if (player.levels.get(Skill.Crafting) < 72) {
                                        item("ruby_bracelet", 400, "you need a Crafting level of at least 42 to engrave the ruby bracelet.")
                                        npc<Sad>("That's a shame. Maybe you can try again another time.")
                                        return@option
                                    }
                                    player.setAnimation("engrave")
                                    player.experience.add(Skill.Crafting, 2000.0)
                                    player["dororan_ruby_bracelet"] = 1
                                    items( "chisel","ruby_bracelet","You carefully engrave 'With beauty blessed' onto the ruby bracelet.")
                                    npc<Happy>("Magnificent! Outstanding! I will give this to her immediately. Please, come back when you have time")
                                }
                                option("Don't engrave the bracelet.") {
                                    npc<Sad>("That's a shame. Maybe you can try again another time.")
                                }
                            }
                        }
                        option<Neutral>("I want to talk about something else.") {
                            npc<Pleased>("What can I do for you?")
                            someThingElse()
                        }
                        option<Neutral>("I don't have time right now.") {
                        }
                    }
                }
            } else if (player["dororan_dragonstone_necklace", 0] != 1) {
                option<Neutral>("What's this one?") {
                    npc<Pleased>("A fine dragonstone necklace.")
                    npc<Happy>("There's not much room...how about just 'Gudrun'?")
                    choice {
                        option("Engrave the necklace.") {
                            if (player.levels.get(Skill.Crafting) < 42) {
                                item("dragonstone_necklace", 400, "you need a Crafting level of at least 72 to engrave the dragonstone necklace.")
                                npc<Sad>("That's a shame. Maybe you can try again another time.")
                                return@option
                            }
                            player.setAnimation("engrave")
                            player.experience.add(Skill.Crafting, 10000.0)
                            player["dororan_dragonstone_necklace"] = 1
                            items( "chisel","dragonstone_necklace","You skillfully engrave 'Gudrun' onto the dragonstone necklace.")
                            npc<Happy>("Another astonishing piece of work! Please, come back later to see if I have other crafting tasks.")
                        }
                        option("Don't engrave the necklace.") {
                            npc<Sad>("That's a shame. Maybe you can try again another time.")
                        }
                    }
                }
            } else if (player["dororan_onyx_amulet", 0] != 1) {
                option<Neutral>("What is it?") {
                    npc<Pleased>("An onyx amulet!")
                    npc<Happy>("'The most beautiful girl in the room.'")
                    choice {
                        option("Engrave the amulet.") {
                            if (player.levels.get(Skill.Crafting) < 90) {
                                item("onyx_amulet", 400, "you need a Crafting level of at least 90 to engrave the onyx amulet.")
                                npc<Sad>("That's a shame. Maybe you can try again another time.")
                                return@option
                            }
                            player.setAnimation("engrave")
                            player.experience.add(Skill.Crafting, 20000.0)
                            player["dororan_onyx_amulet"] = 1
                            items( "chisel","onyx_amulet","You expertly engrave 'The most beautiful girl in the room' onto the onyx amulet.")
                            npc<Happy>("That's fantastic! Excellent work.")
                        }
                        option("Don't engrave the amulet.") {
                            npc<Sad>("That's a shame. Maybe you can try again another time.")
                        }
                    }
                }
            }
            option<Neutral>("I want to talk about something else.") {
                npc<Pleased>("What can I do for you?")
                someThingElse()
            }
            option<Neutral>("I don't have time right now.") {
            }
        }
    } else {
        someThingElse()
    }
}

suspend fun SuspendableContext<Player>.someThingElse() {
    choice {
        option<Neutral>("How are things?") {
            npc<Pleased>("Every morning I wake to sunshine and birdsong! Life is marvellous!")
            elseGoodbye()
        }
        option<Neutral>("This is a very large house.") {
            npc<Amazed>("I know! I don't know where Gunthor would have got such a thing. Maybe Gudrun has some idea.")
            elseGoodbye()
        }
        option<Neutral>("I'd like to see the poem you wrote for Gunthor.") {
            if (player.inventory.isFull()) {
                statement("You don't have room for the poem. Speak to Dororan again when you have room.")
                return@option
            }
            player.inventory.add("gunnars_ground")
            player.setAnimation("pocket_item")
            item("gunnars_ground", 600, "Dororan gives you a copy of the poem.")
            npc<Pleased>("There you go!")
        }
        if (!player.ownsItem("swanky_boots")) {
            option<Surprised>("I seem to have mislaid my swanky boots.") {
                npc<Happy>("Not to worry! There are some left. Here you go.")
                if (player.inventory.isFull()) {
                    statement("you don't have room for the boots.")
                    return@option
                }
                player.setAnimation("pocket_item")
                player.inventory.add("swanky_boots")
                item("swanky_boots", 600, "Dororan gives you some more boots.")
                npc<Happy>("Be more careful with these ones! I don't have an infinite supply.")
            }
        }
        option<Neutral>("Goodbye.") {
            npc<Happy>("Goodbye!")
        }
    }
}

suspend fun SuspendableContext<Player>.giveRing(): Boolean {
    if (player.inventory.isFull()) {
        statement("You don't have room for the ring. Speak to Dororan again when you have room.")
        return false
    }
    player.inventory.add("dororans_engraved_ring")
    player.setAnimation("pocket_item")
    item("dororans_engraved_ring", 400, "Dororan hands you back the engraved ring.")
    return true
}

suspend fun SuspendableContext<Player>.elseGoodbye() {
    choice {
        option<Neutral>("I want to talk about something else.") {
            npc<Pleased>("What can I do for you?")
            someThingElse()
        }
        option<Neutral>("Goodbye.") {
            npc<Happy>("Goodbye!")
        }
    }
}