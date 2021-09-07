package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.SELECTING_QUICK_PRAYERS
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.USING_QUICK_PRAYERS

on<InterfaceOpened>({ name == "prayer_orb" }) { player: Player ->
    player.sendVar(SELECTING_QUICK_PRAYERS)
    player.sendVar(USING_QUICK_PRAYERS)
}

on<InterfaceOpened>({ name == "prayer_list" }) { player: Player ->
    player.sendVar(PRAYERS)
}

on<InterfaceRefreshed>({ name == "prayer_list" }) { player: Player ->
    val quickPrayers = player.getVar(SELECTING_QUICK_PRAYERS, false)
    if(quickPrayers) {
        player.interfaceOptions.unlockAll(name, "quick_prayers", 0..29)
    } else {
        player.interfaceOptions.unlockAll(name, "regular_prayers", 0..29)
    }
}