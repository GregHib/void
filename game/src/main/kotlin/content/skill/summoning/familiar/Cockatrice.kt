package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class Cockatrice : Script {
    init {
        npcOperate("Interact", "*atrice_familiar") {
            if (equipped(EquipSlot.Shield).id == "mirror_shield") {
                npc<Neutral>("You know, I'm sensing some trust issues here.")
                player<Happy>("I'm not sure I know what you are talking about.")
                npc<Neutral>("What are you holding?")
                player<Happy>("A mirror shield.")
                npc<Neutral>("And what do those do?")
                player<Happy>("Mumblemumble...")
                npc<Neutral>("What was that?")
                player<Happy>("It protects me from your gaze attack.")
                npc<Neutral>("See! Why would you need one unless you didn't trust me?")
                player<Happy>("Who keeps demanding that we stop and have staring contests?")
                npc<Neutral>("How about we drop this and call it even?")
                player<Happy>("Fine by me.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Is this what you do for fun?")
                    player<Happy>("Sometimes. Why, what do you do for fun?")
                    npc<Neutral>("I find things and glare at them until they die!")
                    player<Happy>("Well...everyone needs a hobby, I suppose.")
                }
                1 -> {
                    npc<Neutral>("You know, I think I might train as a hypnotist.")
                    player<Happy>("Isn't that an odd profession for a cockatrice?")
                    npc<Neutral>("Not at all! I've already been practicing!")
                    player<Happy>("Oh, really? How is that going?")
                    npc<Neutral>("Not good. I tell them to look in my eyes and that they are feeling sleepy.")
                    player<Happy>("I think I can see where this is headed.")
                    npc<Neutral>("And then they just lie there and stop moving.")
                    player<Happy>("I hate being right sometimes.")
                }
                2 -> {
                    npc<Neutral>("Come on, lets have a staring contest!")
                    player<Happy>("You win!")
                    npc<Neutral>("Yay! I win again!")
                    player<Happy>("Oh, it's no contest alright.")
                }
                3 -> {
                    npc<Neutral>("You know, sometimes I don't think we're good friends.")
                    player<Happy>("What do you mean?")
                    npc<Neutral>("Well, you never make eye contact with me for a start.")
                    player<Happy>("What happened the last time someone made eye contact with you?")
                    npc<Neutral>("Oh, I petrified them really good! Ooooh...okay, point taken.")
                    player<Happy>("I'm glad we had this chat.")
                }
            }
        }
    }
}
