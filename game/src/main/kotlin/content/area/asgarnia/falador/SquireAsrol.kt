package content.area.asgarnia.falador

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue

class SquireAsrol : Script {

    init {
        npcOperate("Talk-to", "squire_asrol") {
            when (quest("the_knights_sword")) {
                "unstarted" -> {
                    npc<Idle>("Hello. I am the squire to Sir Vyvin.")
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
        player<Disheartened>("I'm looking for Imcando dwarves to help me.")
        npc<Disheartened>("Please try and find them quickly... I am scared Sir Vyvin will find out!")
    }

    suspend fun Player.askAboutPicture() {
        npc<Quiz>("So how are you doing getting a sword?")
        player<Happy>("I've found an Imcando dwarf but he needs a picture of the sword before he can make it.")
        npc<Confused>("A picture eh? Hmmm.... The only one I can think of is in a small portrait of Sir Vyvin's father... Sir Vyvin keeps it in a cupboard in his room I think.")
        set("the_knights_sword", "cupboard")
        player<Idle>("Ok, I'll try and get that then.")
        npc<Confused>("Please don't let him catch you! He MUSTN'T know what happened!")
    }

    suspend fun Player.checkPicture() {
        npc<Quiz>("So how are you doing getting a sword?")
        if (carriesItem("portrait")) {
            player<Happy>("I have the picture. I'll just take it to the dwarf now!")
            npc<Confused>("Please hurry!")
            return
        }
        player<Disheartened>("I didn't get the picture yet.")
        npc<Disheartened>("Please try and get it quickly... I am scared Sir Vyvin will find out!")
    }

    suspend fun Player.bluriteSword() {
        if (equipment.contains("blurite_sword")) {
            player<Happy>("I have retrieved your sword for you.")
            npc<Confused>("So can you un-equip it and hand it over to me now please?")
            return
        }
        if (carriesItem("blurite_sword")) {
            player<Happy>("I have retrieved your sword for you.")
            npc<Happy>("Thank you, thank you, thank you! I was seriously worried I would have to own up to Sir Vyvin!")
            statement("You give the sword to the squire.")
            inventory.remove("blurite_sword")
            questComplete()
            return
        }
        if (ownsItem("blurite_sword")) {
            player<Idle>("I got a replacement sword made.")
            npc<Happy>("Thank you! Can I have it?")
            player<Idle>("I've got it stored safely.")
            npc<Frustrated>("Well could you go and get it for me then please? Quickly?")
            player<Idle>("Yeah, okay.")
            return
        }
        npc<Quiz>("So how are you doing getting a sword?")
        player<Happy>("I've found a dwarf who will make the sword. I've just got to find the materials for it now!")
        npc<Confused>("Please hurry!")
    }

    fun ChoiceOption.lifeAsASquire() = option<Quiz>("And how is life as a squire?") {
        npc<Disheartened>("Well, Sir Vyvin is a good guy to work for, however, I'm in a spot of trouble today. I've gone and lost Sir Vyvin's sword!")
        choice {
            whereYouLostIt()
            newSword()
            heAngry()
        }
    }

    fun ChoiceOption.whereYouLostIt() = option<Quiz>("Do you know where you lost it?") {
        npc<Confused>("Well now, if I knew THAT it wouldn't be lost, now would it?")
        choice {
            vagueArea()
            newSword()
            abundantWithSwords()
            heAngry()
        }
    }

    fun ChoiceOption.vagueArea() = option<Quiz>("Well, do you know the VAGUE AREA you lost it in?") {
        npc<Disheartened>("No. I was carrying it for him all the way from where he had it stored in Lumbridge. It must have slipped from my pack during the trip, and you know what people are like these days...")
        npc<Disheartened>("Someone will have just picked it up and kept it for themselves.")
        choice {
            newSword()
            abundantWithSwords()
            hopeYouFind()
        }
    }

    fun ChoiceOption.abundantWithSwords() = option<Idle>("Well the kingdom is fairly abundant with swords...") {
        npc<Disheartened>("Yes. You can get bronze swords anywhere. But THIS isn't any old sword.")
        heirloom()
    }

    suspend fun Player.heirloom() {
        npc<Disheartened>("The thing is, this sword is a family heirloom. It has been passed down through Vyvin's family for five generations! It was originally made by the Imcando dwarves, who were")
        npc<Disheartened>("a particularly skilled tribe of dwarven smiths. I doubt anyone could make it in the style they do.")
        choice {
            anotherSword()
            hopeYouFind()
        }
    }

    fun ChoiceOption.anotherSword() = option<Quiz>("So would these dwarves make another one?") {
        npc<Disheartened>("I'm not a hundred percent sure the Imcando tribe exists anymore. I should think Reldo, the palace librarian in Varrock, will know; he has done a lot of research on the races of Gielinor.")
        npc<Quiz>("I don't suppose you could try and track down the Imcando dwarves for me? I've got so much work to do...")
        startQuest()
    }

    fun ChoiceOption.hopeYouFind() = option<Idle>("Well, I hope you find it soon.") {
        npc<Disheartened>("Yes, me too. I'm not looking forward to telling Vyvin I've lost it. He's going to want it for the parade next week as well.")
    }

    fun ChoiceOption.newSword() = option<Idle>("I can make a new sword if you like...") {
        npc<Disheartened>("Thanks for the offer. I'd be surprised if you could though.")
        heirloom()
    }

    fun ChoiceOption.heAngry() = option<Quiz>("Is he angry?") {
        npc<Disheartened>("He doesn't know yet. I was hoping I could think of something to do before he does find out, But I find myself at a loss.")
        choice {
            vagueArea()
            newSword()
            abundantWithSwords()
            hopeYouFind()
        }
    }

    fun ChoiceOption.squireForMe() = option<Quiz>("Wouldn't you prefer to be a squire for me?") {
        npc<Idle>("No, sorry, I'm loyal to Sir Vyvin.")
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
                player<Idle>("Ok, I'll give it a go.")
                npc<Happy>("Thank you very much! As I say, the best place to start should be with Reldo...")
            }
            option("No.") {
                npc<Disheartened>("Oh man... I'm in such trouble...")
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
