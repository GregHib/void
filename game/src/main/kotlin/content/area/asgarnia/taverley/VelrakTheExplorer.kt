package content.area.asgarnia.taverley

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script

class VelrakTheExplorer : Script {
    init {
        npcOperate("Talk-to", "velrak_the_explorer") {
            if (ownsItem("dusty_key")) {
                player<Quiz>("Are you still here?")
                npc<Sad>("Yes... I'm still plucking up the courage to run out past those Black Knights..")
                return@npcOperate
            }
            npc<Happy>("Thank you for rescuing me! It isn't very comfy in this cell!")
            choice {
                option<Quiz>("So... do you know anywhere good to explore?") {
                    npc<Sad>("Well, this dungeon was quite good to explore ...until I got captured, anyway. I was given a key to an inner part of this dungeon by a mysterious cloaked stranger!")
                    npc<Sad>("It's rather tough for me to get that far into the dungeon however... I just keep getting captured! Would you like to give it a go?")
                    choice {
                        option<Happy>("Yes please!") {
                            addOrDrop("dusty_key")
                            item("dusty_key", "Velrak reaches somewhere mysterious and passes you a key.")
                        }
                        option<Confused>("No, it's too dangerous for me too.") {
                            npc<Neutral>("I don't blame you!")
                        }
                    }
                }
                option("Do I get a reward?") {
                    player<Quiz>("Do I get a reward? For freeing you and all...")
                    npc<Sad>("Well... not really... The Black Knights took all of my stuff before throwing me in here to rot!")
                }
            }
        }
    }
}
