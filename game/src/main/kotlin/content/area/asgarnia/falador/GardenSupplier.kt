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
                    // todo when construction is added, there needs to be a check for if the player has a house or not; then the alternate dialogue can be added
                    npc<Laugh>("For planting in your house's garden, of course!")
                    player<Disheartened>("I don't have a house.")
                    npc<Laugh>("Well they won't do you much good then, will they?")
                }
                option("No thanks") {
                    player<Neutral>("No thanks.")
                }
            }
        }
    }
}
