package world.gregs.voidps.world.interact.entity.player.combat.prayer

import world.gregs.voidps.engine.client.ui.event.interfaceOpened
import world.gregs.voidps.engine.client.ui.event.interfaceRefreshed
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS

interfaceOpened({ id == "prayer_orb" }) { player: Player ->
    player.sendVariable(SELECTING_QUICK_PRAYERS)
    player.sendVariable(USING_QUICK_PRAYERS)
}

interfaceOpened({ id == "prayer_list" }) { player: Player ->
    player.sendVariable(PRAYERS)
}

interfaceRefreshed({ id == "prayer_list" }) { player: Player ->
    val quickPrayers = player[SELECTING_QUICK_PRAYERS, false]
    if(quickPrayers) {
        player.interfaceOptions.unlockAll(id, "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll(id, "regular_prayers", 0..29)
    }
}