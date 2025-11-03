package content.skill.prayer.list

import content.skill.prayer.PrayerConfigs.PRAYERS
import content.skill.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh

class PrayerList : Script {

    init {
        interfaceOpen("prayer_orb") {
            sendVariable(SELECTING_QUICK_PRAYERS)
            sendVariable(USING_QUICK_PRAYERS)
        }

        interfaceOpen("prayer_list") {
            sendVariable(PRAYERS)
        }

        interfaceRefresh("prayer_list") { player ->
            val quickPrayers = player[SELECTING_QUICK_PRAYERS, false]
            if (quickPrayers) {
                player.interfaceOptions.unlockAll(id, "quick_prayers", 0..29)
            } else {
                player.interfaceOptions.unlockAll(id, "regular_prayers", 0..29)
            }
        }
    }
}
