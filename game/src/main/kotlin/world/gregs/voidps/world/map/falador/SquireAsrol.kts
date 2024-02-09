package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playJingle

npcOperate("Talk-to", "squire_asrol") {
    when (player.quest("the_knights_sword")) {
        "unstarted" -> {
            npc<Talking>("Hello. I am the squire to Sir Vyvin.")
            choice {
                lifeAsASquire()
                squireForMe()
            }
        }
        "started" -> started()
        "picture" -> askAboutPicture()
        "cupboard" -> checkPicture()
        "blurite_sword" -> bluriteSword()
        else -> completed()
    }
}

suspend fun CharacterContext.started() {
    npc<Unsure>("So how are you doing getting a sword?")
    player<Sad>("I'm looking for Imcando dwarves to help me.")
    npc<Sad>("Please try and find them quickly... I am scared Sir Vyvin will find out!")
}

suspend fun CharacterContext.askAboutPicture() {
    npc<Unsure>("So how are you doing getting a sword?")
    player<Cheerful>("I've found an Imcando dwarf but he needs a picture of the sword before he can make it.")
    npc<Uncertain>("A picture eh? Hmmm.... The only one I can think of is in a small portrait of Sir Vyvin's father... Sir Vyvin keeps it in a cupboard in his room I think.")
    player["the_knights_sword"] = "cupboard"
    player<Talking>("Ok, I'll try and get that then.")
    npc<Uncertain>("Please don't let him catch you! He MUSTN'T know what happened!")
}

suspend fun CharacterContext.checkPicture() {
    npc<Unsure>("So how are you doing getting a sword?")
    if (player.holdsItem("portrait")) {
        player<Cheerful>("I have the picture. I'll just take it to the dwarf now!")
        npc<Uncertain>("Please hurry!")
        return
    }
    player<Sad>("I didn't get the picture yet.")
    npc<Sad>("Please try and get it quickly... I am scared Sir Vyvin will find out!")
}

suspend fun CharacterContext.bluriteSword() {
    if (player.equipment.contains("blurite_sword")) {
        player<Cheerful>("I have retrieved your sword for you.")
        npc<Uncertain>("So can you un-equip it and hand it over to me now please?")
        return
    }
    if (player.holdsItem("blurite_sword")) {
        player<Cheerful>("I have retrieved your sword for you.")
        npc<Cheerful>("Thank you, thank you, thank you! I was seriously worried I would have to own up to Sir Vyvin!")
        statement("You give the sword to the squire.")
        player.inventory.remove("blurite_sword")
        questComplete()
        return
    }
    if (player.ownsItem("blurite_sword")) {
        player<Talking>("I got a replacement sword made.")
        npc<Cheerful>("Thank you! Can I have it?")
        player<Talking>("I've got it stored safely.")
        npc<Angry>("Well could you go and get it for me then please? Quickly?")
        player<Talking>("Yeah, okay.")
        return
    }
    npc<Unsure>("So how are you doing getting a sword?")
    player<Cheerful>("I've found a dwarf who will make the sword. I've just got to find the materials for it now!")
    npc<Uncertain>("Please hurry!")
}

suspend fun PlayerChoice.lifeAsASquire() = option<Unsure>("And how is life as a squire?") {
    npc<Sad>("Well, Sir Vyvin is a good guy to work for, however, I'm in a spot of trouble today. I've gone and lost Sir Vyvin's sword!")
    choice {
        whereYouLostIt()
        newSword()
        heAngry()
    }
}

suspend fun PlayerChoice.whereYouLostIt() = option<Unsure>("Do you know where you lost it?") {
    npc<Uncertain>("Well now, if I knew THAT it wouldn't be lost, now would it?")
    choice {
        vagueArea()
        newSword()
        abundantWithSwords()
        heAngry()
    }
}

suspend fun PlayerChoice.vagueArea() = option<Unsure>("Well, do you know the VAGUE AREA you lost it in?") {
    npc<Sad>("No. I was carrying it for him all the way from where he had it stored in Lumbridge. It must have slipped from my pack during the trip, and you know what people are like these days...")
    npc<Sad>("Someone will have just picked it up and kept it for themselves.")
    choice {
        newSword()
        abundantWithSwords()
        hopeYouFind()
    }
}

suspend fun PlayerChoice.abundantWithSwords() = option<Talking>("Well the kingdom is fairly abundant with swords...") {
    npc<Sad>("Yes. You can get bronze swords anywhere. But THIS isn't any old sword.")
    heirloom()
}


suspend fun CharacterContext.heirloom() {
    npc<Sad>("The thing is, this sword is a family heirloom. It has been passed down through Vyvin's family for five generations! It was originally made by the Imcando dwarves, who were")
    npc<Sad>("a particularly skilled tribe of dwarven smiths. I doubt anyone could make it in the style they do.")
    choice {
        anotherSword()
        hopeYouFind()
    }
}

suspend fun PlayerChoice.anotherSword() = option<Unsure>("So would these dwarves make another one?") {
    npc<Sad>("I'm not a hundred percent sure the Imcando tribe exists anymore. I should think Reldo, the palace librarian in Varrock, will know; he has done a lot of research on the races of Gielinor.")
    npc<Unsure>("I don't suppose you could try and track down the Imcando dwarves for me? I've got so much work to do...")
    startQuest()
}

suspend fun PlayerChoice.hopeYouFind() = option<Talking>("Well, I hope you find it soon.") {
    npc<Sad>("Yes, me too. I'm not looking forward to telling Vyvin I've lost it. He's going to want it for the parade next week as well.")
}

suspend fun PlayerChoice.newSword() = option<Talking>("I can make a new sword if you like...") {
    npc<Sad>("Thanks for the offer. I'd be surprised if you could though.")
    heirloom()
}

suspend fun PlayerChoice.heAngry() = option<Unsure>("Is he angry?") {
    npc<Sad>("He doesn't know yet. I was hoping I could think of something to do before he does find out, But I find myself at a loss.")
    choice {
        vagueArea()
        newSword()
        abundantWithSwords()
        hopeYouFind()
    }
}

suspend fun PlayerChoice.squireForMe() = option<Unsure>("Wouldn't you prefer to be a squire for me?") {
    npc<Talking>("No, sorry, I'm loyal to Sir Vyvin.")
}

suspend fun CharacterContext.startQuest() {
    if (player.levels.get(Skill.Mining) < 10 && player.combatLevel < 20) {
        statement("Before starting this quest, be aware that one or more of your skill levels are lower than what is required to fully complete it. Your combat level is also lower than the recommended level of 20.")
    } else if (player.levels.get(Skill.Mining) < 10) {
        statement("Before starting this quest, be aware that one or more of your skill levels are lower than what is required to fully complete it.")
    } else if (player.combatLevel < 20) {
        statement("Before starting this quest, be aware that your combat level is lower than the recommended level of 20.")
    }
    choice("Start The Knight's Sword quest?") {
        option("Yes.") {
            player["the_knights_sword"] = "started"
            player<Talking>("Ok, I'll give it a go.")
            npc<Cheerful>("Thank you very much! As I say, the best place to start should be with Reldo...")
        }
        option("No.") {
            npc<Sad>("Oh man... I'm in such trouble...")
        }
    }
}

suspend fun CharacterContext.completed() {
    npc<Cheerful>("Hello friend! Many thanks for all of your help! Vyvin never even realised it was a different sword, and I still have my job!")
    player<Cheerful>("I'm glad the new sword worked out alright.")
}

fun CharacterContext.questComplete() {
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
