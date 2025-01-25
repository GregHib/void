package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

npcOperate("Talk-to", "gnome_trainer") {
    when (random.nextInt(2)) {
        0 -> npc<NeutralOld>("This is training, soldier. Little time for chat! What do you want?")
        1 -> npc<NeutralOld>("This is a serious training area. What do you want?")
        2 -> npc<NeutralOld>("This isn't a grannies' tea party. What do you want?")
    }
    choice("What do you want to say?") {
        option<Quiz>("What is this place?") {
            npc<NeutralOld>("This, my friend, is where we train and improve our Agility. It's an essential skill.")
            player<Talk>("It looks easy enough.")
            npc<NeutralOld>("If you complete the course, from the slippery log to the end, your Agility will increase more rapidly than by repeating just one obstacle.")
        }
        option<Quiz>("What's so special about this course, then?") {
            npc<NeutralOld>("Well, it's where most people tend to start training. We've also made an extension for those who are up for the challenge.")
            player<Quiz>("An extension?")
            if (player.has(Skill.Agility, 85)) {
                npc<NeutralOld>("Well, you look like you can handle it, so I'll fill you in. If you follow the course as normal, you'll see a new branch before the balancing rope.")
                npc<NeutralOld>("This is the way up to the new route. It's taken a while to strengthen the branch, but you should find yourself agile enough to get up to the next level. I suggest you check it out.")
                player<Quiz>("I might just do that. Anything else I need to know?")
                npc<NeutralOld>("Yes. If you do the tougher route, you'll get a much larger experience bonus at the end of it - just make sure you've completed all the obstacles leading up to the branch!")
                npc<NeutralOld>("Oh, I nearly forgot: if you manage to do 250 laps of the advanced route without a fault, we'll give you a special item. We'll keep a tally of your laps, so you can always check your progress.")
            } else {
                npc<NeutralOld>("It's a challenge that I think is a bit out of your depth. I'll give you more information when you're slightly more experienced.")
                player.message("You need an Agility level of 85 to attempt the improved gnome course.")
            }
        }
        option<Quiz>("Can I talk about rewards?", filter = { player["gnome_course_advanced_laps", 0] > 0 }) {
            if (player.containsVarbit("agility_course_rewards_claimed", "agile_legs")) {
                npc<QuizOld>("Of course. How can I help?")
                player<Quiz>("Any chance of some more Agile legs?")
                if (player.inventory.add("agile_legs")) {
                    npc<NeutralOld>("Here you go, try not to lose them.")
                } else {
                    player.inventoryFull() // TODO correct message
                }
            } else if (player["gnome_course_advanced_laps", 0] >= 250) {
                npc<HappyOld>("Well, it looks like you've completed our challenge! Take this as a reward: some Agile legs.")
                npc<HappyOld>("You'll find yourself much lighter than usual while wearing them. They are made from the toughest material we gnomes could find, so it might even protect you in combat.")
                if (player.inventory.add("agile_legs")) {
                    player.addVarbit("agility_course_rewards_claimed", "agile_legs")
                    npc<HappyOld>("There you go. Enjoy!")
                } else {
                    player.inventoryFull() // TODO correct message
                }
            } else {
                npc<NeutralOld>("Well, you've still got work to do. Your lap count is ${player["gnome_course_advanced_laps", 0]}. It's 250 successful laps for the reward!")
            }
        }
        option<Talk>("I'm done for now. Bye.") {
            npc<NeutralOld>("Bye for now. Come back if you need any assistance.")
        }
    }
}