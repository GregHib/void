package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound

class Apprentice : Script {

    init {
        npcOperate("Talk-to", "apprentice") { (target) ->
            if (get("sorceress_garden_unlocked", false)) {
                player<Happy>("Hey apprentice, do you want to try out your teleport skills again?")
                npc<Neutral>("Okay, here goes - and remember, to return just drink from the fountain.")
                teleportToGarden(target)
                return@npcOperate
            }
            player<Neutral>("Hello. What are you doing?")
            npc<Disheartened>("Cleaning, cleaning, always cleaning. This apprenticeship isn't all that it was cracked up to be.")
            player<Quiz>("Whose apprentice are you?")
            npc<Neutral>("Oh, Aadeela, the sorceress upstairs, said she'd teach me magic, she did. And here I am scrubbing floors without a spell to help me.")
            choice {
                option("I could cast a Water Blast or a Wind Blast spell?") {
                    player<Quiz>("I could cast a Water Blast or a Wind Blast spell to hurry things along if you'd like?")
                    npc<Neutral>("No, no, she'd kill me or worse if she knew I was using Magic to do chores. Her last apprentice - well I'd rather not say.")
                    player<Quiz>("Oh go on, what happened to them?")
                    npc<Shock>("They say she turned them into little spiders.")
                    player<Neutral>("Oh, that's too bad. I had better leave you to it.")
                }
                option<Quiz>("Surely there must be upsides to the task?") {
                    npc<Neutral>("Nope. Clean this, clean that. When I'm finished cleaning here I have to go help out in the garden.")
                    player<Quiz>("What garden?")
                    npc<Shock>("Oh, I shouldn't have told you.")
                    choice {
                        option<Neutral>("You're right, you shouldn't have.") {
                            npc<Angry>("Well, if you don't mind then, I'm busy and have to get back to work.")
                            player<Neutral>("Don't let me keep you.")
                        }
                        option<Neutral>("Oh, you can talk to me. I can see you're having a bad day.") {
                            talkToMe(target)
                        }
                    }
                }
            }
        }

        npcOperate("Teleport", "apprentice") { (target) ->
            if (!get("spoken_to_osman", false)) {
                npc<Neutral>("I can't do that now, I'm far too busy sweeping.")
            } else {
                teleportToGarden(target)
            }
        }
    }

    suspend fun Player.talkToMe(target: NPC) {
        npc<Disheartened>("You know you're right. Nobody listens to me.")
        choice {
            option<EvilLaugh>("I can't blame them, all you do is whine.") {
                statement("The apprentice is clearly hurt and ignores you.")
            }
            option<Neutral>("A sympathetic ear can do wonders.") {
                npc<Happy>("Yes, if I just let my frustrations out, I'd feel a lot better. Now what was I saying?")
                choice {
                    option("I don't know. You were whining about something or other.") {
                        player<Neutral>("I don't know. You were whining about something or other. To tell you the truth, I wasn't really listening.")
                        statement("The apprentice is clearly hurt and ignores you.")
                    }
                    option<Neutral>("To tell you the truth, I wasn't really listening.") {
                        statement("The apprentice is clearly hurt and ignores you.")
                    }
                    option<Neutral>("You mentioned something about the garden.") {
                        npc<Neutral>("Oh yeah, that dreadful garden of hers.")
                        player<Neutral>("Where is it?")
                        npc<Neutral>("Oh, it's nowhere.")
                        player<Neutral>("What do you mean?")
                        npc<Neutral>("Well it's here, but not really. You see the sorceress is trying out some new type of compression magic.")
                        player<Neutral>("Oh, that sounds interesting - so how does it work?")
                        npc<Neutral>("It would take too long to explain and, to be honest, I don't really understand how it works.")
                        player<Neutral>("Fair enough, but tell me, how do you get to the garden?")
                        npc<Neutral>("By magic! The sorceress did teach me one spell.")
                        choice {
                            option<Happy>("Wow, cast the spell on me. It will be good Magic training for you.") {
                                castTheSpell(target)
                            }
                            option<Neutral>("Oh, that's nice. Well it's been great talking to you.")
                        }
                    }
                }
            }
        }
    }

    suspend fun Player.castTheSpell(target: NPC) {
        if (!get("spoken_to_osman", false)) {
            npc<Neutral>("I can't do that now, I'm far too busy sweeping.")
        } else {
            npc<Quiz>("You wouldn't mind?")
            player<Neutral>("Of course not. I'd be glad to help.")
            npc<Neutral>("Okay, here goes! Remember, to return, just drink from the fountain.")
            set("sorceress_garden_unlocked", true)
            teleportToGarden(target)
        }
    }

    private val Player.hasFollower: Boolean
        get() = false

    suspend fun Player.teleportToGarden(target: NPC) {
        if (hasFollower) {
            npc<Sad>("Oh, I'm sorry, could you pick up your follower first? I'm really not sure that I could teleport the both of you.")
            return
        }
        if (!World.members) {
            message("You need to be on a members world to use this feature.")
            return
        }
        target.face(this)
        target.say("Seventior Disthinte Molesko!")
        target.gfx("curse_cast")
        sound("curse_cast")
        target.anim("curse")
        target.shoot("curse", tile)
        delay(3)
        sound("curse_impact", delay = 100)
        gfx("curse_impact", delay = 100)
        delay(1)
        tele(2912, 5474)
    }
}
