package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class ThakiTheDeliveryDwarf : Script {

    init {
        npcOperate("Talk-to", "thaki_the_delivery_dwarf") {
            statement("The dwarf hiccups, then burps a foul cloud of stench that turns your stomach. You feel it best to leave before something worse happens.")
        }
    }
}
