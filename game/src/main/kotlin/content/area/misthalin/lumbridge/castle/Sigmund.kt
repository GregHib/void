package content.area.misthalin.lumbridge.castle

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class Sigmund : Script {

    init {
        npcOperate("Talk-to", "sigmund") {
            npc<Neutral>("Can I help you?")
            choice {
                option<Quiz>("Do you have any quests for me?") {
                    npc<Neutral>("I hear the Duke has a task for an adventurer. Otherwise, if you want to make yourself useful, there are always evil monsters to slay.")
                    player<Neutral>("Okay, I might just do that.")
                }
                option<Quiz>("Who are you?") {
                    npc<Neutral>("I'm the Duke's advisor.")
                    player<Quiz>("Can you give me any advice then?")
                    npc<Neutral>("I only advice the Duke. But if you want to make yourself useful, there are evil goblins to slay on the other side of the river.")
                }
            }
        }

        npcOperate("Pickpocket", "sigmund") {
            message("Sigmund doesn't seem to have anything of value.")
        }
    }
}
