import world.gregs.voidps.engine.event.Script

@Script
class PrayerToggle {

    init {
        variableSet("activated_*") { player ->
            player.closeInterfaces()
            val from = (from as? List<String>)?.toSet() ?: emptySet()
            val to = (to as? List<String>)?.toSet() ?: emptySet()
            for (prayer in from.subtract(to)) {
                player.emit(PrayerStop(prayer))
            }
            for (prayer in to.subtract(from)) {
                player.emit(PrayerStart(prayer))
            }
        }

        variableBitAdd(ACTIVE_PRAYERS, ACTIVE_CURSES) { player ->
            player.closeInterfaces()
            player.emit(PrayerStart((value as String).toSnakeCase()))
        }

        variableBitRemove(ACTIVE_PRAYERS, ACTIVE_CURSES) { player ->
            player.closeInterfaces()
            player.emit(PrayerStop((value as String).toSnakeCase()))
        }

    }

    @file:Suppress("UNCHECKED_CAST")
    
    package content.skill.prayer.list
    
    import content.skill.prayer.PrayerConfigs.ACTIVE_CURSES
    import content.skill.prayer.PrayerConfigs.ACTIVE_PRAYERS
    import content.skill.prayer.PrayerStart
    import content.skill.prayer.PrayerStop
    import net.pearx.kasechange.toSnakeCase
    import world.gregs.voidps.engine.client.ui.closeInterfaces
    import world.gregs.voidps.engine.client.variable.variableBitAdd
    import world.gregs.voidps.engine.client.variable.variableBitRemove
    import world.gregs.voidps.engine.client.variable.variableSet
    
}
