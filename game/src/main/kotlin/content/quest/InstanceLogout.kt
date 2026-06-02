package content.quest

import world.gregs.voidps.engine.Script

class InstanceLogout : Script {
    init {
        playerDespawn {
            if (get("instance_logout", false)) {
                exitInstance()
            }
        }
    }
}
