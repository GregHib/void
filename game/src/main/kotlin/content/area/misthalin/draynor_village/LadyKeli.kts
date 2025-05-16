package content.area.misthalin.draynor_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val npcs: NPCs by inject()

npcOperate("Talk-to", "lady_keli") {
    when (player.quest("prince_ali_rescue")) {
        "unstarted", "osman" -> {
            npc<Angry>("What do you want?")
            player<Quiz>("Nothing?")
            npc<Angry>("Clear off then.")
        }
        "joe_beers" -> {
            if (player.inventory.contains("rope")) {
                player<Happy>("Hello! I'm here to tie you up!")
                npc<Uncertain>("What?")
                tieUp()
            } else {
                statement("You cannot tie Keli up until you have all equipment and disabled the guard!")
            }
        }
        "keli_tied_up", "prince_ali_disguise", "completed" -> {
            target.say("You tricked me, and tied me up, Guards kill this stranger!!")
            player.message("Guards alerted to kill you!")
            val guard = npcs[player.tile.regionLevel].sortedBy { it.tile.distanceTo(player.tile) }.firstOrNull { it.id.startsWith("draynor_jail_guard") } ?: return@npcOperate
            guard.mode = Interact(guard, player, PlayerOption(guard, player, "Attack"))
            guard.say("Yes M'lady")
        }
        else -> {
            player<Happy>("Are you the famous Lady Keli? Leader of the toughest gang of mercenary killers around?")
            npc<Shifty>("I am Keli, you have heard of me then?")
            choice {
                option("Heard of you? You are famous in ${Settings["server.name"]}!") {
                    player<Happy>("The great Lady Keli, of course I have heard of you. You are famous in ${Settings["server.name"]}!")
                    heard()
                }
                katrine("I have heard a little, but I think Katrine is tougher.")
                option<Uncertain>("I have heard rumours that you kill people.") {
                    npc<Talk>("There's always someone ready to spread rumours. I hear all sort of ridiculous things these days.")
                    choice {
                        latestPlan()
                        trained()
                        disturb()
                    }
                }
                option<Talk>("No I have never really heard of you.") {
                    npc<Surprised>("You must be new around here then. Everyone knows of Lady Keli and her prowess with a sword.")
                    choice {
                        option<Uncertain>("No, still doesn't ring a bell.") {
                            npc<Angry>("Well, you know of me now. You should also know that I will wring your neck if you don't show some respect.")
                            choice {
                                option<Talk>("I don't show respect to killers and hoodlums.") {
                                    npc<Angry>("You should, you really should. I am wealthy enough to place a bounty on your head, or I could just remove your head myself. Luckily, I am too busy to deal with the likes of you, so clear off!")
                                }
                                latestPlan()
                                trained()
                                disturb()
                            }
                        }
                        option<Happy>("Actually, I have heard of you. You're famous in Gielinor!") {
                            heard()
                        }
                        trained()
                        disturb()
                    }
                }
            }
        }
    }
}

itemOnNPCOperate("rope", "lady_keli") {
    when (player.quest("prince_ali_rescue")) {
        "joe_beers" -> tieUp()
        else -> player.noInterest()
    }
}

fun ChoiceBuilder<NPCOption<Player>>.escape() {
    option<Talk>("Can you be sure they will not try to get him out?") {
        npc<Shifty>("There is no way to release him. The only key to the door is on a chain around my neck and the locksmith who made the lock died suddenly when he had finished.")
        npc<Talk>("There is not another key like this in the world.")
        choice {
            option("Could I see the key please?") {
                player<Talk>("Could I see the key please? Just for a moment. It would be something I can tell my grandchildren. When you are even more famous than you are now.")
                npc<Happy>("As you put it that way I am sure you can see it. You cannot steal the key, it is on a Runite chain.")
                statement("Keli shows you a small key on a strong looking chain.")
                choice {
                    option<Happy>("Could I touch the key for a moment please?") {
                        npc<Talk>("Only for a moment then.")
                        if (player.inventory.contains("soft_clay")) {
                            statement("You put a piece of your soft clay in your hand. As you touch the key, you take an imprint of it.")
                            player.inventory.replace("soft_clay", "key_print")
                            player<Happy>("Thank you so much, you are too kind, o great Keli.")
                            npc<Talk>("You are welcome, run along now, I am very busy.")
                        } else {
                            player<Talk>("Well I'll be off. Good luck.")
                        }
                    }
                    disturb()
                }
            }
            option<Talk>("That is a good way to keep secrets.") {
                npc<Talk>("It is the best way I know. Dead men tell no tales.")
            }
            disturb()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.disturb() {
    option<Happy>("I should not disturb someone as tough as you.") {
        npc<Talk>("Yes, I am very busy. Goodbye.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.areYouSure(text: String = "That's great, are you sure they will pay?") {
    option(text) {
        player<Quiz>("Are you sure they will pay?")
        npc<Talk>("They will pay, or we will cut his hair off and send it to them.")
        player<Uncertain>("How about trying something tougher? Maybe cut his finger off?")
        npc<Talk>("That's a good idea, I could use talented people like you. I may call on you if I need work doing.")
        choice {
            skillful()
            escape()
            disturb()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.skillful() {
    option<Talk>("Ah I see. You must have been very skillful.") {
        npc<Talk>("Yes, I did most of the work. We had to grab the Pr...")
        npc<Talk>("Er, we had to grab him without his ten bodyguards noticing. It was a stroke of genius.")
        choice {
            areYouSure("Are you sure they will pay?")
            escape()
            disturb()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.latestPlan() {
    option("What is your latest plan then?") {
        player<Quiz>("What is your latest plan then? Of course, you need not go into specific details.")
        npc<Talk>("Well, I can tell you I have a valuable prisoner here in my cells.")
        npc<Talk>("I can expect a high reward to be paid very soon for this guy. I can't tell you who he is, but he is a lot colder now.")
        choice {
            skillful()
            areYouSure()
            escape()
            disturb()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.trained() {
    option<Happy>("You must have trained a lot for this work.") {
        npc<Angry>("I have used a sword since I was a girl. My first kill was before I was even six years old.")
    }
}

suspend fun NPCOption<Player>.heard() {
    npc<Happy>("That's very kind of you to say. Reputations are not easily earned. I have managed to succeed where many fail.")
    choice {
        katrine()
        latestPlan()
        trained()
        disturb()
    }
}

fun ChoiceBuilder<NPCOption<Player>>.katrine(text: String = "I think Katrine is tougher.") {
    option<Talk>(text) {
        npc<Angry>("Well you can think that all you like. I know those blackarm cowards dare not leave the city. Out here, I am toughest. You can tell them that! Now get out of my sight, before I call my guards.")
    }
}

suspend fun TargetInteraction<Player, NPC>.tieUp() {
    statement("You overpower Keli, tie her up, and put her in a cupboard.")
    player.inventory.remove("rope")
    player["prince_ali_rescue"] = "keli_tied_up"
    target.hide = true
    target.softQueue("keli_respawn", TimeUnit.SECONDS.toTicks(60)) {
        target.hide = false
    }
}