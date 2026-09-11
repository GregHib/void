package content.minigame.mage_training_arena

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Asleep
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

/**
 * The wizards on the arena's top floor and in its basement. Their Rune Mechanics quest is not
 * implemented, so these are their pre-quest conversations only.
 */
class MageTrainingArenaWizards : Script {

    init {
        npcOperate("Talk-to", "wizard_shug") {
            player<Quiz>("Hello?")
            npc<Asleep>("Zzz zzz zzzzz")
            player<Neutral>("He's out like a light.")
        }

        npcOperate("Talk-to", "apprentice_clerval") {
            player<Neutral>("Hello.")
            npc<Quiz>("Hello?")
            player<Quiz>("What are you doing down here?")
            npc<Neutral>("Well, I'm supposed to be training my magic, but I got a bit...distracted. I came down here to think.")
            player<Quiz>("What are you thinking about?")
            npc<Neutral>("Rune guardians! Aren't they just fascinating? I was speaking to one earlier and now I just can't stop thinking about them.")
            player<Quiz>("What exactly is so fascinating about them?")
            npc<Neutral>("Can't you see it? They're walking, talking ROCKS!")
            npc<Neutral>("I've been pondering how I could make my own rune guardian, then I would always have a friend with me. I just don't know how to go about doing it.")
            player<Neutral>("Well, why don't you TALK to the wizards?")
            npc<Angry>("NO! They'd yell at me and tell me to get back to the training rooms. I'm terrible at casting spells under duress. I get nervous and end up casting spells on myself.")
        }

        npcOperate("Talk-to", "wizard_dougal") {
            player<Neutral>("Hello.")
            npc<Angry>("Not another one. Don't you useless apprentices have anything better to do than bother me? First that bumbling Clerval and now you.")
        }

        npcOperate("Talk-to", "wizard_edvin") {
            player<Neutral>("Hello.")
            npc<Neutral>("Hmm, it's still just not right. I think I've been working on it too long.")
            npc<Neutral>("I got rid of the lava...so that should make it safer. No one's gotten trapped between teleports since I fixed the abyssal quadrangle algorithm.")
        }
    }
}
