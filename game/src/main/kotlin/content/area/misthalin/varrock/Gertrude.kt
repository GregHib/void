package content.area.misthalin.varrock

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.member.gertrudes_cat.GERTRUDES_CAT_STRING_NAME
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.longQueue

/**
 * Minimal Gertrude dialogue. Once Gertrude's Cat quest is completed, lets the
 * player request a replacement kitten (the canonical post-quest pet acquisition
 * path). The quest itself is not yet implemented in Void; this dialogue is
 * inert until the quest sets `quest("gertrudes_cat")` to `"completed"`.
 */
class Gertrude : Script {

    init {
        npcOperate("Talk-to", "gertrude") {
            if (get(FLUFFS_MILK_VAR, false) && get(FLUFFS_FED_VAR, false) && quest("gertrudes_cat") == "attempt_fluffs_pickup") {
                mustBeAReason()
                return@npcOperate
            }
            when (quest("gertrudes_cat")) {
                "completed" -> postQuest()
                "spoke_to_gertrude" -> lookingForInformation()
                "found_the_boys" -> findingFluffs()
                "found_fluffs" -> hungryAndThirsty()
                "attempt_fluffs_pickup" -> hungryAndThirsty()
                "fluffs_returned" -> finishQuest()
                else -> unstarted()
            }
        }
    }

    private suspend fun Player.finishQuest() {
        if (!get("gertrudes_cat_talked_about_reward", false)) {
            player<Happy>("Hello, Gertrude. Fluffs had run off with her kittens, lost them and I have now returned them to her.")
            statement("Gertrude thanks you heartily")
            npc<Happy>("Thank you! If you hadn't found her kittens then they would have died out there. I've got some presents for you in thanks for your help.")
            player<Happy>("That's okay, I like to do my bit.")
            set("gertrudes_cat_talked_about_reward", true)
        } else {
            player<Neutral>("Hello again. You said something about presents...")
        }
        if (inventory.spaces < 3) {
            npc<Neutral>("You don't have space to hold all three of my presents. Come back and talk to me again when you do.")
            return
        }
        npc<Happy>("I have no real material possessions but I do have kittens. I've cooked you some food too.")
        player<Neutral>("Well if one needs a home.")
        npc<Happy>("I would sell one to my cousin in West Ardougne. I hear there's a rat epidemic there but it's too far for me to travel, what with my boys and all.")
        npc<Happy>("Here you go. Look after her and thank you again.")
        npc<Neutral>("Oh, by the way, the kitten can live in your backpack but, to ensure it grows, you must take it out, feed it and stroke it often.")
        statement("Gertrude gives you a kitten.")
        questComplete()
    }

