package content.area.kandarin.tree_gnome_stronghold

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class GnomeTrainer : Script {

    init {
        npcOperate("Talk-to", "gnome_trainer") {
            when (random.nextInt(2)) {
                0 -> npc<Neutral>("This is training, soldier. Little time for chat! What do you want?")
                1 -> npc<Neutral>("This is a serious training area. What do you want?")
                2 -> npc<Neutral>("This isn't a grannies' tea party. What do you want?")
            }
            choice("What do you want to say?") {
                option<Quiz>("What is this place?") {
                    npc<Neutral>("This, my friend, is where we train and improve our Agility. It's an essential skill.")
                    player<Talk>("It looks easy enough.")
                    npc<Neutral>("If you complete the course, from the slippery log to the end, your Agility will increase more rapidly than by repeating just one obstacle.")
                }
                option<Quiz>("What's so special about this course, then?") {
                    npc<Neutral>("Well, it's where most people tend to start training. We've also made an extension for those who are up for the challenge.")
                    player<Quiz>("An extension?")
                    if (has(Skill.Agility, 85)) {
                        npc<Neutral>("Well, you look like you can handle it, so I'll fill you in. If you follow the course as normal, you'll see a new branch before the balancing rope.")
                        npc<Neutral>("This is the way up to the new route. It's taken a while to strengthen the branch, but you should find yourself agile enough to get up to the next level. I suggest you check it out.")
                        player<Quiz>("I might just do that. Anything else I need to know?")
                        npc<Neutral>("Yes. If you do the tougher route, you'll get a much larger experience bonus at the end of it - just make sure you've completed all the obstacles leading up to the branch!")
                        npc<Neutral>("Oh, I nearly forgot: if you manage to do 250 laps of the advanced route without a fault, we'll give you a special item. We'll keep a tally of your laps, so you can always check your progress.")
                    } else {
                        npc<Neutral>("It's a challenge that I think is a bit out of your depth. I'll give you more information when you're slightly more experienced.")
                        message("You need an Agility level of 85 to attempt the improved gnome course.")
                    }
                }
                if (get("gnome_course_advanced_laps", 0) > 0) {
                    option<Quiz>("Can I talk about rewards?") {
                        if (containsVarbit("agility_course_rewards_claimed", "agile_legs")) {
                            npc<Quiz>("Of course. How can I help?")
                            player<Quiz>("Any chance of some more Agile legs?")
                            if (inventory.add("agile_legs")) {
                                npc<Neutral>("Here you go, try not to lose them.")
                            } else {
                                inventoryFull() // TODO correct message
                            }
                        } else if (get("gnome_course_advanced_laps", 0) >= 250) {
                            npc<Happy>("Well, it looks like you've completed our challenge! Take this as a reward: some Agile legs.")
                            npc<Happy>("You'll find yourself much lighter than usual while wearing them. They are made from the toughest material we gnomes could find, so it might even protect you in combat.")
                            if (inventory.add("agile_legs")) {
                                addVarbit("agility_course_rewards_claimed", "agile_legs")
                                npc<Happy>("There you go. Enjoy!")
                            } else {
                                inventoryFull() // TODO correct message
                            }
                        } else {
                            npc<Neutral>("Well, you've still got work to do. Your lap count is ${get("gnome_course_advanced_laps", 0)}. It's 250 successful laps for the reward!")
                        }
                    }
                }
                option<Talk>("I'm done for now. Bye.") {
                    npc<Neutral>("Bye for now. Come back if you need any assistance.")
                }
            }
        }
    }
}
