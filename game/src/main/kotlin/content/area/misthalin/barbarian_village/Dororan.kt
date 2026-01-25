package content.area.misthalin.barbarian_village

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.questCompleted
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.softQueue

class Dororan : Script {

    init {
        itemOnItem("chisel", "ring_from_jeffery") { _, _ ->
            if (quest("gunnars_ground") == "jeffery_ring") {
                noInterest()
            } else {
                softQueue("engraving") {
                    item("dororans_engraved_ring", 400, "You engrave 'Gudrun the Fair, Gudrun the Fiery' onto the ring.")
                    anim("engrave")
                    experience.add(Skill.Crafting, 125.0)
                    inventory.replace("ring_from_jeffery", "dororans_engraved_ring")
                    set("gunnars_ground", "engraved_ring")
                }
            }
        }

        npcOperate("Talk-to", "dororan_*") {
            when (quest("gunnars_ground")) {
                "started" -> started()
                "love_poem", "jeffery_ring" -> lovePoem()
                "engrave" -> {
                    npc<Idle>("Is it done? Have you created a work of magnificent beauty?")
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

        npcOperate("Talk-to", "dororan_after_quest") {
            if (!questCompleted("gunnars_ground")) {
                return@npcOperate
            }
            if (get("dororan_ruby_bracelet", 0) != 1) {
                npc<Happy>("Come in, my friend, come in! There is another matter I could use your assistance with.")
            } else if (get("dororan_dragonstone_necklace", 0) != 1) {
                npc<Pleased>("I have another piece of jewellery to engrave.")
            } else if (get("dororan_onyx_amulet", 0) != 1) {
                npc<Pleased>("I have one last piece of jewellery to engrave.")
            } else {
                npc<Pleased>("Thanks so much for everything you've done for us!")
                npc<Pleased>("What can I do for you?")
            }
            if (get("dororan_ruby_bracelet", 0) != 1 || get("dororan_dragonstone_necklace", 0) != 1 || get("dororan_onyx_amulet", 0) != 1) {
                choice {
                    if (get("dororan_ruby_bracelet", 0) != 1) {
                        option<Idle>("What is it?") {
                            npc<Pleased>("I have some more jewellery for Gudrun and I need your help to engrave them.")
                            choice {
                                option<Idle>("What's the first piece?") {
                                    npc<Pleased>("A magnificent ruby bracelet.")
                                    npc<Happy>("'With beauty blessed.'")
                                    choice {
                                        option("Engrave the bracelet.") {
                                            if (levels.get(Skill.Crafting) < 72) {
                                                item("ruby_bracelet", 400, "you need a Crafting level of at least 42 to engrave the ruby bracelet.")
                                                npc<Disheartened>("That's a shame. Maybe you can try again another time.")
                                                return@option
                                            }
                                            anim("engrave")
                                            experience.add(Skill.Crafting, 2000.0)
                                            set("dororan_ruby_bracelet", 1)
                                            items("chisel", "ruby_bracelet", "You carefully engrave 'With beauty blessed' onto the ruby bracelet.")
                                            npc<Happy>("Magnificent! Outstanding! I will give this to her immediately. Please, come back when you have time")
                                        }
                                        option("Don't engrave the bracelet.") {
                                            npc<Disheartened>("That's a shame. Maybe you can try again another time.")
                                        }
                                    }
                                }
                                option<Idle>("I want to talk about something else.") {
                                    npc<Pleased>("What can I do for you?")
                                    someThingElse()
                                }
                                option<Idle>("I don't have time right now.") {
                                }
                            }
                        }
                    } else if (get("dororan_dragonstone_necklace", 0) != 1) {
                        option<Idle>("What's this one?") {
                            npc<Pleased>("A fine dragonstone necklace.")
                            npc<Happy>("There's not much room...how about just 'Gudrun'?")
                            choice {
                                option("Engrave the necklace.") {
                                    if (levels.get(Skill.Crafting) < 42) {
                                        item("dragonstone_necklace", 400, "you need a Crafting level of at least 72 to engrave the dragonstone necklace.")
                                        npc<Disheartened>("That's a shame. Maybe you can try again another time.")
                                        return@option
                                    }
                                    anim("engrave")
                                    experience.add(Skill.Crafting, 10000.0)
                                    set("dororan_dragonstone_necklace", 1)
                                    items("chisel", "dragonstone_necklace", "You skillfully engrave 'Gudrun' onto the dragonstone necklace.")
                                    npc<Happy>("Another astonishing piece of work! Please, come back later to see if I have other crafting tasks.")
                                }
                                option("Don't engrave the necklace.") {
                                    npc<Disheartened>("That's a shame. Maybe you can try again another time.")
                                }
                            }
                        }
                    } else if (get("dororan_onyx_amulet", 0) != 1) {
                        option<Idle>("What is it?") {
                            npc<Pleased>("An onyx amulet!")
                            npc<Happy>("'The most beautiful girl in the room.'")
                            choice {
                                option("Engrave the amulet.") {
                                    if (levels.get(Skill.Crafting) < 90) {
                                        item("onyx_amulet", 400, "you need a Crafting level of at least 90 to engrave the onyx amulet.")
                                        npc<Disheartened>("That's a shame. Maybe you can try again another time.")
                                        return@option
                                    }
                                    anim("engrave")
                                    experience.add(Skill.Crafting, 20000.0)
                                    set("dororan_onyx_amulet", 1)
                                    items("chisel", "onyx_amulet", "You expertly engrave 'The most beautiful girl in the room' onto the onyx amulet.")
                                    npc<Happy>("That's fantastic! Excellent work.")
                                }
                                option("Don't engrave the amulet.") {
                                    npc<Disheartened>("That's a shame. Maybe you can try again another time.")
                                }
                            }
                        }
                    }
                    option<Idle>("I want to talk about something else.") {
                        npc<Pleased>("What can I do for you?")
                        someThingElse()
                    }
                    option<Idle>("I don't have time right now.") {
                    }
                }
            } else {
                someThingElse()
            }
        }
    }

    suspend fun Player.poem() {
        if (!ownsItem("gunnars_ground")) {
            player<Disheartened>("Er, I lost the poem.")
            npc<Neutral>("Luckily for you, I wrote a second draft.")
            if (inventory.isFull()) {
                statement("You don't have room for the poem. Speak to Dororan again when you have room.")
                return
            }
            inventory.add("gunnars_ground")
            anim("pocket_item")
            item("gunnars_ground", 600, "Dororan gives you another poem.")
            npc<Neutral>("Try not to lose this one.")
            return
        }
        npc<Quiz>("My poem is terrible, isn't it? The Chieftain will probably have me killed.")
        choice {
            option<Neutral>("Everything will work out.")
            option<Neutral>("I expect so.")
        }
    }

    suspend fun Player.poemDone() {
        npc<Cackle>("At last! It's done! It's finished! My finest work! Thank you so much for your help!")
        player<Pleased>("Are you ready to present it to Chieftain?!")
        npc<Shock>("What? No! I'm a writer, not a performer.")
        npc<Neutral>("I think the Chieftain would respond best to one of his people. Perhaps you could ask Gudrun to recite it to hew father?")
        if (inventory.isFull()) {
            statement("You don't have room for the poem. Speak to Dororan again when you have room.")
            return
        }
        set("gunnars_ground", "poem")
        inventory.add("gunnars_ground")
        anim("pocket_item")
        item("gunnars_ground", 400, "Dororan hands you the poem.")
        choice {
            option<Neutral>("I'll get right on it.")
            option<Neutral>("This had better be the last time.")
        }
    }

    suspend fun Player.sweptToWar() {
        npc<Pleased>("'Who then, in face of madness, <blue>swept to war.</col>'")
        npc<Happy>("That's it! That's brilliant!")
        set("gunnars_ground", "poem_done")
        poemDone()
    }

    suspend fun Player.oneMore() {
        npc<Pleased>("It's coming together. We're nearly done! One more to go!")
        npc<Quiz>("This one is tricky, though. It's a phrase I need. Someone did something.")
        phraseMenu()
    }

    suspend fun Player.phraseMenu() {
        choice {
            option<Neutral>("Threw the ball.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Ate a tasty pie.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option<Neutral>("Schemed intently.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option<Neutral>("Went for a walk.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
        }
    }

    suspend fun Player.threeSyllablesMenu() {
        choice {
            option<Neutral>("Picked a rose.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Made a raft.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Learned to soar.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option<Neutral>("Cleaned the floor.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option("More words") {
                threeSyllablesMenu2()
            }
        }
    }

    suspend fun Player.threeSyllablesMenu2() {
        choice {
            option<Neutral>("Heard a song.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option<Neutral>("Picked a flight.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Swept to war.") {
                sweptToWar()
            }
            option<Neutral>("Tamed a shrew.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option("More words") {
                threeSyllablesMenu()
            }
        }
    }

    suspend fun Player.fightMenu() {
        choice {
            option<Neutral>("Picked a fight.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Started a war.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option<Neutral>("Marched to battle.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Settled the score.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option("More words") {
                fightMenu2()
            }
        }
    }

    suspend fun Player.fightMenu2() {
        choice {
            option<Neutral>("Swept to war.") {
                sweptToWar()
            }
            option<Neutral>("Loosed a mighty roar.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option<Neutral>("Initiated a battle.") {
                npc<Neutral>("That doesn't really fit. It needs tp rhyme with the word 'lore'.")
                loreMenu()
            }
            option<Neutral>("Commenced fisticuffs.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option("More words") {
                fightMenu()
            }
        }
    }

    suspend fun Player.loreMenu() {
        choice {
            option<Neutral>("Started a war.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option<Neutral>("Cleaned the floor.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option<Neutral>("Loosed a mighty roar.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option<Neutral>("Shut the door.") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option("More words") {
                loreMenu2()
            }
        }
    }

    suspend fun Player.loreMenu2() {
        choice {
            option<Neutral>("Learned to soar") {
                npc<Neutral>("that doesn't really fit. It needs to imply some aggressive action, like 'started a fight'.")
                fightMenu()
            }
            option<Neutral>("Settled the score.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option<Neutral>("Swept to war.") {
                sweptToWar()
            }
            option<Neutral>("Counted to flour.") {
                npc<Pleased>("that doesn't really fit. It needs to be three syllables long.")
                threeSyllablesMenu()
            }
            option("More words") {
                loreMenu()
            }
        }
    }

    suspend fun Player.threat() {
        npc<Pleased>("'But long is gone the author of that <blue>threat.</col>'")
        npc<Happy>("Perfect! Yes!")
        set("gunnars_ground", "one_more_poem")
        oneMore()
    }

    suspend fun Player.morePoem() {
        npc<Disheartened>("The poem still isn't finished, though. I have another missing word. Give me another one; anything, to get me started.")
        choice {
            option<Neutral>("Stockade.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option<Neutral>("Longsword.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option<Neutral>("Dungeoneering.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Grass.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
        }
    }

    suspend fun Player.syllablePoemMenu() {
        choice {
            option<Neutral>("Storm.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Wet.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option<Neutral>("Hat.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Length.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option("More words.") {
                syllablePoemMenu2()
            }
        }
    }

    suspend fun Player.syllablePoemMenu2() {
        choice {
            option<Neutral>("Debt.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option<Neutral>("Threat.") {
                threat()
            }
            option<Neutral>("Axe.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Risk.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option("More words.") {
                syllablePoemMenu()
            }
        }
    }

    suspend fun Player.rhymePoemMenu() {
        choice {
            option<Neutral>("Debt.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option<Neutral>("Sweat.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option<Neutral>("Upset.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option<Neutral>("Brunette.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option("More words.") {
                rhymePoemMenu2()
            }
        }
    }

    suspend fun Player.rhymePoemMenu2() {
        choice {
            option<Neutral>("Threat.") {
                threat()
            }
            option<Neutral>("Regret.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option<Neutral>("Set.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option<Neutral>("Wet.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'danger'.")
                dangerPoemMenu()
            }
            option("More words.") {
                rhymePoemMenu()
            }
        }
    }

    suspend fun Player.dangerPoemMenu() {
        choice {
            option<Neutral>("Risk.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Crisis.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option<Neutral>("Peril.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option<Neutral>("Menace.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option("More words.") {
                dangerPoemMenu2()
            }
        }
    }

    suspend fun Player.dangerPoemMenu2() {
        choice {
            option<Neutral>("Upset.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllablePoemMenu()
            }
            option<Neutral>("Storm.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Hazard.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'yet'.")
                rhymePoemMenu()
            }
            option<Neutral>("Threat.") {
                threat()
            }
            option("More words.") {
                dangerPoemMenu()
            }
        }
    }

    suspend fun Player.aboutRing() {
        npc<Quiz>("Did you give Gudrun the ring? What did she think? Did it capture her heart?")
        player<Neutral>("There's a problem.")
        npc<Cry>("It's because I'm a dwarf, isn't it? Or because I'm a poet? I knew it! I'm completely worthless!")
        choice {
            option<Neutral>("No, she liked the ring.") {
                npc<Amazed>("Oh! Then what's the problem?")
                mostCruel()
            }
            option<Neutral>("Would you be quiet for a moment?") {
                npc<Sad>("Sorry!")
                mostCruel()
            }
        }
    }

    suspend fun Player.mostCruel() {
        player<Neutral>("Gudrun's father won't let her be with someone from outside the village.")
        npc<Amazed>("Most cruel is fate! Most cruel! Why not?")
        player<Neutral>("He's obsessed with the stories of his ancestors. He says his people are still at war.")
        npc<Sad>("This village has stood for a hundred years!")
        player<Neutral>("I heard him arguing with one of the others. He says he honours his ancestors this way.")
        npc<Quiz>("Really? Interesting.")
        choice {
            option<Neutral>("Do you know a lot about the village's history?") {
                npc<Neutral>("Not really. I talked with Hunding, who guards this tower here.")
                anyIdea()
            }
            option<Neutral>("What are we going to do?") {
                anyIdea()
            }
        }
    }

    suspend fun Player.anyIdea() {
        npc<Amazed>("An idea occurs to me, but it is hubris of the greatest magnitude.")
        player<Quiz>("What is it?")
        npc<Neutral>("What if I wrote a poem? Forged a sweeping, historical epic? Crafted a tale to touch the chieftain's soul?")
        player<Quiz>("Will that work?")
        npc<Pleased>("To win the heart of my beloved from her father's iron grasp? It is worth it just to try!")
        open("fade_out")
        delay(5)
        open("fade_in")
        delay(1)
        set("gunnars_ground", "write_poem")
        writePoem()
    }

    suspend fun Player.writePoem() {
        npc<Neutral>("'Even the bloodiest rose must settle.' Mixed metaphor. Whats settles? Detritus. That's hardly flattering.")
        npc<Neutral>("'Even the rolliest boulder...'")
        player<Neutral>("How is the poem going?")
        npc<Cry>("I'm stuck! I'm a worthless wordsmith! My work is pointless! My life is pointless!")
        choice {
            option<Neutral>("I'm sure that's not true.") {
                stuckOnWord()
            }
            option<Neutral>("What's the problem?") {
                stuckOnWord()
            }
        }
    }

    suspend fun Player.stuckOnWord() {
        npc<Cry>("I'm stuck on a word. By the colossus of King Alvis! I can't find the words!")
        player<Neutral>("Maybe I can help. What sort of word?")
        npc<Sad>("I don't know! I'm not some kind of word scientist. I just feel it out as I go.")
        npc<Neutral>("Maybe you could suggest some words to get me started. Then I can tell you more.")
        player<Neutral>("Alright, How about, uh...")
        poemMenu()
    }

    suspend fun Player.poemMenu() {
        choice {
            option<Happy>("Cucumber.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
            option<Happy>("Monkey.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option<Happy>("Saradomin.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Barbarian.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
        }
    }

    suspend fun Player.syllableMenu() {
        choice {
            option<Happy>("Ham.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Fey.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option<Happy>("Jaunt.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Grass.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option("More words") {
                syllableMenu2()
            }
        }
    }

    suspend fun Player.syllableMenu2() {
        choice {
            option<Happy>("Roam.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Fish.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Stray.") {
                stray()
            }
            option<Happy>("Hay.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option("More words") {
                syllableMenu()
            }
        }
    }

    suspend fun Player.wordsMenu() {
        choice {
            option<Happy>("Deviate.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
            option<Happy>("Roam.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Veer.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option<Happy>("Traipse.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
                rhymeMenu()
            }
            option("More words") {
                wordsMenu2()
            }
        }
    }

    suspend fun Player.wordsMenu2() {
        choice {
            option<Happy>("Meander.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
            option<Happy>("Astray.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
            option<Happy>("Jaunt.") {
                npc<Neutral>("That doesn't really fit. It needs to rhyme with the word 'day'.")
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

    suspend fun Player.rhymeMenu() {
        choice {
            option<Happy>("Lay.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option<Happy>("Beret.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
            option<Happy>("May.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option<Happy>("Hay.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option("More words") {
                rhymeMenu2()
            }
        }
    }

    suspend fun Player.rhymeMenu2() {
        choice {
            option<Happy>("Stray.") {
                stray()
            }
            option<Happy>("Dismay.") {
                npc<Neutral>("That doesn't really fit. It needs to be one syllable long.")
                syllableMenu()
            }
            option<Happy>("Tray.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option<Happy>("Fey.") {
                npc<Neutral>("That doesn't really fit. It needs to mean something like 'wandering aimlessly'.")
                wordsMenu()
            }
            option("More words") {
                rhymeMenu()
            }
        }
    }

    suspend fun Player.stray() {
        npc<Neutral>("'And from his righteous purpose never <blue>stray.</col>'")
        npc<Pleased>("That fits! It fits perfectly. Right meaning, right length, right rhyme. Well done!")
        set("gunnars_ground", "more_poem")
        morePoem()
    }

    suspend fun Player.meetChieftain() {
        npc<Quiz>("Did you give Gudrun the ring? What did she think?")
        player<Neutral>("She liked it, but there's a problem. I'm dealing with it.")
        npc<Amazed>("Oh no!")
    }

    suspend fun Player.showGudrun() {
        if (!ownsItem("dororans_engraved_ring")) {
            npc<Shock>("I know. I found it on the ground.")
            if (!giveRing()) {
                return
            }
            npc<Happy>("Please try not to lose it again. It's very precious.")
            return
        }
        npc<Neutral>("Please take the ring to Gudrun for me.")
        choice {
            option<Neutral>("Where is she?") {
                npc<Neutral>("Inside the barbarian village.")
            }
            option<Neutral>("I'm on it.")
        }
    }

    suspend fun Player.engravedRing() {
        npc<Idle>("Is it done? Have you created a work of magnificent beauty?")
        if (!ownsItem("dororans_engraved_ring")) {
            player<Disheartened>("I did engrave it. but I seem to have lost it.")
            npc<Happy>("Is this it? I found it on the ground. You've done a great job on it.")
            if (!giveRing()) {
                return
            }
            npc<Happy>("Please try not to lose it again! Now, will you do one more thing for me?")
            oneMoreThing()
            return
        }
        choice {
            option<Neutral>("It's come out perfectly.") {
                item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
                npc<Happy>("You're right! It's perfect!")
                npc<Happy>("Will you do one more thing for me?")
                oneMoreThing()
            }
            option<Neutral>("How does this look?") {
                item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
                npc<Happy>("Brilliant! That's perfect")
                npc<Happy>("Will you do one more thing for me?")
                oneMoreThing()
            }
            option<Neutral>("It's a complete disaster.") {
                item("dororans_engraved_ring", 400, "You show Dororan the engraved ring.")
                npc<Happy>("I don't know what you mean: it's perfect!")
                npc<Happy>("Will you do one more thing for me?")
                oneMoreThing()
            }
        }
    }

    suspend fun Player.oneMoreThing() {
        choice {
            option<Neutral>("Of course.") {
                veryWell()
            }
            option<Neutral>("What now?") {
                veryWell()
            }
        }
    }

    suspend fun Player.veryWell() {
        npc<Disheartened>("I fear she will only judge this poor book by its cover. Would you take the ring to Gudrun for me?")
        choice {
            option<Neutral>("Very well.") {
                whereIsShe()
            }
            option<Neutral>("I hope this is going somewhere.") {
                whereIsShe()
            }
        }
    }

    suspend fun Player.whereIsShe() {
        npc<Neutral>("Please don't tell her I'm a dwarf just yet.")
        set("gunnars_ground", "show_gudrun")
        choice {
            option<Neutral>("Where is she?") {
                npc<Neutral>("Inside the barbarian village.")
            }
            option<Neutral>("I'm on it.")
        }
    }

    suspend fun Player.unstarted() {
        npc<Sad>("'My heart with burdens heavy does it lie.'")
        npc<Sad>("'For never did I...'")
        npc<Sad>("Um...")
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

    suspend fun Player.started() {
        npc<Neutral>("I need a ring of purest gold. Then we can engrave it with the words of my heart.")
        refreshQuestJournal()
        npc<Pleased>("Oh! I know the perfect place to get a gold ring.")
        npc<Quiz>("Edgeville's metalsmith, jeffery, labours like myself under the weight of unrequited love.")
        npc<Pleased>("Perhaps, if you took one of my love poems to jeffery, he would trade it for a gold ring.")
        if (inventory.isFull()) {
            statement("You don't have room for the poem. Speak to Dororan again when you have room.")
            return
        }
        set("gunnars_ground", "love_poem")
        inventory.add("love_poem")
        anim("pocket_item")
        item("love_poem", 600, "Dororan gives you a poem.")
        choice {
            option<Idle>("I have some questions.") {
                npc<Pleased>("By all means.")
                lovePoemMenu()
            }
            option<Idle>("I'll return with a ring from Jeffery.")
        }
    }

    suspend fun Player.somethingElse() {
        choice {
            option<Idle>("I want to ask about something else.") {
                npc<Pleased>("By all means.")
                lovePoemMenu()
            }
            option<Idle>("I'll return with a ring from Jeffery.")
        }
    }

    suspend fun Player.lovePoemMenu() {
        choice {
            option<Idle>("Does it have to be a ring from Jeffery?") {
                npc<Neutral>("Yes! Jeffery's rings are timeless works of incomparable romantic splendour.")
                somethingElse()
            }
            option<Idle>("Where is Edgeville?") {
                npc<Disheartened>("North of here, beyond a ruined fortress. It used to be a bustling den of cutthroats but it's quite quiet these days.")
                somethingElse()
            }
            option<Idle>("Why can't you go yourself?") {
                npc<Disheartened>("Some time ago, Jeffery asked me for advice in acting on his affections. I gave him the best advice that I could.")
                npc<Neutral>("Things didn't work out very well for him. One thing let to another and now he no longer wishes to speak to me.")
                somethingElse()
            }
            option<Idle>("Why can't you give a poem directly to Gudrun?") {
                npc<Neutral>("These love poems are written in the Misthalinian style. A noble barbarian maiden would be insulted, not flattered.")
                somethingElse()
            }
            option<Idle>("You want me to trick her into thinking you made the ring?") {
                npc<Neutral>("Oh no, nothing like that! I have the words, I just need your help with the tools.")
                somethingElse()
            }
        }
    }

    suspend fun Player.lovePoem() {
        npc<Idle>("'I await in eagerness for a loop of lustrous grandeur.' No, that just sounds ridiculous. Have you brought me a ring from Jeffery?'")
        if (!ownsItem("ring_from_jeffery") && quest("gunnars_ground") == "jeffery_ring") {
            player<Happy>("I did get a ring from jeffery, but I seem to have lost it.")
            npc<Shock>("How careless!")
            npc<Quiz>("Is it this one? I found it on the ground.")
            if (inventory.isFull()) {
                statement("You don't have room for the ring. Speak to Dororan again when you have room.")
                return
            }
            inventory.add("ring_from_jeffery")
            anim("pocket_item")
            // playSound("") // TODO
            item("ring_from_jeffery", 600, "Dororan gives you back the ring.")
            engrave()
            return
        }
        if (carriesItem("ring_from_jeffery")) {
            player<Happy>("I have one right here.")
            item("ring_from_jeffery", 600, "You show Dororan the ring from Jeffery.")
            npc<Happy>("Thank you! That's exactly what I need!")
            engrave()
            return
        }
        choice {
            if (!ownsItem("love_poem") && quest("gunnars_ground") == "love_poem") {
                option<Idle>("I lost the poem I was supposed to take to Jeffer.") {
                    npc<Sad>("I'll give you another one.")
                    if (inventory.isFull()) {
                        statement("You don't have room for the poem. Speak to Dororan again when you have room.")
                        return@option
                    }
                    inventory.add("love_poem")
                    anim("pocket_item")
                    item("love_poem", 600, "Dororan gives you another poem.")
                    npc<Neutral>("Try to be more careful with this one.")
                    return@option
                }
            }
            option<Idle>("Where would I find one?") {
                npc<Neutral>("Go north to Jeffery in Edgeville and trade the poem I gave you for a gold ring.")
                choice {
                    option<Idle>("I have some questions.") {
                        npc<Pleased>("By all means.")
                        lovePoemMenu()
                    }
                    option<Idle>("I'll return with a ring from Jeffery.") {
                    }
                }
            }
            option<Idle>("I'll return with a ring from Jeffery.")
        }
    }

    suspend fun Player.engrave() {
        npc<Neutral>("Now, would you engrave something on it for me?")
        choice {
            option<Idle>("What do you want me to engrave?") {
                engraveSomething()
            }
            option<Idle>("It had better be something impressive.") {
                engraveSomething()
            }
        }
    }

    suspend fun Player.engraveSomething() {
        npc<Pleased>("I've given this some thought.")
        npc<Happy>("'Gudrun the Fair, Gudrun the Fiery.'")
        choice {
            option<Idle>("How do I engrave that?") {
                npc<Neutral>("Just use a chisel on the gold ring.")
                set("gunnars_ground", "engrave")
                engraveMenu()
            }
            option<Idle>("That sounds simple enough.") {
                npc<Neutral>("Just use a chisel on the gold ring.")
                set("gunnars_ground", "engrave")
                engraveMenu()
            }
        }
    }

    suspend fun Player.engraveMenu() {
        choice {
            option<Idle>("Do you have a chisel I can use?") {
                haveChisel()
            }
            option<Idle>("Isn't a chisel a bit clumsy for that?") {
                chiselBitClumsy()
            }
            option<Neutral>("Not yet.")
        }
    }

    suspend fun Player.haveChisel() {
        npc<Happy>("Yes, here you go.")
        if (inventory.isFull()) {
            statement("You don't have room for the chisel. Speak to Dororan again when you have room.")
        } else {
            inventory.add("chisel")
            anim("pocket_item")
            item("chisel", 600, "Dororan gives you a chisel.")
        }
        choice {
            option<Idle>("Isn't a chisel a bit clumsy for that?") {
                chiselBitClumsy()
            }
            option<Neutral>("Okay.")
        }
    }

    suspend fun Player.chiselBitClumsy() {
        npc<Happy>("I've seen jewelcrafters use them for all sorts of precise work.")
        choice {
            option<Idle>("Do you have a chisel I can use?") {
                haveChisel()
            }
            option<Neutral>("Okay.")
        }
    }

    suspend fun Player.poet() {
        npc<Happy>("You're a poet too?")
        choice {
            option<Happy>("Yes.") {
                npc<Sad>("Ah! Then I'm sure you can identify with the arduous state of my life.")
                identify()
            }
            option<Neutral>("Maybe a bit.") {
                npc<Sad>("Oh. Then maybe you can identify with the arduous state of my life.")
                identify()
            }
            option<Neutral>("No.") {
                npc<Sad>("oh. How can I expect you to identify with the arduous state of my life?")
                identify()
            }
        }
    }

    suspend fun Player.identify() {
        npc<Cry>("My heart is stricken with that most audacious of maladies!")
        choice {
            option<Idle>("Angina?") {
                love()
            }
            option<Idle>("Hypertension?") {
                love()
            }
            option<Idle>("Coclearabsidosis?") {
                love()
            }
        }
    }

    suspend fun Player.love() {
        npc<Amazed>("Love!")
        npc<Sad>("The walls of my heart are besieged by love's armies, and those walls begin to tumble!")
        npc<Sad>("In the barbarian village lives the fairest maiden I have witnessed in all my life.")
        choice {
            option<Idle>("What's so special about her?") {
                npc<Pleased>("I wouldn't know where to start! Her fiery spirit? Her proud bearing? Her winsome form?")
                choice {
                    option<Idle>("But why is this making you sad?") {
                        getToThePoint()
                    }
                    option<Idle>("What do you actually need?") {
                        getToThePoint()
                    }
                }
            }
            option<Idle>("Get to the point.") {
                getToThePoint()
            }
        }
    }

    suspend fun Player.getToThePoint() {
        npc<Disheartened>("The people of this village value strength, stature and riches. I have none of these things.")
        npc<Sad>("My people are indomitable warriors, dripping with gold and precious gems, but not I.")
        npc<Disheartened>("I am not built for combat, and poetry has proven a life of poverty!")
        choice {
            option<Idle>("There must be something you can do.") {
                helpMe()
            }
            option<Idle>("Not to mention low stature.") {
                npc<Cry>("You see!")
                helpMe()
            }
        }
    }

    suspend fun Player.helpMe() {
        npc<Quiz>("If Gudrun could ever love a dwarf, surely she would need to see my artisanry.")
        npc<Neutral>("Will you help me? I am no crafter of metal.")
        if (levels.get(Skill.Crafting) < 5) {
            statement("You need a Crafting level of at least 5 to start this quest.")
        } else {
            choice("Start Gunnar's Ground quest?") {
                option("Yes.") {
                    set("gunnars_ground", "started")
                    started()
                }
                option("No.") {
                }
            }
        }
    }

    suspend fun Player.someThingElse() {
        choice {
            option<Idle>("How are things?") {
                npc<Pleased>("Every morning I wake to sunshine and birdsong! Life is marvellous!")
                elseGoodbye()
            }
            option<Idle>("This is a very large house.") {
                npc<Amazed>("I know! I don't know where Gunthor would have got such a thing. Maybe Gudrun has some idea.")
                elseGoodbye()
            }
            option<Idle>("I'd like to see the poem you wrote for Gunthor.") {
                if (inventory.isFull()) {
                    statement("You don't have room for the poem. Speak to Dororan again when you have room.")
                    return@option
                }
                inventory.add("gunnars_ground")
                anim("pocket_item")
                item("gunnars_ground", 600, "Dororan gives you a copy of the poem.")
                npc<Pleased>("There you go!")
            }
            if (!ownsItem("swanky_boots")) {
                option<Shock>("I seem to have mislaid my swanky boots.") {
                    npc<Happy>("Not to worry! There are some left. Here you go.")
                    if (inventory.isFull()) {
                        statement("you don't have room for the boots.")
                        return@option
                    }
                    anim("pocket_item")
                    inventory.add("swanky_boots")
                    item("swanky_boots", 600, "Dororan gives you some more boots.")
                    npc<Happy>("Be more careful with these ones! I don't have an infinite supply.")
                }
            }
            option<Idle>("Goodbye.") {
                npc<Happy>("Goodbye!")
            }
        }
    }

    suspend fun Player.giveRing(): Boolean {
        if (inventory.isFull()) {
            statement("You don't have room for the ring. Speak to Dororan again when you have room.")
            return false
        }
        inventory.add("dororans_engraved_ring")
        anim("pocket_item")
        item("dororans_engraved_ring", 400, "Dororan hands you back the engraved ring.")
        return true
    }

    suspend fun Player.elseGoodbye() {
        choice {
            option<Idle>("I want to talk about something else.") {
                npc<Pleased>("What can I do for you?")
                someThingElse()
            }
            option<Idle>("Goodbye.") {
                npc<Happy>("Goodbye!")
            }
        }
    }
}
