package content.skill.prayer

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import content.skill.prayer.PrayerConfigs.PRAYERS
import content.skill.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import content.skill.prayer.PrayerConfigs.USING_QUICK_PRAYERS

interfaceOpen("prayer_orb") { player ->
    player.sendVariable(SELECTING_QUICK_PRAYERS)
    player.sendVariable(USING_QUICK_PRAYERS)
}

interfaceOpen("prayer_list") { player ->
    player.sendVariable(PRAYERS)
}

interfaceRefresh("prayer_list") { player ->
    val quickPrayers = player[SELECTING_QUICK_PRAYERS, false]
    if (quickPrayers) {
        player.interfaceOptions.unlockAll(id, "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll(id, "regular_prayers", 0..29)
    }
}