    suspend fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "gertrudes_cat")
        // AdventurersLogs.questCompleted(this, "the_restless_ghost", points = 1) // Unknown if needed
        set(GERTRUDES_CAT_STRING_NAME, "completed")
        jingle("quest_complete_1")
        exp(Skill.Cooking, 1525.0)
        giveKitten()
        inventory.add("chocolate_cake")
        inventory.add("stew")
        refreshQuestJournal()
        inc("quest_points")
        longQueue("quest_complete", 1) {
            questComplete(
                "Gertrude's Cat",
                "1 Quest Point",
                "A kitten!",
                "1,525 Cooking XP",
                "A chocolate cake",
                "A bowl of stew",
                "The ability to raise cats",
                item = "pet_cat", // TODO: Find cat id
            )
        }
    }

    private suspend fun Player.mustBeAReason() {
        player<Happy>("Hi!")
        npc<Quiz>("Hey traveller, did Fluffs eat the sardines?")
        player<Neutral>("Yeah, she loved them, but she still won't leave.")
        npc<Quiz>("Well that is strange, there must be a reason.")
    }

    private suspend fun Player.hungryAndThirsty() {
        findShilopQ()
        player<Happy>("Yes, I've found Fluffs!")
        npc<Happy>("That's great; where is she?")
        player<Quiz>("She's still in Varrock. I think she may be hungry, thirsty or both.")
        npc<Disheartened>("Oh dear, oh dear! Maybe she's just hungry. She loves doogle sardines but I'm all out.")
        player<Quiz>("Doogle sardines?")
        npc<Neutral>("Yes, raw sardines seasoned with doogle leaves. Unfortunately, I've used all my doogles leaves, but you may find some on the bush out back.")
        player<Quiz>("What if she is thirsty?")
        npc<Neutral>("In that case, she'd probably like some milk. A bucketful would tempt her, I'm sure.")
        player<Neutral>("It seems a rather large amount of milk for one small cat, but I'll give it a try.")
    }

    private suspend fun Player.findingFluffs() {
        findShilopQ()
        player<Neutral>("I think so. I'm just going to look now.")
        npc<Happy>("Thanks again, adventurer.")
    }

    private suspend fun Player.findShilopQ() {
        player<Neutral>("Hello Gertrude.")
        npc<Neutral>("Hello again, did you manage to find Shilop? I can't keep an eye on him for the life of me.")
        player<Neutral>("He does seem quite a handful.")
        npc<Laugh>("You have no idea! Did he help at all?")
    }
    private suspend fun Player.postQuest() {
        npc<Happy>("Hello dear. How are my kittens treating you?")
        choice {
            option<Quiz>("Could I have another kitten, please?") {
                giveKitten()
            }
            option<Happy>("Just fine, thank you.") {
                npc<Happy>("Wonderful. Do come back if you'd like another one.")
            }
        }
    }

    private suspend fun Player.giveKitten() {
        if (inventory.contains("pet_kitten")) {
            npc<Sad>("It looks like you've already got a kitten with you. Come back when you'd like another.")
            return
        }
        if (get("pet_active_item", "") == "pet_kitten") {
            npc<Sad>("You've already got one of my kittens following you about. Look after that one first.")
            return
        }
        if (!inventory.add("pet_kitten")) {
            npc<Sad>("Come back when you've got room in your backpack, dear.")
            return
        }
        npc<Happy>("Here you go - take good care of her!")
    }

    private suspend fun Player.unstarted() {
        player<Quiz>("Hello, are you okay?")
        npc<Angry>("Do I look okay? Those kids drive me crazy.")
        npc<Sad>("I'm sorry. It's just that I've lost her.")
        player<Quiz>("Lost whom?")
        npc<Sad>("Fluffs, poor Fluffs. She never hurt anyone.")
        player<Quiz>("Who's Fluffs?")
        npc<Sad>("My beloved feline friend, Fluffs. She's been purring by my side for almost a decade. Please, could you go and search for her while I take care of the children?")
        choice {
            option<Idle>("Well, I suppose I could, though I'd need more details.") {
                questAccepted()
            }
            option<Quiz>("What's in it for me?") {
                npc<Sad>("I'm sorry, I'm too poor to pay you anything, the best I could offer is a warm meal.")
                npc<Quiz>("So, can you help?")
                player<Quiz>("Just a meal? It's not the best offer I've had, but I suppose I can help.")
                npc<Happy>("I suppose I could give you some nice, yummy chocolate cake; maybe even a kitten too, if you seem like a nice sort.")
                npc<Quiz>("Is that something you could be persuaded with?")
                choice {
                    option<Neutral>("Well, I suppose I could, though I'd need more details.") {
                        questAccepted()
                    }
                    option<Neutral>("Sorry, I'm too busy to play pet rescue.") {
                        questRejected()
                    }
                }
            }
            option<Neutral>("Sorry, I'm too busy to play pet rescue.") {
                questRejected()
            }
        }
    }
    private suspend fun Player.questRejected() {
        npc<Sad>("Well, okay then. I'll have to find someone else; someone less heartless. It will be on your conscience if a poor kitty is lost in the wilds, though.")
    }
    private suspend fun Player.questAccepted() {
        set(GERTRUDES_CAT_STRING_NAME, "spoke_to_gertrude")
        npc<Happy>("Really? Thank you so much! I really have no idea where she could be!")
        npc<Neutral>("I think my sons, Shilop and Wilough, saw the cat last. They'll be out in the marketplace.")
        player<Quiz>("The marketplace? Which one would that be? It would help to know what they get up to, as well.")
        npc<Happy>("Really? Well, I generally let them do what they want, so I've no idea exactly what they would be doing. They are good lads, though. I'm sure they are just watching the passers-by in Varrock Marketplace.")
        npc<Happy>("Oh, to be young and carefree again!")
        player<Happy>("Alright then. I'll see what I can do. Two young lads in Varrock Marketplace; I can only hope that there's no school trip passing through when I arrive.")
    }

    private suspend fun Player.lookingForInformation() {
        player<Neutral>("Hello Gertrude.")
        npc<Sad>("Have you seen my poor Fluffs?")
        player<Sad>("I'm afraid not.")
        npc<Quiz>("What about Shilop?")
        player<Sad>("No sign of him either.")
        npc<Confused>("Hmm, strange; he should be in Varrock Marketplace.")
    }
}
