package content.area.misthalin.draynor_village.manor

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.proj.shoot
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

class ProfessorOddenstein : Script {
    init {
        npcOperate("Talk-to", "professor_oddenstein") { (target) -> talk(target) }
    }

    private suspend fun Player.talk(target: NPC) {
        if (oddensteinErnest(target)) {
            return
        }
        if (!questCompleted("ernest_the_chicken")) {
            npc<Neutral>("Be careful in here, there's lots of dangerous equipment.")
            choice {
                option<Quiz>("What does this machine do?") {
                    npc<Neutral>("Nothing at the moment... It's broken. It's meant to be a transmutation machine.")
                    npc<Neutral>("It has also spent time as a time travel machine, and a dramatic lightning generator, and a thing for generating monsters.")
                }
                option<Quiz>("Is this your house?") {
                    npc<Neutral>("No, I'm just one of the tenants. It belongs to the count who lives in the basement.")
                }
            }
            return
        }
        // TODO on enter portal without boots
        npc<Confused>("Errr, just before you go through there...")
        player<Quiz>("What's the problem?")
        npc<Neutral>("That portal opens into a plane populated with some very shocking creatures. You should wear some kind of insulated armour before going there.")
        choice {
            option<Quiz>("Where can I get insulated armour from?") {
                npc<Neutral>("Well there were some pretty tough people here last week. Said they were Slayer Masters. They were planning on making some protective boots. You should speak to one of them.")
                choice {
                    option<Neutral>("Thanks, I'll do that.") {
                        npc<Neutral>("No problem. See you later.")
                    }
                    option("I don't want to run around after Slayer Masters, I'm going through.") {
                        tele(2677, 5214, 2)
                    }
                }
            }
            option<Neutral>("Thanks, I think I'll stay here for a while then.")
            option("Thanks for the warning, but I'm not scared of any monster.") {
                player<Neutral>("Thanks for the warning, but I'm not scared of any monster.")
                npc<Neutral>("Ok. Just don't say I didn't warn you")
                tele(2677, 5214, 2)
            }
        }
    }

    /** Return false to retain the professor's ordinary and post-quest dialogue. */
    private suspend fun Player.oddensteinErnest(professor: NPC): Boolean {
        when (quest("ernest_the_chicken")) {
            "started" -> {
                npc<Neutral>("Be careful in here, there's lots of dangerous equipment.")
                choice {
                    option<Neutral>("I'm looking for a guy called Ernest.") {
                        npc<Neutral>("Ah Ernest, top notch bloke. He's helping me with my experiments.")
                        player<Neutral>("So you know where he is then?")
                        npc<Neutral>("He's that chicken over there.")
                        player<Neutral>("Ernest is a chicken...? Are you sure?")
                        npc<Neutral>("Oh, he isn't normally a chicken, or at least he wasn't until he helped me test my pouletmorph machine.")
                        npc<Neutral>("It was originally going to be called a transmutation machine. But after testing pouletmorph seems more appropriate.")
                        choice {
                            option<Neutral>("I'm glad Veronica didn't actually get engaged to a chicken.") {
                                npc<Neutral>("Who's Veronica?")
                                player<Neutral>("Ernest's fiancee. She probably doesn't want to marry a chicken.")
                                npc<Neutral>("Ooh I dunno. She could have free eggs for breakfast.")
                                player<Neutral>("I think you'd better change him back.")
                                explainErnestParts()
                            }
                            option<Neutral>("Change him back this instant!") { explainErnestParts() }
                        }
                    }
                    option<Neutral>("What does this machine do?") {
                        npc<Neutral>("Nothing at the moment... It's broken. It's meant to be a transmutation machine.")
                        npc<Neutral>("It has also spent time as a time travel machine, and a dramatic lightning generator, and a thing for generating monsters.")
                    }
                    option<Neutral>("Is this your house?") {
                        npc<Neutral>("No, I'm just one of the tenants. It belongs to the count who lives in the basement.")
                    }
                }
                return true
            }
            "parts" -> return restoreErnest(professor)
            "restored" -> {
                finishErnest()
                return true
            }
            else -> return false
        }
    }

    private suspend fun Player.explainErnestParts() {
        npc<Neutral>("Umm... It's not so easy...")
        npc<Neutral>("My machine is broken, and the house gremlins have run off with some vital bits.")
        player<Neutral>("Well I can look for them.")
        npc<Neutral>("That would be a help. They'll be somewhere in the manor house or its grounds, the gremlins never get further than the entrance gate.")
        npc<Neutral>("I'm missing the pressure gauge and a rubber tube. They've also taken my oil can, which I'm going to need to get this thing started again.")
        set("ernest_the_chicken", "parts")
    }

