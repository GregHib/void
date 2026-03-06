package content.quest

import world.gregs.voidps.engine.Script

class InstanceLogout : Script {
    init {
        playerDespawn {
            exitInstance()
        }
    }
}
