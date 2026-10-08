package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.construction.House.Companion.hasHouse
import world.gregs.voidps.engine.Script

class GardenSupplier : Script {

    init {
        npcOperate("Talk-to", "garden_supplier") {
            npc<Happy>("Do you want to buy some garden plants?")
            choice {
                option("Yes please!") {
                    player<Happy>("Yes please!")
                    openShop("garden_centre")
                }
                option<Quiz>("What are these plants for?") {
                    npc<Laugh>("For planting in your house's garden, of course!")
                    if (hasHouse()) {
                        player<Quiz>("How do I do that?")
                        npc<Laugh>("The same way you make furniture. You'll find plant hotspots in your garden and you just need to have one of my bagged saplings with you when you build at them.")
                        player<Neutral>("Ah, I see.")
                        npc<Happy>("So do you want to look at my stock?")
                        choice {
                            option("Yes please!") {
                                openShop("garden_centre")
                            }
                            option("No thanks") {
                                player<Neutral>("No thanks.")
                            }
                        }
                    } else {
                        player<Disheartened>("I don't have a house.")
                        npc<Laugh>("Well they won't do you much good then, will they?")
                    }
                }
                option("No thanks") {
                    player<Neutral>("No thanks.")
                }
            }
        }
    }
}
