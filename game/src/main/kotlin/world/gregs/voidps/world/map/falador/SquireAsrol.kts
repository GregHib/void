package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.inc
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.*
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.sound.playJingle

on<NPCOption>({ operate && npc.id == "squire_asrol" && option == "Talk-to" }) { player: Player ->
    when (player["the_knights_sword", "unstarted"]) {
        "unstarted" -> {
            npc<Talking>("Hello. I am the squire to Sir Vyvin.")
            choice {
                option("And how is life as a squire?") {
                    lifeAsASquire()
                }
                option("Wouldn't you prefer to be a squire for me?") {
                    squireForMe()
                }
            }
        }
        "started" -> started()
        "stage4" -> stage4()
        "stage5" -> stage5()
        "stage6" -> stage6()
        else -> completed()
    }
}

suspend fun Interaction.started() {
    npc<Unsure>("So how are you doing getting a sword?")
    player<Sad>("I'm looking for Imcando dwarves to help me.")
    npc<Sad>("""
        Please try and find them quickly... I am scared Sir 
        Vyvin will find out!
    """)
}

suspend fun Interaction.stage4() {
    npc<Unsure>("So how are you doing getting a sword?")
    player<Cheerful>("""
        I've found an Imcando dwarf but he needs a picture of
        the sword before he can make it.
    """)
    npc<Uncertain>("""
        A picture eh? Hmmm.... The only one I can think of is
        in a small portrait of Sir Vyvin's father... Sir Vyvin
        keeps it in a cupboard in his room I think.
    """)
    player["the_knights_sword"] = "stage5"
    player<Talking>("Ok, I'll try and get that then.")
    npc<Uncertain>("""
        Please don't let him catch you! He MUSTN'T know
        what happened!
    """)
}

suspend fun Interaction.stage5() {
    if (player.hasItem("portrait")) {
        npc<Unsure>("So how are you doing getting a sword?")
        player<Cheerful>("""
            I have the picture.
            I'll just take it to the dwarf now!
        """)
        npc<Uncertain>("Please hurry!")
        return
    }
    npc<Unsure>("So how are you doing getting a sword?")
    player<Sad>("I didn't get the picture yet.")
    npc<Sad>("""
        Please try and get it quickly... I am scared Sir Vyvin
        will find out!
    """)
}

suspend fun Interaction.stage6() {
    if (player.equipment.contains("blurite_sword")) {
        player<Cheerful>("I have retrieved your sword for you.")
        npc<Uncertain>("""
            So can you un-equip it and hand it over to me now
            please?
        """)
        return
    }
    if (player.hasItem("blurite_sword")) {
        player<Cheerful>("I have retrieved your sword for you.")
        npc<Cheerful>("""
            Thank you, thank you, thank you! I was seriously
            worried I would have to own up to Sir Vyvin!
        """)
        statement("You give the sword to the squire.")
        player.inventory.remove("blurite_sword")
        questComplete()
        return
    }
    if (player.hasBanked("blurite_sword")) {
        player<Talking>("I got a replacement sword made.")
        npc<Cheerful>("Thank you! Can I have it?")
        player<Talking>("I've got it stored safely.")
        npc<Angry>("""
            Well could you go and get it for me then please?
            Quickly?
        """)
        player<Talking>("Yeah, okay.")
        return
    }
    npc<Unsure>("So how are you doing getting a sword?")
    player<Cheerful>("""
        I've found a dwarf who will make the sword.
        I've just got to find the materials for it now!
    """)
    npc<Uncertain>("Please hurry!")
}

suspend fun Interaction.lifeAsASquire() {
    player<Unsure>("And how is life as a squire?")
    npc<Sad>("""
        Well, Sir Vyvin is a good guy to work for, however,
        I'm in a spot of trouble today. I've gone and lost Sir
        Vyvin's sword!
    """)
    choice {
        option("Do you know where you lost it?") {
            whereYouLostIt()
        }
        option("I can make a new sword if you like...") {
            newSword()
        }
        option("Is he angry?") {
            heAngry()
        }
    }
}

suspend fun Interaction.whereYouLostIt() {
    player<Unsure>("Do you know where you lost it?")
    npc<Uncertain>("""
        Well now, if I knew THAT it wouldn't be lost, now 
        would it?
    """)
    choice {
        option("Well, do you know the VAGUE AREA you lost it in?") {
            vagueArea()
        }
        option("I can make a new sword if you like...") {
            newSword()
        }
        option("Well the kingdom is fairly abundant with swords...") {
            abundantWithSwords()
        }
        option("Is he angry?") {
            heAngry()
        }
    }
}

