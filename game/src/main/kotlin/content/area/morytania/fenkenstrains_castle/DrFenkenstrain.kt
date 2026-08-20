package content.area.morytania.fenkenstrains_castle

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class DrFenkenstrain : Script {

    init {
        npcOperate("Talk-to", "dr_fenkenstrain*") {
            when (quest("creature_of_fenkenstrain")) {
                "unstarted" -> introInterview()
                "body_parts" -> bodyPartsDelivery()
                "sewing" -> needleThreadDelivery()
                "conductor" -> conductorInstructions()
                "creature_alive" -> creatureRampage()
                "creature_loose" -> awaitingDestruction()
                "creature_convinced" -> creatureDestroyedThreat()
                "completed" -> {
                    npc<Scared>("theyrecomingtogetme theyrecomingtogetme...")
                    postQuestChat()
                }
            }
        }

        npcOperate("Pickpocket", "dr_fenkenstrain*") {
            pickpocket()
        }
    }

    private suspend fun Player.introInterview() {
        npc<Neutral>("Have you come to apply for the job?")
        choice {
            option("Yes.") { acceptJob() }
            option("No.") {
                player<Neutral>("No thanks, I wouldn't work here if you paid me.")
                npc<Neutral>("Charming.")
            }
        }
    }

    private suspend fun Player.acceptJob() {
        player<Neutral>("Yes, if it pays well.")
        npc<Neutral>("I'll have to ask you some questions first.")
        player<Neutral>("Okay...")
        npc<Quiz>("How would you describe yourself in one word?")
        choice {
            option("Stunning") { rejected() }
            option("Awe-inspiring") { rejected() }
            option("Breathtaking") { rejected() }
            option("Braindead") {
                npc<Neutral>("Mmmm, I see.")
                npc<Quiz>("Just one more question. What would you say is your greatest skill?")
                choice {
                    option("Combat") { rejected() }
                    option("Magic") { rejected() }
                    option("Cooking") { rejected() }
                    option("Grave-digging") { offerJob() }
                }
            }
        }
    }

    private suspend fun Player.rejected() {
        npc<Neutral>("Mmmm, I see.")
        npc<Neutral>("I'm sorry, but I don't think you have the aptitude for this position.")
    }

    private suspend fun Player.offerJob() {
        set("creature_of_fenkenstrain", "body_parts")
        npc<Neutral>("Mmmm, I see.")
        npc<Happy>("Looks like you're just the ${if (male) "man" else "woman"} for the job! Welcome aboard!")
        player<Quiz>("Is there anything you'd like me to do for you, sir?")
        npc<Neutral>("Yes, there is. You're highly skilled at grave-digging, yes?")
        player<Shifty>("Er...yes, that's what I said.")
        npc<Neutral>("Excellent. Now listen carefully.")
        npc<Shifty>("I need you to find some... stuff...for me.")
        player<Quiz>("Stuff?")
        npc<Neutral>("That's what I said...stuff.")
        player<Quiz>("What kind of stuff?")
        npc<Neutral>("Well...dead stuff.")
        player<Neutral>("Go on...")
        npc<Neutral>("I need you to get me enough dead body parts for me to stitch together a complete body, which I plan to bring to life.")
        player<Scared>("Right...okay...if you insist.")
    }

    private suspend fun Player.bodyPartsDelivery() {
        if (allPartsFound()) {
            sewingTransition()
            return
        }
        val hasAnyPart = (inventory.contains("fenk_arms") && !get("fenk_arms", false)) ||
            (inventory.contains("fenk_legs") && !get("fenk_legs", false)) ||
            (inventory.contains("fenk_torso") && !get("fenk_torso", false)) ||
            (inventory.contains("fenk_head_full") && !get("fenk_head", false))
        choice {
            if (hasAnyPart) {
                option<Neutral>("I have some body parts for you.") { handOverParts() }
            } else {
                option<Quiz>("Do you know where I could find body parts?") { whereToFindParts() }
            }
            option<Quiz>("Remind me what you want me to do.") { remindMission() }
            option<Quiz>("Why are you trying to make this creature?") { whyCreatureBackstory() }
            option<Quiz>("Will his creature put me out of a job?") {
                npc<Neutral>("No, my friend. I have a very special purpose in mind for this creature.")
            }
            option<Neutral>("I must get back to work, sir.")
        }
    }

    private suspend fun Player.handOverParts() {
        if (inventory.contains("fenk_arms") && !get("fenk_arms", false)) {
            inventory.remove("fenk_arms")
            set("fenk_arms", true)
            npc<Happy>("Great you've brought me some arms.")
        }
        if (inventory.contains("fenk_legs") && !get("fenk_legs", false)) {
            inventory.remove("fenk_legs")
            set("fenk_legs", true)
            npc<Happy>("Excellent you've brought me some legs.")
        }
        if (inventory.contains("fenk_torso") && !get("fenk_torso", false)) {
            inventory.remove("fenk_torso")
            set("fenk_torso", true)
            npc<Happy>("Splendid you've brought me a torso.")
        }
        if (inventory.contains("fenk_head_full") && !get("fenk_head", false)) {
            inventory.remove("fenk_head_full")
            set("fenk_head", true)
            npc<Happy>("Fantastic you've brought me a head.")
        }
        if (allPartsFound()) {
            sewingTransition()
        }
    }

    private suspend fun Player.sewingTransition() {
        set("creature_of_fenkenstrain", "sewing")
        npc<Happy>("Superb!! Those are all the parts I need. Now to sew them together ...")
        npc<Neutral>("Oh bother! I haven't got a needle or thread!")
        npc<Neutral>("Go and get me a needle, and I'll need 5 lots of thread.")
    }

    private fun Player.allPartsFound(): Boolean = get("fenk_arms", false) &&
        get("fenk_legs", false) &&
        get("fenk_torso", false) &&
        get("fenk_head", false)

    private suspend fun Player.whereToFindParts() {
        npc<Neutral>("The soil of Morytania is unique in its ability to preserve the bodies of the dead, which is one reason why I have chosen to carry out my experiments here.")
        npc<Neutral>("I recommend digging up some graves in the local area. To the south-east you will find the Haunted Woods; I believe there are many graves there.")
        npc<Neutral>("There is also a mausoleum on an island west of this castle. I expect the bodies that are buried there to be extremely well preserved, as they were wealthy in life.")
    }

    private suspend fun Player.remindMission() {
        npc<Neutral>("I need you to get me enough dead body parts for me to stitch together a complete body, which I plan to bring to life.")
        player<Scared>("Right...okay...if you insist.")
    }

    private suspend fun Player.whyCreatureBackstory() {
        npc<Neutral>("I came to the land of Morytania many years ago, to find a safe sanctuary for my experiments. This abandoned castle suited my purposes exactly.")
        player<Quiz>("What were you experimenting in?")
        npc<Neutral>("Oh, perfectly innocent experiments - for the good of mankind.")
        player<Quiz>("Then why did you need to come to Morytania?")
        npc<Neutral>("Enough questions, now. Get back to your work.")
    }

    private suspend fun Player.needleThreadDelivery() {
        val needleGiven = get("fenk_needle", false)
        val threadCount = get("fenk_threads_given", 0)

        if (needleGiven && threadCount == 5) {
            sewLifeFromLightning()
            return
        }
        when {
            !needleGiven && threadCount < 5 -> npc<Quiz>("Where are my needle and thread, $name?")
            threadCount < 5 -> npc<Quiz>("Where's my thread, $name?")
            else -> npc<Quiz>("Where's my needle, $name?")
        }
        if (!needleGiven && inventory.contains("needle")) {
            inventory.remove("needle")
            set("fenk_needle", true)
            npc<Happy>("Ah, a needle. Wonderful.")
        }
        val carryThread = get("fenk_threads_given", 0)
        if (carryThread < 5 && inventory.contains("thread")) {
            val give = minOf(inventory.count("thread"), 5 - carryThread)
            inventory.remove("thread", give)
            set("fenk_threads_given", carryThread + give)
            npc<Happy>("Some thread, excellent.")
        }
        if (get("fenk_needle", false) && get("fenk_threads_given", 0) == 5) {
            sewLifeFromLightning()
        }
    }

    private suspend fun Player.sewLifeFromLightning() {
        set("creature_of_fenkenstrain", "conductor")
        statement("Fenkenstrain uses the needle and thread to sew the body parts together. Soon, a hideous creature lies inanimate on the ritual table.")
        npc<Neutral>("Perfect. But I need one more thing from you - flesh and bones by themselves do not make life.")
        player<Quiz>("Really?")
        npc<Neutral>("I have honed to perfection an ancient ritual that will give life to this creature, but for this I must harness the very power of Nature.")
        player<Quiz>("And what power is this?")
        npc<Neutral>("The power of lightning.")
        player<Confused>("Sorry, can't make lightning, you've got the wrong ${if (male) "man" else "woman"}-")
        npc<Neutral>("Silence your insolent tongue! The storm that brews overhead will create the lightning. What I need you to do is to repair the lightning conductor on the balcony above.")
        player<Confused>("Repair the lightning conductor, right. Can I have a break, soon? By law I'm entitled to 15 minutes every-")
        npc<Angry>("Repair the conductor and BEGONE!!!")
    }

    private suspend fun Player.conductorInstructions() {
        player<Quiz>("How do I repair the lightning conductor?")
        npc<Angry>("Oh, it would be easier to do it myself! If you find a conductor mould you should be able to cast a new one.")
        npc<Neutral>("Remember this, $name: my experiment will only work with a conductor made from silver.")
    }

    private suspend fun Player.creatureRampage() {
        player<Quiz>("So did it work, then?")
        npc<Scared>("Yes, I'm afraid it did, $name - all too well.")
        player<Confused>("I can't see it anywhere.")
        npc<Neutral>("I tricked it into going up to the Tower, and there it remains, imprisoned.")
        player<Quiz>("So the creature wasn't all you'd hoped, then?")
        npc<Sad>("...oh, what have I done...")
        player<Quiz>("Oh, I see, we're developing a sense of right and wrong now are we?")
        player<Sad>("Bit late for that, I'd say.")
        npc<Scared>("I have no control over it! It's coming to get me!")
        player<Quiz>("What do you want me to do about it?")
        set("creature_of_fenkenstrain", "creature_loose")
        npc<Scared>("Destroy it!!! Take the key to the Tower and take back the life I never should have granted!!!")
        addOrDrop("fenk_tower_key")
    }

    private suspend fun Player.awaitingDestruction() {
        npc<Quiz>("So have you destroyed it?!!?")
        if (!inventory.contains("fenk_tower_key") && !get("fenk_unlocked_tower", false)) {
            player<Sad>("I seem to have lost the Tower key.")
            addOrDrop("fenk_tower_key")
            npc<Angry>("Oh, for goodness sake. Here's a copy I had made.")
            return
        }
        player<Neutral>("Not yet.")
        npc<Scared>("Please, hurry - save me!!!!")
    }

    private suspend fun Player.creatureDestroyedThreat() {
        npc<Quiz>("So have you destroyed it?!!?")
        player<Angry>("Never, now that he has told me the truth!")
        npc<Scared>("Oh my, oh my, this is exactly what I feared!")
        npc<Scared>("Why did you have to pick Rologarth's brain of all brains?!?")
        player<Angry>("I'm through working for you.")
        npc<Angry>("No!! I refuse to release you!! You must help me build another creature to destroy this dreadful mistake!!")
        postQuestChat()
    }

    private suspend fun Player.postQuestChat() {
        player<Angry>("It is all you deserve. Lord Rologarth is master of this castle once more. Let him protect you - if he wants to.")
        npc<Scared>("theyrecomingtogetme theyrecomingtogetme...")
    }

    private suspend fun Player.pickpocket() {
        if (!hasMax(Skill.Thieving, 25)) {
            statement("You need level 25 Thieving to pickpocket Fenkenstrain.")
            return
        }
        if (quest("creature_of_fenkenstrain") !in setOf("creature_convinced", "completed") || inventory.contains("ring_of_charos")) {
            npc<Quiz>("What do you think you're doing???")
            return
        }

        anim("pick_pocket")
        message("You steal the Ring of Charos from Fenkenstrain.")

        if (questCompleted("creature_of_fenkenstrain")) {
            addOrDrop("ring_of_charos")
            return
        }
        sendQuestReward()
    }

    private fun Player.sendQuestReward() {
        jingle("quest_complete_1")
        exp(Skill.Thieving, 1000.0)
        addOrDrop("ring_of_charos")
        inc("quest_points", 2)
        AuditLog.event(this, "quest_completed", "creature_of_fenkenstrain")
        set("creature_of_fenkenstrain", "completed")
        resetQuestVarbits()
        refreshQuestJournal()
        questComplete(
            "Creature of Fenkenstrain",
            "2 Quest Points",
            "Ring of Charos",
            "1000 Thieving XP",
            item = "ring_of_charos",
        )
    }

    private fun Player.resetQuestVarbits() {
        clear("fenk_arms")
        clear("fenk_legs")
        clear("fenk_torso")
        clear("fenk_head")
        clear("fenk_needle")
        clear("fenk_threads_given")
        clear("fenk_coffin")
        clear("fenk_spoken_to_gardener")
        clear("fenk_wound_clock")
        clear("fenk_unlocked_tower")
        clear("fenk_unlocked_cavern")
        clear("fenk_unlocked_shed")
        clear("fenk_read_signpost")
    }
}
