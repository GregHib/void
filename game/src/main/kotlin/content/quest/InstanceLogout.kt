package content.quest

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHinted

class InstanceLogout : Script {
    init {
        playerSpawn {
            // Saves written before instance exits cleared it can still carry a logout tile from
            // an instance left long ago; drop it so deaths and exits use the real location.
            if (instance() == null) {
                clear("instance_logout_tile")
            }
        }

        playerDespawn {
            if (get("instance_logout", false)) {
                exitInstance()
            }
            clearHinted()
        }
    }
}
