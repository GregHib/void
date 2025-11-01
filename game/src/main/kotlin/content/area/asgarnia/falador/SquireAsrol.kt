package content.area.asgarnia.falador

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.jingle
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.SuspendableContext

class SquireAsrol : Script {

    init {
        npcOperate("Talk-to", "squire_asrol") {
            when (quest("the_knights_sword")) {
                "unstarted" -> {
                    npc<Neutral>("Hello. I am the squire to Sir Vyvin.")
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
    }

    suspend fun Player.started() {
        npc<Quiz>("So how are you doing getting a sword?")
        player<Sad>("I'm looking for Imcando dwarves to help me.")
        npc<Sad>("Please try and find them quickly... I am scared Sir Vyvin will find out!")
    }

    suspend fun Player.askAboutPicture() {
        npc<Quiz>("So how are you doing getting a sword?")
        player<Happy>("I've found an Imcando dwarf but he needs a picture of the sword before he can make it.")
        npc<Uncertain>("A picture eh? Hmmm.... The only one I can think of is in a small portrait of Sir Vyvin's father... Sir Vyvin keeps it in a cupboard in his room I think.")
        set("the_knights_sword", "cupboard")
        player<Neutral>("Ok, I'll try and get that then.")
        npc<Uncertain>("Please don't let him catch you! He MUSTN'T know what happened!")
    }

    suspend fun Player.checkPicture() {
        npc<Quiz>("So how are you doing getting a sword?")
        if (holdsItem("portrait")) {
            player<Happy>("I have the picture. I'll just take it to the dwarf now!")
            npc<Uncertain>("Please hurry!")
            return
        }
        player<Sad>("I didn't get the picture yet.")
        npc<Sad>("Please try and get it quickly... I am scared Sir Vyvin will find out!")
    }

    suspend fun Player.bluriteSword() {
        if (equipment.contains("blurite_sword")) {
            player<Happy>("I have retrieved your sword for you.")
            npc<Uncertain>("So can you un-equip it and hand it over to me now please?")
            return
        }
        if (holdsItem("blurite_sword")) {
            player<Happy>("I have retrieved your sword for you.")
            npc<Happy>("Thank you, thank you, thank you! I was seriously worried I would have to own up to Sir Vyvin!")
            statement("You give the sword to the squire.")
            inventory.remove("blurite_sword")
            questComplete()
            return
        }
        if (ownsItem("blurite_sword")) {
            player<Neutral>("I got a replacement sword made.")
            npc<Happy>("Thank you! Can I have it?")
            player<Neutral>("I've got it stored safely.")
            npc<Frustrated>("Well could you go and get it for me then please? Quickly?")
            player<Neutral>("Yeah, okay.")
            return
        }
        npc<Quiz>("So how are you doing getting a sword?")
        player<Happy>("I've found a dwarf who will make the sword. I've just got to find the materials for it now!")
        npc<Uncertain>("Please hurry!")
    }

    suspend fun ChoiceBuilder2.lifeAsASquire() = option<Quiz>("And how is life as a squire?") {
        npc<Sad>("Well, Sir Vyvin is a good guy to work for, however, I'm in a spot of trouble today. I've gone and lost Sir Vyvin's sword!")
        choice {
            whereYouLostIt()
            newSword()
            heAngry()
        }
    }

    suspend fun ChoiceBuilder2.whereYouLostIt() = option<Quiz>("Do you know where you lost it?") {
        npc<Uncertain>("Well now, if I knew THAT it wouldn't be lost, now would it?")
        choice {
            vagueArea()
            newSword()
            abundantWithSwords()
            heAngry()
        }
    }

    suspend fun ChoiceBuilder2.vagueArea() = option<Quiz>("Well, do you know the VAGUE AREA you lost it in?") {
        npc<Sad>("No. I was carrying it for him all the way from where he had it stored in Lumbridge. It must have slipped from my pack during the trip, and you know what people are like these days...")
        npc<Sad>("Someone will have just picked it up and kept it for themselves.")
        choice {
            newSword()
            abundantWithSwords()
            hopeYouFind()
        }
    }

    suspend fun ChoiceBuilder2.abundantWithSwords() = option<Neutral>("Well the kingdom is fairly abundant with swords...") {
        npc<Sad>("Yes. You can get bronze swords anywhere. But THIS isn't any old sword.")
        heirloom()
    }

    suspend fun Player.heirloom() {
        npc<Sad>("The thing is, this sword is a family heirloom. It has been passed down through Vyvin's family for five generations! It was originally made by the Imcando dwarves, who were")
        npc<Sad>("a particularly skilled tribe of dwarven smiths. I doubt anyone could make it in the style they do.")
        choice {
            anotherSword()
            hopeYouFind()
        }
    }

    suspend fun ChoiceBuilder2.anotherSword() = option<Quiz>("So would these dwarves make another one?") {
        npc<Sad>("I'm not a hundred percent sure the Imcando tribe exists anymore. I should think Reldo, the palace librarian in Varrock, will know; he has done a lot of research on the races of Gielinor.")
        npc<Quiz>("I don't suppose you could try and track down the Imcando dwarves for me? I've got so much work to do...")
        startQuest()
    }

    suspend fun ChoiceBuilder2.hopeYouFind() = option<Neutral>("Well, I hope you find it soon.") {
        npc<Sad>("Yes, me too. I'm not looking forward to telling Vyvin I've lost it. He's going to want it for the parade next week as well.")
    }

    suspend fun ChoiceBuilder2.newSword() = option<Neutral>("I can make a new sword if you like...") {
        npc<Sad>("Thanks for the offer. I'd be surprised if you could though.")
        heirloom()
    }

    suspend fun ChoiceBuilder2.heAngry() = option<Quiz>("Is he angry?") {
        npc<Sad>("He doesn't know yet. I was hoping I could think of something to do before he does find out, But I find myself at a loss.")
        choice {
            vagueArea()
            newSword()
            abundantWithSwords()
            hopeYouFind()
        }
    }

    suspend fun ChoiceBuilder2.squireForMe() = option<Quiz>("Wouldn't you prefer to be a squire for me?") {
        npc<Neutral>("No, sorry, I'm loyal to Sir Vyvin.")
    }

    suspend fun Player.startQuest() {
        if (levels.get(Skill.Mining) < 10 && combatLevel < 20) {
            statement("Before starting this quest, be aware that one or more of your skill levels are lower than what is required to fully complete it. Your combat level is also lower than the recommended level of 20.")
        } else if (levels.get(Skill.Mining) < 10) {
            statement("Before starting this quest, be aware that one or more of your skill levels are lower than what is required to fully complete it.")
        } else if (combatLevel < 20) {
            statement("Before starting this quest, be aware that your combat level is lower than the recommended level of 20.")
        }
        choice("Start The Knight's Sword quest?") {
            option("Yes.") {
                set("the_knights_sword", "started")
                player<Neutral>("Ok, I'll give it a go.")
                npc<Happy>("Thank you very much! As I say, the best place to start should be with Reldo...")
            }
            option("No.") {
                npc<Sad>("Oh man... I'm in such trouble...")
            }
        }
    }

    suspend fun Player.completed() {
        npc<Happy>("Hello friend! Many thanks for all of your help! Vyvin never even realised it was a different sword, and I still have my job!")
        player<Happy>("I'm glad the new sword worked out alright.")
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "the_knights_sword")
        set("the_knights_sword", "completed")
        jingle("quest_complete_1")
        experience.add(Skill.Smithing, 12725.0)
        refreshQuestJournal()
        inc("quest_points")
        message("Congratulations! Quest complete!")
        softQueue("quest_complete", 1) {
            player.questComplete(
                "The Knight's Sword Quest",
                "1 Quest Point",
                "12,725 Smithing XP",
                item = "blurite_sword",
            )
        }
    }
}
