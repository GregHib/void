package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class Davey : Script {
    init {
        npcOperate("Talk-to", "davey") { (target) ->
            walkToDelay(tile = Tile(2132, 5099, 1), forceWalk = true)
            when (questStage("rum_deal")) {
                13 -> rumDealHelpRequest(target)
                19 -> postQuestGreeting()
                else -> defaultGreeting()
            }
        }
    }

    // ===== Default greeting (player hasn't reached the wrench step yet) =====

    private suspend fun Player.defaultGreeting() {
        player<Happy>("Hello!")
        npc<Shifty>("'Ello guv.")
        player<Quiz>("Why are you not helping out the others?")
        npc<Shifty>(
            "Well you see, guv, I've got these unshakable religious convictions.",
        )
        player<Quiz>("And those are?")
        npc<Shifty>(
            "Well, it's like this, see. I used to do a bit of the old priestin' on the side.",
        )
        npc<Neutral>(
            "You know, collectin' for repairs to the church roof, passing the collection " +
                "plate, jumble sales, nicking the lead off the temple roof so they needed " +
                "repairing again.",
        )
        npc<Neutral>("Real holy stuff, you know.")
        player<Neutral>("That doesn't sound all that holy to me.")
        npc<Shifty>("Well, that's because you're not a man of the cloth are you?")
        npc<Neutral>(
            "You are, in fact, what we refer to in layman's terms as a punter.",
        )
        npc<Neutral>(
            "But anyway, all that priestin' left me with an unshakable faith in three things.",
        )
        npc<Neutral>("The power of good over evil.")
        npc<Neutral>("The true glory of the human spirit.")
        npc<Neutral>(
            "And that I ain't goin out there with those things runnin' around.",
        )
    }

    // ===== Progress 13: Wrench blessing request =====

    private suspend fun Player.rumDealHelpRequest(dave: NPC) {
        player<Quiz>("You used to be a priest, right?")
        npc<Shifty>(
            "I didn't nick anything, guv. I've got twenty people who'll swear blind I was...",
        )
        player<Quiz>("What?")
        player<Neutral>(
            "Never mind. I need some help with the spirit in the brewing equipment.",
        )
        when {
            inventory.contains("holy_wrench") -> {
                npc<Shifty>(
                    "You mean more help? I've already blessed one wrench for you, that should " +
                        "be enough.",
                )
            }
            !inventory.contains("wrench") -> {
                npc<Shifty>(
                    "Well, I'll bless something for you if you need it, but just don't spread " +
                        "it around that I'm giving out freebies.",
                )
            }
            levels.get(Skill.Prayer) >= 47 -> {
                npc<Shifty>(
                    "Trust me, guv, lots of people need help after coming into contact with " +
                        "the spirits we produce here.",
                )
                askToBlessWrench(dave)
            }
            else -> {
                askToBlessWrench(dave)
            }
        }
    }

    private suspend fun Player.askToBlessWrench(dave: NPC) {
        player<Quiz>("Could you bless this wrench for me?")
        if (levels.get(Skill.Prayer) >= 47) {
            blessTheWrench(dave)
        } else {
            npc<Shifty>("Nope.")
            player<Quiz>("Nope?")
            npc<Shifty>("Nope. You don't strike me as being... devout, you know?")
            player<Quiz>("Come again?")
            npc<Shifty>(
                "Your holy aura, it's a little shoddy, if you catch my drift.",
            )
            player<Quiz>("What?")
            npc<Shifty>("You need a Prayer of 47.")
            player<Happy>("Oh!")
            player<Quiz>("Well why didn't you just say so?")
            npc<Shifty>(
                "I might have had a bit of an overestimation problem guv. I'm sure it won't " +
                    "happen again.",
            )
        }
    }

    private suspend fun Player.blessTheWrench(dave: NPC) {
        npc<Shifty>("I might well do.  Remember, only the first one's free.")
        npc<Neutral>("Dominoes Ad Nauseum, Romanes Eunt Domus.")
        npc<Neutral>("Sorted.")
        player<Quiz>("Is that it?")
        npc<Shifty>("Oh, you want the full package deal.")
        npc<Neutral>("All right. Brace yourself.")
        playBlessingCutscene(dave)
        player<Happy>("Thanks!")
        npc<Shifty>("No problem, guv. Good luck with your little problem.")
        npc<Neutral>(
            "You might find that little wrench worth hanging on to after you're done with the " +
                "Spirit.",
        )
        player<Quiz>(
            "Really? I mean, it's holy and everything, but I don't think it looks all that " +
                "useful.",
        )
        npc<Shifty>(
            "Well, it may not look much, but you'll find that you might need a few less " +
                "prayer potions if you have it in your pack, if you know what I mean.",
        )
        player<Happy>(
            "Well, no.  I don't know what you mean, but I'm sure I'll find out!",
        )
    }

    // ===== Progress 19: Post-quest greeting =====

    private suspend fun Player.postQuestGreeting() {
        if (inventory.contains("holy_wrench")) {
            npc<Shifty>("Ello again guv. How's things?")
            player<Happy>("Good! Everything seems to have worked out ok.")
            npc<Shifty>(
                "I'm sure it'll stay that way so long as you don't know it's missing.",
            )
            player<Quiz>("What?")
            npc<Shifty>("Nothin' guv, you just have a nice day.")
        } else {
            npc<Shifty>("Ello again guv. I take it you're here about the wrench?")
            player<Quiz>("What? The Holy Wrench? But I lost it somewhere...")
            npc<Shifty>("Well guv, maybe that's what I want you to think.")
            if (inventory.spaces > 0) {
                inventory.add("holy_wrench")
                npc<Neutral>("There you go. I've kept it nice and holy for you.")
                player<Quiz>("Thanks...")
            } else {
                npc<Neutral>(
                    "As soon as you've got somewhere to put it I'll hand it over. I'll keep it " +
                        "nice and holy until then.",
                )
            }
        }
    }

    // ===== Helpers =====

    private suspend fun Player.playBlessingCutscene(davey: NPC) {
        davey.anim("emote_cheer")
        inventory.remove("wrench")
        inventory.add("holy_wrench")
        delay(4)
        moveCamera(
            tile = Tile(2134, 5099),
            height = 180,
            speed = 10,
            acceleration = 70,
        )
        turnCamera(
            tile = Tile(2130, 5099),
            height = 100,
            speed = 10,
            acceleration = 70,
        )
        delay(2)
        moveCamera(
            tile = Tile(2135, 5099),
            height = 275,
            speed = 1,
            acceleration = 70,
        )
        turnCamera(
            tile = Tile(2132, 5099),
            height = 200,
            speed = 10,
            acceleration = 70,
        )
        delay(3)
        gfx("deal_spotanim")
        anim("deal_wrench_flourish")
        delay(3)
        say("Groovy.")
        delay(3)
        clearCamera()
    }
}
