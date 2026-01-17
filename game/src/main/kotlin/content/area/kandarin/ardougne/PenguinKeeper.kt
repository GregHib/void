package content.area.kandarin.ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class PenguinKeeper : Script {

    init {
        npcOperate("Talk-to", "penguin_keeper_ardougne") {
            player<Neutral>("Hello there. how are the penguins doing today?")
            npc<Happy>("They are doing fine, thanks.")
            if (has(Skill.Summoning, 30) && !ownsItem("penguin_egg")) {
                npc<Quiz>("Actually, you might be able to help me with something - if you are interested.")
                player<Quiz>("What do you mean?")
                var penguinCount = 0
                if (ownsItem("penguin_grey")) {
                    penguinCount++
                }
                if (ownsItem("penguin_brown")) {
                    penguinCount++
                }
                if (ownsItem("penguin_blue")) {
                    penguinCount++
                }
                when (penguinCount) {
                    0 -> {
                        npc<Neutral>("Well, you see, the penguins have been laying so many eggs recently that we can't afford to raise them all ourselves.")
                        npc<Neutral>("You seem to know a bit about raising animals - would you like to raise a penguin for us?")
                    }
                    1 -> {
                        // TODO proper message
                        npc<Neutral>("It would be great if you could take care of one more - would you like to raise another penguin for us?")
                    }
                    2 -> {
                        npc<Neutral>("Well, we are now taking care of an incomprehensible amount of penguins!")
                        npc<Neutral>("It would be great if you could take care of just one more - would you like to raise a final penguin for us?")
                    }
                    else -> return@npcOperate
                }
                choice {
                    option<Neutral>("Yes, of course.") {
                        npc<Happy>("Wonderful!")
                        if (!inventory.add("penguin_egg")) {
                            inventoryFull()
                            return@option
                        }
                        npc<Happy>("Here you go - this egg will hatch into a baby penguin.")
                        npc<Neutral>("They eat raw fish and aren't particularly fussy about anything, so it won't be any trouble to raise.")
                        player<Happy>("Okay, thank you very much.")
                    }
                    option<Neutral>("No thanks.") {
                        npc<Neutral>("Fair enough. The offer still stands if you change your mind.")
                    }
                }
            }
        }
    }
}