    /** Report the parts actually carried, without taking a partial hand-in. */
    private suspend fun Player.reportErnestParts(missing: List<String>) {
        val found = listOf("pressure_gauge", "rubber_tube", "oil_can").filterNot { it in missing }.toSet()
        when {
            found.isEmpty() -> {
                player<Neutral>("I'm afraid I don't have any of them yet!")
                npc<Neutral>("I need a rubber tube, a pressure gauge and a can of oil. Then your friend can stop being a chicken.")
            }
            found.size == 1 -> {
                val part = found.single().replace('_', ' ')
                player<Neutral>("I've only got the $part so far.")
                val rest = when (found.single()) {
                    "rubber_tube" -> "pressure gauge and oil can"
                    "pressure_gauge" -> "oil can and rubber tube"
                    else -> "pressure gauge and rubber tube"
                }
                npc<Neutral>("It's a good start, but I'll need my $rest too.")
                player<Neutral>("Ok, I'll go and look for them.")
            }
            else -> {
                val parts = when (missing.single()) {
                    "oil_can" -> "rubber tube and the pressure gauge"
                    "pressure_gauge" -> "oil can and the rubber tube"
                    else -> "oil can and the pressure gauge"
                }
                player<Neutral>("I've got the $parts.")
                npc<Neutral>("That's good, but I still need my ${missing.single().replace('_', ' ')}.")
                player<Neutral>("Ok, I'll go and look for it.")
            }
        }
    }

    /** Cache varp 32 selects the chicken at 0-2, human Ernest at 3, and neither at 4. */
    private suspend fun Player.restoreErnest(professor: NPC): Boolean {
        if (professor.hasClock("ernest_machine")) {
            message("The professor is busy with the machine. Please wait a moment.")
            return true
        }
        if (!get("ernest_parts_returned", false)) {
            npc<Neutral>("Have you found anything yet?")
            val missing = listOf("pressure_gauge", "rubber_tube", "oil_can").filterNot { inventory.contains(it) }
            if (missing.isNotEmpty()) {
                reportErnestParts(missing)
                return true
            }
            player<Neutral>("I have everything!")
            npc<Neutral>("Give 'em here then.")
            if (!inventory.transaction {
                    for (part in listOf("pressure_gauge", "rubber_tube", "oil_can")) {
                        remove(part)
                    }
                }
            ) {
                return true
            }
            // Remember the hand-in before any further dialogue can be dismissed or the player logs out.
            set("ernest_parts_returned", true)
            message("You give the rubber tube, a pressure gauge, and a can of oil to the professor.")
        }
        npc<Neutral>("Let's get this fixed then.")
        if (professor.hasClock("ernest_machine")) {
            message("The professor is busy with the machine. Please wait a moment.")
            return true
        }
        val chicken = NPCs.findOrNull(professor.tile.regionLevel, "chicken_draynor") ?: return true
        val ernest = NPCs.findOrNull(professor.tile.regionLevel, "ernest") ?: return true
        // A standard expiring clock also releases the machine if the player logs out mid-sequence.
        professor.start("ernest_machine", 40)
        val machine = Tile(3112, 3367, 2)
        val machineSide = Tile(3111, 3367, 2)
        professor.walkTo(machineSide, forceWalk = true)
        var attempts = 0
        while (professor.tile != machineSide && attempts++ < 12) {
            delay()
        }
        if (professor.tile != machineSide) {
            professor.stop("ernest_machine")
            message("The professor can't reach the machine. Please try again.")
            return true
        }
        professor.face(machine)
        professor.anim("take")
        professor.delay = 8
        delay(2)
        message("Oddenstein starts up the machine.")
        message("The machine hums and shakes.")
        areaGfx("weaken_cast", machine, height = 80)
        val flight = machine.shoot("weaken", chicken, delay = 0, height = 40)
        delay(((flight + 29) / 30).coerceAtLeast(1))
        chicken.gfx("weaken_impact")
        chicken.sound("weaken_impact")
        delay()
        chicken.gfx("stun_long")
        delay(3)
        chicken.clearGfx()
        ernest.tele(chicken.tile)
        set("ernest_the_chicken", "restored")
        // Ordinary NPC movement stops on arrival; no follower or temporary actor needs cleanup.
        ernest.mode = Movement(ernest, CharacterTargetStrategy(this))
        attempts = 0
        while (ernest.tile.distanceTo(tile) > 1 && attempts++ < 12) {
            delay()
        }
        ernest.face(this)
        professor.stop("ernest_machine")
        finishErnest()
        return true
    }

    private suspend fun Player.finishErnest() {
        npc<Happy>("ernest_human", "Thank you, ${if (male) "sir" else "m'lady"}. It was dreadfully irritating being a chicken. How can *cluck* I ever thank you?")
        player<Neutral>("Well a cash reward is always nice...")
        npc<Happy>("ernest_human", "Of course, of course.")
        if (quest("ernest_the_chicken") != "restored") {
            return
        }
        if (!inventory.transaction {
                add("coins", 3000)
                add("egg_noted", 10)
                add("feather", 300)
            }
        ) {
            message("Make room for your reward, then talk to the professor again.")
            return
        }
        set("ernest_the_chicken", "completed")
        clear("ernest_parts_returned")
        inc("quest_points", 4)
        jingle("quest_complete_1")
        questComplete("Ernest the Chicken", "4 Quest Points", "3,000 coins", "10 noted eggs", "300 feathers", item = "feather")
    }
}
