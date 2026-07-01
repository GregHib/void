package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class Minotaur : Script {
    init {
        npcOperate("Interact", "*_minotaur_familiar") {
            val hat = equipped(EquipSlot.Hat).id
            if (hat.contains("guthan") || hat.contains("berserker") || hat.contains("archer_helm") || hat.contains("dragon_med") || hat.contains("bandos")) {
                npc<Neutral>("...")
                player<Happy>("What?")
                npc<Neutral>("Are you having a laugh?")
                player<Happy>("I'm not sure I know what you-")
                npc<Neutral>("Listen, no-horns, you have two choices: take off the horns yourself or I'll headbutt you until they fall off.")
                player<Happy>("Yessir.")
                npc<Neutral>("Good, no-horns. Let's not have this conversation again.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("All this walking about is making me angry.")
                    player<Happy>("You seem to be quite happy about that.")
                    npc<Neutral>("Yeah! There's nothing like getting a good rage on and then working it out on some no-horns.")
                    player<Happy>("I can't say I know what you mean.")
                    npc<Neutral>("Well I didn't think a no-horns like you would get it!")
                }
                1 -> {
                    npc<Neutral>("Can you tell me why we're not fighting yet?")
                    player<Happy>("Buck up; I'll find you something to hit soon.")
                    npc<Neutral>("You'd better, no-horns, because that round head of yours is looking mighty axeable.")
                }
                2 -> {
                    npc<Neutral>("Hey, no-horns?")
                    player<Happy>("Why do you keep calling me no-horns?")
                    npc<Neutral>("Do I really have to explain that?")
                    player<Happy>("No, thinking about it, it's pretty self-evident.")
                    npc<Neutral>("Glad we're on the same page, no-horns.")
                    player<Happy>("So, what did you want?")
                    npc<Neutral>("I've forgotten, now. I'm sure it'll come to me later.")
                }
                3 -> {
                    npc<Neutral>("Hey no-horns!")
                    player<Happy>("Yes?")
                    npc<Neutral>("Oh, I don't have anything to say, I was just yelling at you.")
                    player<Happy>("Why?")
                    npc<Neutral>("No reason. I do like to mess with the no-horns, though.")
                }
            }
        }
    }
}