suspend fun Interaction.vagueArea() {
    player<Unsure>("Well, do you know the VAGUE AREA you lost it in?")
    npc<Sad>("""
        No. I was carrying it for him all the way from where
        he had it stored in Lumbridge. It must have slipped
        from my pack during the trip, and you know what 
        people are like these days...
    """)
    npc<Sad>("""
        Someone will have just picked it up and kept it for
        themselves.
    """)
    choice {
        option("I can make a new sword if you like...") {
            newSword()
        }
        option("Well the kingdom is fairly abundant with swords...") {
            abundantWithSwords()
        }
        option("Well, I hope you find it soon.") {
            findItSoon()
        }
    }
}

suspend fun Interaction.abundantWithSwords() {
    player<Talking>("Well, the kingdom is fairly abundant with swords...")
    npc<Sad>("""
        Yes. You can get bronze swords anywhere. But THIS
        isn't any old sword.
    """)
    heirloom()
}


suspend fun Interaction.heirloom() {
    npc<Sad>("""
        The thing is, this sword is a family heirloom. It has been 
        passed down through Vyvin's family for five 
        generations! It was originally made by the Imcando 
        dwarves, who were
    """)
    npc<Sad>("""
        a particularly skilled tribe of dwarven smiths. I doubt 
        anyone could make it in the style they do.
    """)
    choice {
        option("So would these dwarves make another one?") {
            anotherSword()
        }
        option("Well, I hope you find it soon.") {
            findItSoon()
        }
    }
}

suspend fun Interaction.anotherSword() {
    player<Unsure>("So would these dwarves make another one?")
    npc<Sad>("""
        I'm not a hundred percent sure the Imcando tribe
        exists anymore. I should think Reldo, the palace
        librarian in Varrock, will know; he has done a lot of 
        research on the races of Gielinor.
    """)
    npc<Unsure>("""
        I don't suppose you could try and track down the 
        Imcando dwarves for me? I've got so much work to
        do...
    """)
    startQuest()
}

suspend fun Interaction.findItSoon() {
    player<Talking>("Well, I hope you find it soon.")
    npc<Sad>("""
        Yes, me too. I'm not looking forward to telling Vyvin
        I've lost it. He's going to want it for the parade next
        week as well.
    """)
}

suspend fun Interaction.newSword() {
    player<Talking>("I can make a new sword if you like...")
    npc<Sad>("""
        Thanks for the offer. I'd be surprised if you could
        though.
    """)
    heirloom()
}

suspend fun Interaction.heAngry() {
    player<Unsure>("Is he angry?")
    npc<Sad>("""
        He doesn't know yet. I was hoping I could think of 
        something to do before he does find out, But I find 
        myself at a loss.
    """)
    choice {
        option("Well, do you know the VAGUE AREA you lost it in?") {
            vagueArea()
        }
        option("I can make a new sword if you like...") {
            newSword()
        }
        option("Well the kingdom is fairly abundant with swords...") {
            abundantWithSwords()
        }
        option("Well, I hope you find it soon.") {
            findItSoon()
        }
    }
}

suspend fun Interaction.squireForMe() {
    player<Unsure>("Wouldn't you prefer to be a squire for me?")
    npc<Talking>("No, sorry, I'm loyal to Sir Vyvin.")
}

suspend fun Interaction.startQuest() {
    if (player.levels.get(Skill.Mining) < 10 && player.combatLevel < 20) {
        statement("""
            Before starting this quest, be aware that one or more of your skill
            levels are lower than what is required to fully complete it. Your
            combat level is also lower than the recommended level of 20.
        """)
    } else if (player.levels.get(Skill.Mining) < 10) {
        statement("""
            Before starting this quest, be aware that one or more of your skill
            levels are lower than what is required to fully complete it.
        """)
    } else if (player.combatLevel < 20) {
        statement("""
            Before starting this quest, be aware that your combat level is lower
            than the recommended level of 20.
        """)
    }
    choice("Start The Knight's Sword quest?") {
        option("yes.") {
            player["the_knights_sword"] = "started"
            player<Talking>("Ok, I'll give it a go.")
            npc<Cheerful>("""
                Thank you very much! As I say, the best place to start 
                should be with Reldo...
            """)
        }
        option("No.") {
            npc<Sad>("Oh man... I'm in such trouble...")
        }
    }
}

suspend fun Interaction.completed() {
    npc<Cheerful>("""
        Hello friend! Many thanks for all of your help! Vyvin
        never even realised it was a different sword, and I still
        have my job!
    """)
    player<Cheerful>("I'm glad the new sword worked out alright.")
}

fun Interaction.questComplete() {
    player["the_knights_sword"] = "completed"
    player.playJingle("quest_complete_1")
    player.experience.add(Skill.Smithing, 12725.0)
    player.refreshQuestJournal()
    player.inc("quest_points")
    player.message("Congratulations! Quest complete!")
    player.softQueue("quest_complete", 1) {
        player.sendQuestComplete("The Knight's Sword Quest", listOf(
            "1 Quest Point",
            "12,725 Smithing XP"
        ), Item("blurite_sword"))
    }
}
