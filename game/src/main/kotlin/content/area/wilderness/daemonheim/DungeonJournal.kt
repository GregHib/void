package content.area.wilderness.daemonheim

import world.gregs.voidps.engine.Script

class DungeonJournal : Script {
    init {
        interfaceOpened("dungeon_journal") {
            interfaces.sendText("dungeon_journal", "bilrach", "Chronicles of Bilrach")
            interfaces.sendText("dungeon_journal", "marmaros", "Marmaros and Thok")
            interfaces.sendText("dungeon_journal", "stalker", "Stalker Notes")
            interfaces.sendText("dungeon_journal", "behemoth", "Behemoth Notes")
            interfaces.sendText("dungeon_journal", "kalgerion", "Kal'Gerion Notes")
        }
    }
}