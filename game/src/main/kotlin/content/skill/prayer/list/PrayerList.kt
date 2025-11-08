package content.skill.prayer.list

import content.skill.prayer.PrayerConfigs.PRAYERS
import content.skill.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.USING_QUICK_PRAYERS
import world.gregs.voidps.engine.Script

class PrayerList : Script {

    init {
        interfaceOpened("prayer_orb") {
            sendVariable(SELECTING_QUICK_PRAYERS)
            sendVariable(USING_QUICK_PRAYERS)
        }

        interfaceOpened("prayer_list") {
            sendVariable(PRAYERS)
        }

        interfaceRefresh("prayer_list") { id ->
            val quickPrayers = get(SELECTING_QUICK_PRAYERS, false)
            if (quickPrayers) {
                interfaceOptions.unlockAll(id, "quick_prayers", 0..29)
            } else {
                interfaceOptions.unlockAll(id, "regular_prayers", 0..29)
            }
        }
    }
}
