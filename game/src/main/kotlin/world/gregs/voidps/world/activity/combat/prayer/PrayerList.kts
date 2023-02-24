package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS

on<InterfaceOpened>({ id == "prayer_orb" }) { player: Player ->
    player.sendVariable(SELECTING_QUICK_PRAYERS)
    player.sendVariable(USING_QUICK_PRAYERS)
}

on<InterfaceOpened>({ id == "prayer_list" }) { player: Player ->
    player.sendVariable(PRAYERS)
}

on<InterfaceRefreshed>({ id == "prayer_list" }) { player: Player ->
    val quickPrayers = player[SELECTING_QUICK_PRAYERS, false]
    if(quickPrayers) {
        player.interfaceOptions.unlockAll(id, "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll(id, "regular_prayers", 0..29)
    }
}