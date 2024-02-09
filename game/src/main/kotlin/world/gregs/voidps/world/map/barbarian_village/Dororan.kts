package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*


itemOnItem("chisel", "ring_from_jeffery") { player: Player ->
    if (player.quest("gunnars_ground") == "jeffery_ring") {
        player.message("Nothing interesting happens.")
    } else {
        // item("dororans_engraved_ring", 400, "You engrave 'Gudrun the Fair, Gudrun the Fiery' onto the ring.")//todo fix
        player.setAnimation("engrave")
        player.experience.add(Skill.Crafting, 125.0)
        player.inventory.replace("ring_from_jeffery", "dororans_engraved_ring")
        player["gunnars_ground"] = "engraved_ring"
    }
}

on<NPCOption>({ operate && target.id == "dororan" && option == "Talk-to" }) { player: Player ->
    when (player.quest("gunnars_ground")) {
        "started" -> started()
        "love_poem", "jeffery_ring" -> lovePoem()
        "engrave" -> {
            npc<Talking>("Is it done? Have you created a work of magnificent beauty?")
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

suspend fun CharacterContext.poem() {
    if (!player.ownsItem("gunnars_ground")) {
        player<Sad>("Er, I lost the poem.")
        npc<Talk>("Luckily for you, I wrote a second draft.")
        if (player.inventory.isFull()) {
            statement("You don't have room for the poem. Speak to Dororan again when you have room.")
            return
        }
        player.inventory.add("gunnars_ground")
        player.setAnimation("14738")
        item("gunnars_ground", 600, "Dororan gives you another poem.")
        npc<Talk>("Try not to lose this one.")
        return
    }
    npc<Unsure>("My poem is terrible, isn't it? The Chieftain will probably have me killed.")
    choice {
        option<Talk>("Everything will work out.") {
        }
        option<Talk>("I expect so.") {
        }
    }
}

suspend fun CharacterContext.poemDone() {
    npc<Unknown_expression>("At last! It's done! It's finished! My finest work! Thank you so much for your help!")
    player<Happy>("Are you ready to present it to Chieftain?!")
    npc<Surprised>("What? No! I'm a writer, not a performer.")
    npc<Talk>("I think the Chieftain would respond best to one of his people. Perhaps you could ask Gudrun to recite it to hew father?")
    if (player.inventory.isFull()) {
        statement("You don't have room for the poem. Speak to Dororan again when you have room.")
        return
    }
    player["gunnars_ground"] = "poem"
    player.inventory.add("gunnars_ground")
    player.setAnimation("14738")
    item("gunnars_ground", 400, "Dororan hands you the poem.")
    choice {
        option<Talk>("I'll get right on it.") {
        }
        option<Talk>("This had better be the last time.") {
        }
    }
}

suspend fun CharacterContext.sweptToWar() {
    npc<Happy>("'Who then, in face of madness, <blue>swept to war.</col>'")
    npc<Cheerful>("That's it! That's brilliant!")
    player["gunnars_ground"] = "poem_done"
    poemDone()
}

suspend fun CharacterContext.oneMore() {
    npc<Happy>("It's coming together. We're nearly done! One more to go!")
    npc<Unsure>("This one is tricky, though. It's a phrase I need. Someone did something.")
    phraseMenu()
}

suspend fun CharacterContext.phraseMenu() {
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
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
    }
}

suspend fun CharacterContext.threeSyllablesMenu() {
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

suspend fun CharacterContext.threeSyllablesMenu2() {
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

suspend fun CharacterContext.fightMenu() {
    choice {
        option<Talk>("Picked a fight.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Started a war.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Marched to battle.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Settled the score.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option("More words") {
            fightMenu2()
        }
    }
}

suspend fun CharacterContext.fightMenu2() {
    choice {
        option<Talk>("Swept to war.") {
            sweptToWar()
        }
        option<Talk>("Loosed a mighty roar.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Initiated a battle.") {
            npc<Talk>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
            loreMenu()
        }
        option<Talk>("Commenced fisticuffs.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option("More words") {
            fightMenu()
        }
    }
}

suspend fun CharacterContext.loreMenu() {
    choice {
        option<Talk>("Started a war.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Cleaned the floor.") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Loosed a mighty roar.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
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

suspend fun CharacterContext.loreMenu2() {
    choice {
        option<Talk>("Learned to soar") {
            npc<Talk>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
            fightMenu()
        }
        option<Talk>("Settled the score.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option<Talk>("Swept to war.") {
            sweptToWar()
        }
        option<Talk>("Counted to flour.") {
            npc<Happy>("that doesn't really fit. It needs to be three syllables long.")
            threeSyllablesMenu()
        }
        option("More words") {
            loreMenu()
        }
    }
}

suspend fun CharacterContext.threat() {
    npc<Happy>("'But long is gone the author of that <blue>threat.</col>'")
    npc<Cheerful>("Perfect! Yes!")
    player["gunnars_ground"] = "one_more_poem"
    oneMore()
}

suspend fun CharacterContext.morePoem() {
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

suspend fun CharacterContext.syllablePoemMenu() {
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

suspend fun CharacterContext.syllablePoemMenu2() {
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

suspend fun CharacterContext.rhymePoemMenu() {
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

suspend fun CharacterContext.rhymePoemMenu2() {
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

suspend fun CharacterContext.dangerPoemMenu() {
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

suspend fun CharacterContext.dangerPoemMenu2() {
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

suspend fun CharacterContext.aboutRing() {
    npc<Unsure>("Did you give Gudrun the ring? What did she think? Did it capture her heart?")
    player<Talk>("There's a problem.")
    npc<Unknown_expression>("It's because I'm a dwarf, isn't it? Or because I'm a poet? I knew it! I'm completely worthless!")
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

suspend fun CharacterContext.mostCruel() {
    npc<Amazed>("Most cruel is fate! Most cruel! Why not?")
    player<Talk>("He's obsessed with the stories of his ancestors. He says his people are still at war.")
    npc<Upset>("This village has stood for a hundred years!")
    player<Talk>("I heard him arguing with one of the others. He says he honours his ancestors this way.")
    npc<Unsure>("Really? Interesting.")
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

suspend fun CharacterContext.anyIdea() {
    npc<Amazed>("An idea occurs to me, but it is hubris of the greatest magnitude.")
    player<Unsure>("What is it?")
    npc<Talk>("What if I wrote a poem? Forged a sweeping, historical epic? Crafted a tale to touch the chieftain's soul?")
    player<Unsure>("Will that work?")
    npc<Happy>("To win the heart of my beloved from her father's iron grasp? It is worth it just to try!")
    player.open("fade_out")
    delay(5)
    player.open("fade_in")
    delay(1)
    player["gunnars_ground"] = "write_poem"
    writePoem()
}

suspend fun CharacterContext.writePoem() {
    npc<Talk>("'Even the bloodiest rose must settle.' Mixed metaphor. Whats settles? Detritus. That's hardly flattering.")
    npc<Talk>("'Even the rolliest boulder...'")
    player<Talk>("How is the poem going?")
    npc<Unknown_expression>("I'm stuck! I'm a worthless wordsmith! My work is pointless! My life is pointless!")
    choice {
        option<Talk>("I'm sure that's not true.") {
            stuckOnWord()
        }
        option<Talk>("What's the problem?") {
            stuckOnWord()
        }
    }
}

suspend fun CharacterContext.stuckOnWord() {
    npc<Unknown_expression>("I'm stuck on a word. By the colossus of King Alvis! I can't find the words!")
    player<Talk>("Maybe I can help. What sort of word?")
    npc<Upset>("I don't know! I'm not some kind of word scientist. I just feel it out as I go.")
    npc<Talk>("Maybe you could suggest some words to get me started. Then I can tell you more.")
    player<Talk>("Alright, How about, uh...")
    poemMenu()
}

suspend fun CharacterContext.poemMenu() {
    choice {
        option<Cheerful>("Cucumber.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Cheerful>("Monkey.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Cheerful>("Saradomin.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Barbarian.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
    }
}

suspend fun CharacterContext.syllableMenu() {
    choice {
        option<Cheerful>("Ham.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Fey.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Cheerful>("Jaunt.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Grass.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option("More words") {
            syllableMenu2()
        }
    }
}

suspend fun CharacterContext.syllableMenu2() {
    choice {
        option<Cheerful>("Roam.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Fish.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Stray.") {
            stray()
        }
        option<Cheerful>("Hay.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option("More words") {
            syllableMenu()
        }
    }
}

suspend fun CharacterContext.wordsMenu() {
    choice {
        option<Cheerful>("Deviate.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Cheerful>("Roam.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Veer.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Traipse.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option("More words") {
            wordsMenu2()
        }
    }
}

suspend fun CharacterContext.wordsMenu2() {
    choice {
        option<Cheerful>("Meander.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Cheerful>("Astray.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Cheerful>("Jaunt.") {
            npc<Talk>("That doesn't really fit. It needs to rhyme with the word 'day'.")
            rhymeMenu()
        }
        option<Cheerful>("Stray.") {
            stray()
        }
        option("More words") {
            wordsMenu()
        }
    }
}

suspend fun CharacterContext.rhymeMenu() {
    choice {
        option<Cheerful>("Lay.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Cheerful>("Beret.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Cheerful>("May.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Cheerful>("Hay.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option("More words") {
            rhymeMenu2()
        }
    }
}

suspend fun CharacterContext.rhymeMenu2() {
    choice {
        option<Cheerful>("Stray.") {
            stray()
        }
        option<Cheerful>("Dismay.") {
            npc<Talk>("That doesn't really fit. It needs to be one syllable long.")
            syllableMenu()
        }
        option<Cheerful>("Tray.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option<Cheerful>("Fey.") {
            npc<Talk>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
            wordsMenu()
        }
        option("More words") {
            rhymeMenu()
        }
    }
}

suspend fun CharacterContext.stray() {
    npc<Talk>("'And from his righteous purpose never <blue>stray.</col>'")
    npc<Happy>("That fits! It fits perfectly. Right meaning, right length, right rhyme. Well done!")
    player["gunnars_ground"] = "more_poem"
    morePoem()
}

suspend fun CharacterContext.meetChieftain() {
    npc<Unsure>("Did you give Gudrun the ring? What did she think?")
    player<Talk>("She liked it, but there's a problem. I'm dealing with it.")
    npc<Amazed>("Oh no!")
}

suspend fun CharacterContext.showGudrun() {
    if (!player.ownsItem("dororans_engraved_ring")) {
        npc<Surprised>("I know. I found it on the ground.")
        if (!giveRing()) {
            return
        }
        npc<Cheerful>("Please try not to lose it again. It's very precious.")
        return
    }
    npc<Talk>("Please take the ring to Gudrun for me.")
    choice {
        option<Talk>("Where is she?") {
            npc<Talk>("Inside the barbarian village.")
        }
        option<Talk>("I'm on it.") {
        }
    }
}

suspend fun CharacterContext.engravedRing() {
    npc<Talking>("Is it done? Have you created a work of magnificent beauty?")
    if (!player.ownsItem("dororans_engraved_ring")) {
        player<Sad>("I did engrave it. but I seem to have lost it.")
        npc<Cheerful>("Is this it? I found it on the ground. You've done a great job on it.")
        if (!giveRing()) {
            return
        }
        npc<Cheerful>("Please try not to lose it again! Now, will you do one more thing for me?")
        oneMoreThing()
        return
    }
    choice {
        option<Talk>("It's come out perfectly.") {
            item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
            npc<Cheerful>("You're right! It's perfect!")
            npc<Cheerful>("Will you do one more thing for me?")
            oneMoreThing()
        }
        option<Talk>("How does this look?") {
            item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
            npc<Cheerful>("Brilliant! That's perfect")
            npc<Cheerful>("Will you do one more thing for me?")
            oneMoreThing()
        }
        option<Talk>("It's a complete disaster.") {
            item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
            npc<Cheerful>("I don't know what you mean: it's perfect!")
            npc<Cheerful>("Will you do one more thing for me?")
            oneMoreThing()
        }
    }
}

suspend fun CharacterContext.oneMoreThing() {
    choice {
        option<Talk>("Of course.") {
            veryWell()
        }
        option<Talk>("What now?") {
            veryWell()
        }
    }
}

suspend fun CharacterContext.veryWell() {
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

suspend fun CharacterContext.whereIsShe() {
    npc<Talk>("Please don't tell her I'm a dwarf just yet.")
    player["gunnars_ground"] = "show_gudrun"
    choice {
        option<Talk>("Where is she?") {
            npc<Talk>("Inside the barbarian village.")
        }
        option<Talk>("I'm on it.") {
        }
    }
}

suspend fun CharacterContext.unstarted() {
    npc<Upset>("'My heart with burdens heavy does it lie.'")
    npc<Upset>("'For never did I...'")
    npc<Upset>("Um...")
    choice {
        option<Cheerful>("'...ever learn to fly?'") {
            poet()
        }
        option<Cheerful>("'...eat redberry pie?'") {
            poet()
        }
        option<Cheerful>("'...get the evil eye?'") {
            poet()
        }
    }
}

suspend fun CharacterContext.started() {
    npc<Talk>("I need a ring of purest gold. Then we can engrave it with the words of my heart.")
    player.refreshQuestJournal()
    npc<Happy>("Oh! I know the perfect place to get a gold ring.")
    npc<Unsure>("Edgeville's metalsmith, jeffery, labours like myself under the weight of unrequited love.")
    npc<Happy>("Perhaps, if you took one of my love poems to jeffery, he would trade it for a gold ring.")
    if (player.inventory.isFull()) {
        statement("You don't have room for the poem. Speak to Dororan again when you have room.")
        return
    }
    player["gunnars_ground"] = "love_poem"
    player.inventory.add("love_poem")
    player.setAnimation("14738")
    item("love_poem", 600, "Dororan gives you a poem.")
    choice {
        option<Talking>("I have some questions.") {
            npc<Happy>("By all means.")
            lovePoemMenu()
        }
        option<Talking>("I'll return with a ring from Jeffery.") {
        }
    }
}

suspend fun CharacterContext.somethingElse() {
    choice {
        option<Talking>("I want to ask about something else.") {
            npc<Happy>("By all means.")
            lovePoemMenu()
        }
        option<Talking>("I'll return with a ring from Jeffery.") {
        }
    }
}

suspend fun CharacterContext.lovePoemMenu() {
    choice {
        option<Talking>("Does it have to be a ring from Jeffery?") {
            npc<Talk>("Yes! Jeffery's rings are timeless works of incomparable romantic splendour.")
            somethingElse()
        }
        option<Talking>("Where is Edgeville?") {
            npc<Sad>("North of here, beyond a ruined fortress. It used to be a bustling den of cutthroats but it's quite quiet these days.")
            somethingElse()
        }
        option<Talking>("Why can't you go yourself?") {
            npc<Sad>("Some time ago, Jeffery asked me for advice in acting on his affections. I gave him the best advice that I could.")
            npc<Talk>("Things didn't work out very well for him. One thing let to another and now he no longer wishes to speak to me.")
            somethingElse()
        }
        option<Talking>("Why can't you give a poem directly to Gudrun?") {
            npc<Talk>("These love poems are written in the Misthalinian style. A noble barbarian maiden would be insulted, not flattered.")
            somethingElse()
        }
        option<Talking>("You want me to trick her into thinking you made the ring?") {
            npc<Talk>("Oh no, nothing like that! I have the words, I just need your help with the tools.")
            somethingElse()
        }
    }
}

suspend fun CharacterContext.lovePoem() {
    npc<Talking>("'I await in eagerness for a loop of lustrous grandeur.' No, that just sounds ridiculous. Have you brought me a ring from Jeffery?'")
    if (!player.ownsItem("ring_from_jeffery") && player.quest("gunnars_ground") == "jeffery_ring") {
        player<Cheerful>("I did get a ring from jeffery, but I seem to have lost it.")
        npc<Surprised>("How careless!")
        npc<Unsure>("Is it this one? I found it on the ground.")
        if (player.inventory.isFull()) {
            statement("You don't have room for the ring. Speak to Dororan again when you have room.")
            return
        }
        player.inventory.add("ring_from_jeffery")
        player.setAnimation("14738")
        // player.playSound("")//todo
        item("ring_from_jeffery", 600, "Dororan gives you back the ring.")
        engrave()
        return
    }
    if (player.holdsItem("ring_from_jeffery")) {
        player<Cheerful>("I have one right here.")
        item("ring_from_jeffery", 600, "You show Dororan the ring from Jeffery.")
        npc<Cheerful>("Thank you! That's exactly what I need!")
        engrave()
        return
    }
    choice {
        if (!player.ownsItem("love_poem") && player.quest("gunnars_ground") == "love_poem") {
            option<Talking>("I lost the poem I was supposed to take to Jeffer.") {
                npc<Upset>("I'll give you another one.")
                if (player.inventory.isFull()) {
                    statement("You don't have room for the poem. Speak to Dororan again when you have room.")
                    return@option
                }
                player.inventory.add("love_poem")
                player.setAnimation("14738")
                item("love_poem", 600, "Dororan gives you another poem.")
                npc<Talk>("Try to be more careful with this one.")
                return@option
            }
        }
        option<Talking>("Where would I find one?") {
            npc<Talk>("Go north to Jeffery in Edgeville and trade the poem I gave you for a gold ring.")
            choice {
                option<Talking>("I have some questions.") {
                    npc<Happy>("By all means.")
                    lovePoemMenu()
                }
                option<Talking>("I'll return with a ring from Jeffery.") {
                }
            }
        }
        option<Talking>("I'll return with a ring from Jeffery.") {
        }
    }
}

suspend fun CharacterContext.engrave() {
    npc<Talk>("Now, would you engrave something on it for me?")
    choice {
        option<Talking>("What do you want me to engrave?") {
            engraveSomething()
        }
        option<Talking>("It had better be something impressive.") {
            engraveSomething()
        }
    }
}

suspend fun CharacterContext.engraveSomething() {
    npc<Happy>("I've given this some thought.")
    npc<Cheerful>("'Gudrun the Fair, Gudrun the Fiery.'")
    choice {
        option<Talking>("How do I engrave that?") {
            npc<Talk>("Just use a chisel on the gold ring.")
            player["gunnars_ground"] = "engrave"
            engraveMenu()
        }
        option<Talking>("That sounds simple enough.") {
            npc<Talk>("Just use a chisel on the gold ring.")
            player["gunnars_ground"] = "engrave"
            engraveMenu()
        }
    }
}

suspend fun CharacterContext.engraveMenu() {
    choice {
        option<Talking>("Do you have a chisel I can use?") {
            haveChisel()
        }
        option<Talking>("Isn't a chisel a bit clumsy for that?") {
            chiselBitClumsy()
        }
        option<Talk>("Not yet.") {
        }
    }
}

suspend fun CharacterContext.haveChisel() {
    npc<Cheerful>("Yes, here you go.")
    if (player.inventory.isFull()) {
        statement("You don't have room for the chisel. Speak to Dororan again when you have room.")
    } else {
        player.inventory.add("chisel")
        player.setAnimation("14738")
        item("chisel", 600, "Dororan gives you a chisel.")
    }
    choice {
        option<Talking>("Isn't a chisel a bit clumsy for that?") {
            chiselBitClumsy()
        }
        option<Talk>("Okay.") {
        }
    }
}

suspend fun CharacterContext.chiselBitClumsy() {
    npc<Cheerful>("I've seen jewelcrafters use them for all sorts of precise work.")
    choice {
        option<Talking>("Do you have a chisel I can use?") {
            haveChisel()
        }
        option<Talk>("Okay.") {
        }
    }
}

suspend fun CharacterContext.poet() {
    npc<Cheerful>("You're a poet too?")
    choice {
        option<Cheerful>("Yes.") {
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

suspend fun CharacterContext.identify() {
    npc<Unknown_expression>("My heart is stricken with that most audacious of maladies!")
    choice {
        option<Talking>("Angina?") {
            love()
        }
        option<Talking>("Hypertension?") {
            love()
        }
        option<Talking>("Coclearabsidosis?") {
            love()
        }
    }
}

suspend fun CharacterContext.love() {
    npc<Amazed>("Love!")
    npc<Upset>("The walls of my heart are besieged by love's armies, and those walls begin to tumble!")
    npc<Upset>("In the barbarian village lives the fairest maiden I have witnessed in all my life.")
    choice {
        option<Talking>("What's so special about her?") {
            npc<Happy>("I wouldn't know where to start! Her fiery spirit? Her proud bearing? Her winsome form?")
            choice {
                option<Talking>("But why is this making you sad?") {
                    getToThePoint()
                }
                option<Talking>("What do you actually need?") {
                    getToThePoint()
                }
            }
        }
        option<Talking>("Get to the point.") {
            getToThePoint()
        }
    }
}

suspend fun CharacterContext.getToThePoint() {
    npc<Sad>("The people of this village value strength, stature and riches. I have none of these things.")
    npc<Upset>("My people are indomitable warriors, dripping with gold and precious gems, but not I.")
    npc<Sad>("I am not built fpr combat, and poetry has proven a life of poverty!")
    choice {
        option<Talking>("There must be something you can do.") {
            helpMe()
        }
        option<Talking>("Not to mention low stature.") {
            npc<Unknown_expression>("You see!")
            helpMe()
        }
    }
}

suspend fun CharacterContext.helpMe() {
    npc<Unsure>("If Gudrun could ever love a dwarf, surely she would need to see my artisanry.")
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

on<NPCOption>({ operate && target.id == "dororan_after_quest" && option == "Talk-to" }) { player: Player ->
    when (player.quest("gunnars_ground")) {
        "completed" -> {
            if (player["dororan_ruby_bracelet", 0] != 1) {
                npc<Cheerful>("Come in, my friend, come in! There is another matter I could use your assistance with.")
            } else if (player["dororan_dragonstone_necklace", 0] != 1) {
                npc<Happy>("I have another piece of jewellery to engrave.")
            } else if (player["dororan_onyx_amulet", 0] != 1) {
                npc<Happy>("I have one last piece of jewellery to engrave.")
            } else {
                npc<Happy>("Thanks so much for everything you've done for us!")
                npc<Happy>("What can I do for you?")
            }
            if (player["dororan_ruby_bracelet", 0] != 1 || player["dororan_dragonstone_necklace", 0] != 1 || player["dororan_onyx_amulet", 0] != 1) {
                choice {
                    if (player["dororan_ruby_bracelet", 0] != 1) {
                        option<Talking>("What is it?") {
                            npc<Happy>("I have some more jewellery for Gudrun and I need your help to engrave them.")
                            choice {
                                option<Talking>("What's the first piece?") {
                                    npc<Happy>("A magnificent ruby bracelet.")
                                    npc<Cheerful>("'With beauty blessed.'")
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
                                            // items( "chisel","ruby_bracelet","You carefully engrave 'With beauty blessed' onto the ruby bracelet.")
                                            npc<Cheerful>("Magnificent! Outstanding! I will give this to her immediately. Please, come back when you have time")
                                        }
                                        option("Don't engrave the bracelet.") {
                                            npc<Sad>("That's a shame. Maybe you can try again another time.")
                                        }
                                    }
                                }
                                option<Talking>("I want to talk about something else.") {
                                    npc<Happy>("What can I do for you?")
                                    someThingElse()
                                }
                                option<Talking>("I don't have time right now.") {
                                }
                            }
                        }
                    } else if (player["dororan_dragonstone_necklace", 0] != 1) {
                        option<Talking>("What's this one?") {
                            npc<Happy>("A fine dragonstone necklace.")
                            npc<Cheerful>("There's not much room...how about just 'Gudrun'?")
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
                                    // items( "chisel","dragonstone_necklace","You skillfully engrave 'Gudrun' onto the dragonstone necklace.")
                                    npc<Cheerful>("Another astonishing piece of work! Please, come back later to see if I have other crafting tasks.")
                                }
                                option("Don't engrave the necklace.") {
                                    npc<Sad>("That's a shame. Maybe you can try again another time.")
                                }
                            }
                        }
                    } else if (player["dororan_onyx_amulet", 0] != 1) {
                        option<Talking>("What is it?") {
                            npc<Happy>("An onyx amulet!")
                            npc<Cheerful>("'The most beautiful girl in the room.'")
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
                                    // items( "chisel","onyx_amulet","You expertly engrave 'The most beautiful girl in the room' onto the onyx amulet.")
                                    npc<Cheerful>("That's fantastic! Excellent work.")
                                }
                                option("Don't engrave the amulet.") {
                                    npc<Sad>("That's a shame. Maybe you can try again another time.")
                                }
                            }
                        }
                    }
                    option<Talking>("I want to talk about something else.") {
                        npc<Happy>("What can I do for you?")
                        someThingElse()
                    }
                    option<Talking>("I don't have time right now.") {
                    }
                }
            } else {
                someThingElse()
            }
        }
        else -> player.message("error")
    }
}

suspend fun CharacterContext.someThingElse() {
    choice {
        option<Talking>("How are things?") {
            npc<Happy>("Every morning I wake to sunshine and birdsong! Life is marvellous!")
            elseGoodbye()
        }
        option<Talking>("This is a very large house.") {
            npc<Amazed>("I know! I don't know where Gunthor would have got such a thing. Maybe Gudrun has some idea.")
            elseGoodbye()
        }
        option<Talking>("I'd like to see the poem you wrote for Gunthor.") {
            if (player.inventory.isFull()) {
                statement("you don't have room for the poem.")
                return@option
            }
            player.inventory.add("gunnars_ground")
            player.setAnimation("14738")
            item("gunnars_ground", 600, "Dororan gives you a copy of the poem.")
            npc<Happy>("There you go!")
        }
        if (!player.ownsItem("swanky_boots")) {
            option<Surprised>("I seem to have mislaid my swanky boots.") {
                npc<Cheerful>("Not to worry! There are some left. Here you go.")
                if (player.inventory.isFull()) {
                    statement("you don't have room for the boots.")
                    return@option
                }
                player.setAnimation("14738")
                player.inventory.add("swanky_boots")
                item("swanky_boots", 600, "Dororan gives you some more boots.")
                npc<Cheerful>("Be more careful with these ones! I don't have an infinite supply.")
            }
        }
        option<Talking>("Goodbye.") {
            npc<Cheerful>("Goodbye!")
        }
    }
}

suspend fun CharacterContext.giveRing(): Boolean {
    if (player.inventory.isFull()) {
        statement("You don't have room for the ring. Speak to Dororan again when you have room.")
        return false
    }
    player.inventory.add("dororans_engraved_ring")
    player.setAnimation("14738")
    item("dororans_engraved_ring", 400, "Dororan hands you back the engraved ring.")
    return true
}

suspend fun CharacterContext.elseGoodbye() {
    choice {
        option<Talking>("I want to talk about something else.") {
            npc<Happy>("What can I do for you?")
            someThingElse()
        }
        option<Talking>("Goodbye.") {
            npc<Cheerful>("Goodbye!")
        }
    }
}