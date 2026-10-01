package content.area.asgarnia.falador

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.addHouseRoom
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class EstateAgent : Script {
    init {
        npcOperate("Talk-to", "estate_agent*") {
            npc<Neutral>("Hello. Welcome to the ${Settings["server.name"]} Housing Agency! What can I do for you?")
            choice {
                if (!contains("house_location")) {
                    option<Quiz>("How can I get a house?") {
                        buyHouse()
                    }
                } else {
                    // TODO move + redecorate house
                }
                option<Quiz>("What's that cape you are wearing?") {
                    skillcapeMasterDialogue(Skill.Construction, "a master home builder")
                }
                option<Neutral>("Never mind.")
            }
        }
    }

    private suspend fun Player.buyHouse() {
        npc<Neutral>("I can sell you a starting house in Rimmington for 1000 coins. As you increase your construction skill you will be able to have your house moved to other areas and redecorated in other styles.")
        npc<Quiz>("Do you want to buy a starter house?")
        choice {
            option<Happy>("Yes please!") {
                if (!inventory.remove("coins", 1000)) {
                    player<Sad>("I haven't got 1,000 coins on me.")
                    npc<Neutral>("Well come back when you have it then.")
                    return@option
                }
                set("house_location", "rimmington")
                addHouseRoom("garden", START_ROOM)
                npc<Neutral>("Thank you. Go through the Rimmington house portal and you will find your house ready for you to start building in it.")
                npc<Neutral>("This book will help you to start building your house.")
                addOrDrop("construction_guide")
            }
            option<Neutral>("No thanks.") {
                npc<Neutral>("Well enjoy your player-owned cardboard box or wherever you're going to sleep tonight!")
            }
        }
    }
}
