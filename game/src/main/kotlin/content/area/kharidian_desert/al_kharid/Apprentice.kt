package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Upset
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

@Script
class Apprentice : Api {
    init {
        npcOperateDialogue("Talk-to", "apprentice") {
            if (player["sorceress_garden_unlocked", false]) {
                player<Happy>("Hey apprentice, do you want to try out your teleport skills again?")
                npc<Talk>("Okay, here goes - and remember, to return just drink from the fountain.")
                teleportToGarden()
                return@npcOperateDialogue
            }
            player<Talk>("Hello. What are you doing?")
            npc<Sad>("Cleaning, cleaning, always cleaning. This apprenticeship isn't all that it was cracked up to be.")
            player<Quiz>("Whose apprentice are you?")
            npc<Talk>("Oh, Aadeela, the sorceress upstairs, said she'd teach me magic, she did. And here I am scrubbing floors without a spell to help me.")
            choice {
                option("I could cast a Water Blast or a Wind Blast spell?") {
                    player<Quiz>("I could cast a Water Blast or a Wind Blast spell to hurry things along if you'd like?")
                    npc<Talk>("No, no, she'd kill me or worse if she knew I was using Magic to do chores. Her last apprentice - well I'd rather not say.")
                    player<Quiz>("Oh go on, what happened to them?")
                    npc<Surprised>("They say she turned them into little spiders.")
                    player<Talk>("Oh, that's too bad. I had better leave you to it.")
                }
                option<Quiz>("Surely there must be upsides to the task?") {
                    npc<Talk>("Nope. Clean this, clean that. When I'm finished cleaning here I have to go help out in the garden.")
                    player<Quiz>("What garden?")
                    npc<Surprised>("Oh, I shouldn't have told you.")
                    choice {
                        option<Talk>("You're right, you shouldn't have.") {
                            npc<Angry>("Well, if you don't mind then, I'm busy and have to get back to work.")
                            player<Talk>("Don't let me keep you.")
                        }
                        option<Talk>("Oh, you can talk to me. I can see you're having a bad day.") {
                            talkToMe()
                        }
                    }
                }
            }
        }

        npcOperateDialogue("Teleport", "apprentice") {
            if (!player["spoken_to_osman", false]) {
                npc<Talk>("I can't do that now, I'm far too busy sweeping.")
            } else {
                teleportToGarden()
            }
        }
    }

    suspend fun Dialogue.talkToMe() {
        npc<Sad>("You know you're right. Nobody listens to me.")
        choice {
            option<EvilLaugh>("I can't blame them, all you do is whine.") {
                statement("The apprentice is clearly hurt and ignores you.")
            }
            option<Talk>("A sympathetic ear can do wonders.") {
                npc<Happy>("Yes, if I just let my frustrations out, I'd feel a lot better. Now what was I saying?")
                choice {
                    option("I don't know. You were whining about something or other.") {
                        player<Talk>("I don't know. You were whining about something or other. To tell you the truth, I wasn't really listening.")
                        statement("The apprentice is clearly hurt and ignores you.")
                    }
                    option<Talk>("To tell you the truth, I wasn't really listening.") {
                        statement("The apprentice is clearly hurt and ignores you.")
                    }
                    option<Talk>("You mentioned something about the garden.") {
                        npc<Talk>("Oh yeah, that dreadful garden of hers.")
                        player<Talk>("Where is it?")
                        npc<Talk>("Oh, it's nowhere.")
                        player<Talk>("What do you mean?")
                        npc<Talk>("Well it's here, but not really. You see the sorceress is trying out some new type of compression magic.")
                        player<Talk>("Oh, that sounds interesting - so how does it work?")
                        npc<Talk>("It would take too long to explain and, to be honest, I don't really understand how it works.")
                        player<Talk>("Fair enough, but tell me, how do you get to the garden?")
                        npc<Talk>("By magic! The sorceress did teach me one spell.")
                        choice {
                            option<Happy>("Wow, cast the spell on me. It will be good Magic training for you.") {
                                castTheSpell()
                            }
                            option<Talk>("Oh, that's nice. Well it's been great talking to you.")
                        }
                    }
                }
            }
        }
    }

    suspend fun Dialogue.castTheSpell() {
        if (!player["spoken_to_osman", false]) {
            npc<Talk>("I can't do that now, I'm far too busy sweeping.")
        } else {
            npc<Quiz>("You wouldn't mind?")
            player<Talk>("Of course not. I'd be glad to help.")
            npc<Talk>("Okay, here goes! Remember, to return, just drink from the fountain.")
            player["sorceress_garden_unlocked"] = true
            teleportToGarden()
        }
    }

    private val Player.hasFollower: Boolean
        get() = false

    suspend fun Dialogue.teleportToGarden() {
        if (player.hasFollower) {
            npc<Upset>("Oh, I'm sorry, could you pick up your follower first? I'm really not sure that I could teleport the both of you.")
            return
        }
        if (!World.members) {
            player.message("You need to be on a members world to use this feature.")
            return
        }
        target.face(player)
        target.say("Seventior Disthinte Molesko!")
        target.gfx("curse_cast")
        player.sound("curse_cast")
        target.anim("curse")
        target.shoot("curse", player.tile)
        delay(3)
        player.sound("curse_impact", delay = 100)
        player.gfx("curse_impact", delay = 100)
        delay(1)
        player.tele(2912, 5474)
    }
}
