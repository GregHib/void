package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject

class Recruiter : Script {

    val floorItems: FloorItems by inject()

    init {
        npcOperate("Talk-to", "recruiter") {
            npc<Neutral>("Citizens of West Ardougne! King Tyras needs you for his Royal Army! Who will join this noble cause?")
            npc<Angry>("w_ardougnecitizen3", "Plague bringer!")
            npc<Angry>("w_ardougnecitizen3", "King Tyras is scum!")
            npc<Surprised>("Tyras will be informed of these words of treason!")
            player.sound("plague_tomato")
            floorItems.add(target.tile, "tomato", disappearTicks = 300)
            statement("Someone throws a tomato at the recruiter.")
        }
    }
}
