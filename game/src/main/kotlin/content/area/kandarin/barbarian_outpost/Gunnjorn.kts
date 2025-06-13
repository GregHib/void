package content.area.kandarin.barbarian_outpost

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

npcOperate("Talk-to", "gunnjorn") {
    choice("What do you want to say?") {
        option<Quiz>("Hey there. What is this place?") {
            npc<Happy>("Aha! Welcome to my obstacle course. Have fun, but remember this isn't a children's playground. People have died here.")
            npc<Talk>("This course starts at the ropeswings to the east. When you've done the swing, head across the slippery log to the building. When you've traversed the obstacles inside, you'll come out the other side.")
            npc<Talk>("From there, head across the low walls to finish. If you've done all the obstacles as I've described, and in order, you'll get a lap bonus.")
        }
        option<Quiz>("What's wrong with the wall after the log balance?") {
            npc<Talk>("The wall after the log balance? Nothing, really. I just put some tough material on it, giving people something to grip hold of.")
            player<Quiz>("Why would you do that?")
            npc<Talk>("So people like you can have a tougher route round this course.")
            npc<Talk>("Me and a mate got together and set up a new challenge that only the truly agile will conquer.")
            npc<Talk>("The extra stuff starts at that wall; so, if you think you're up to it, I suggest you scramble up there after the log balance.")
            player<Quiz>("Sounds interesting. Anything else I should know?")
            npc<Talk>("Nothing, really. Just make sure you complete the other obstacles before ya do. If you finish a full lap, you'll get an increased bonus for doing the tougher route.")
            npc<Talk>("If you manage to do 250 laps of this advanced route without a single mistake, I'll let you have a special item. I'll keep track of your lap tallies, so you can check how you're getting on with me any time.")
        }
        option<Quiz>("Can I talk about rewards?", filter = { player["barbarian_course_advanced_laps", 0] > 0 }) {
            if (player.containsVarbit("agility_course_rewards_claimed", "agile_top")) {
                npc<Talk>("Of course. How can I help?")
                player<Quiz>("Any chance of another Agile top?")
                if (player.inventory.add("agile_top")) {
                    npc<Talk>("Here you go.")
                } else {
                    player.inventoryFull() // TODO correct message
                }
            } else if (player["gnome_course_advanced_laps", 0] >= 250) {
                npc<Happy>("Sure, and congratulations, Player! That took dedication and great dexterity to complete that many laps.")
                npc<Talk>("As promised, I'll give you an item you may find useful - an Agile top. You'll find yourself lighter than usual while wearing it.")
                npc<Talk>("We barbarians are tough folks, as you know, so it'll even keep you safe if you get drawn into combat.")
                if (player.inventory.add("agile_top")) {
                    player.addVarbit("agility_course_rewards_claimed", "agile_top")
                    npc<Happy>("There you go. Enjoy!")
                } else {
                    player.inventoryFull() // TODO correct message
                }
            } else {
                npc<Talk>("There's no reward for you just yet. Your lap count is only ${player["barbarian_course_advanced_laps", 0]}. It's 250 successful laps or no reward.")
            }
        }
        option<Talk>("That's all I need for now. Bye.") {
            npc<Talk>("Bye for now. Come back if you need any help.")
        }
    }
}
