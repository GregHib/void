package content.area.kharidian_desert.kalphite_lair

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class KalphiteHive : Script {
    init {
        playerSpawn {
            sendVariable("kalphite_tunnel_rope")
            sendVariable("kalphite_lair_rope")
        }

        itemOnObjectOperate("rope", "kalphite_hive_tunnel") {
            if (!get("kalphite_tunnel_rope", false) && inventory.remove("rope")) {
                set("kalphite_tunnel_rope", true)
            }
        }

        itemOnObjectOperate("rope", "kalphite_lair_entrance") {
            if (!get("kalphite_lair_rope", false) && inventory.remove("rope")) {
                set("kalphite_lair_rope", true)
            }
        }

        objectOperate("Enter", "kalphite_hive_wall_tunnel") {
            message("It looks like the tunnel is blocked at the other end.")
        }
    }
}
