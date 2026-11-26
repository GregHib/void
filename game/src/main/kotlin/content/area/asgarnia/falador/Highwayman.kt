package content.area.asgarnia.falador

import world.gregs.voidps.engine.Script

class Highwayman : Script {

    init {
        huntPlayer("highwayman*", "cowardly") {
            say("Stand and deliver!")
        }
    }
}
