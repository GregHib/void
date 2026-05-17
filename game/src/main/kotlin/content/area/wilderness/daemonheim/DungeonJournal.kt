package content.area.wilderness.daemonheim

import world.gregs.voidps.engine.Script

class DungeonJournal : Script {
    init {
        interfaceOpened("dungeon_journals") {
            interfaces.sendText(it, "bilrach", "Chronicles of Bilrach")
            interfaces.sendText(it, "marmaros", "Marmaros and Thok")
            interfaces.sendText(it, "stalker", "Stalker Notes")
            interfaces.sendText(it, "behemoth", "Behemoth Notes")
            interfaces.sendText(it, "kalgerion", "Kal'Gerion Notes")
        }
    }
}
