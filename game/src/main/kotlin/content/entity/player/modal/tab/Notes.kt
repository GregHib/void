package content.entity.player.modal.tab

import content.entity.player.dialogue.type.stringEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.entity.character.player.Player

class Notes : Script {

    init {
        interfaceOpened("notes") { id ->
            interfaceOptions.unlockAll(id, "notes", 0..30)
            interfaces.sendVisibility("notes", "add", !contains("note_29"))
            set("notes_active", true)
            set("selected_note", -1)
            sendScript("clear_notes")
            sendScript("show_notes")
            for (i in 0 until 30) {
                if (contains("note_$i")) {
                    sendVariable("note_$i")
                    sendVariable("note_colour_$i")
                }
            }
        }

        interfaceOption("Add note", "notes:add") {
            val note = stringEntry("Add note:").take(80)
            for (i in 0 until 30) {
                if (contains("note_$i")) {
                    continue
                }
                set("note_$i", note)
                set("selected_note", i)
                break
            }
            if (contains("note_29")) {
                interfaces.sendVisibility("notes", "add", false)
            }
        }

        interfaceOption("Select", "notes:notes") {
            set("selected_note", it.itemSlot)
        }

        interfaceOption("Edit", "notes:notes") {
            val note = stringEntry("Edit note:", get("note_${it.itemSlot}")).take(80)
            set("note_${it.itemSlot}", note)
            set("selected_note", it.itemSlot)
        }

        interfaceOption("Colour", "notes:notes") {
            set("selected_note", it.itemSlot)
            interfaces.sendVisibility("notes", "colours", true)
        }

        interfaceOption("Delete", "notes:notes") {
            val slot = it.itemSlot
            delete(slot)
        }

        interfaceOption("Delete", "notes:delete") {
            val selected = get("selected_note", -1)
            if (selected == -1) {
                // https://youtu.be/zQwLVkwLgaI?t=12
                message("Please select a note to delete first.")
                return@interfaceOption
            }
            delete(selected)
        }

        interfaceOption("Delete all", "notes:delete") {
            for (i in 0 until 30) {
                clear("note_$i")
                clear("note_colour_$i")
            }
            set("selected_note", -1)
            sendScript("clear_notes")
            interfaces.sendVisibility("notes", "add", true)
        }

        interfaceOption("White", "notes:white") {
            val slot: Int = get("selected_note") ?: return@interfaceOption
            set("note_colour_$slot", 0)
            sendScript("close_note_colours")
        }

        interfaceOption("Green", "notes:green") {
            val slot: Int = get("selected_note") ?: return@interfaceOption
            set("note_colour_$slot", 1)
            sendScript("close_note_colours")
        }

        interfaceOption("Amber", "notes:amber") {
            val slot: Int = get("selected_note") ?: return@interfaceOption
            set("note_colour_$slot", 2)
            sendScript("close_note_colours")
        }

        interfaceOption("Red", "notes:red") {
            val slot: Int = get("selected_note") ?: return@interfaceOption
            set("note_colour_$slot", 3)
            sendScript("close_note_colours")
        }
    }

    private fun Player.delete(slot: Int) {
        clear("note_$slot")
        clear("note_colour_$slot")
        for (i in slot + 1 .. 30) {
            val next = get<String>("note_$i")
            if (next == null) {
                clear("note_${i - 1}")
                clear("note_colour_${i - 1}")
                if (i - 1 == slot) {
                    dec("selected_note")
                }
                break
            }
            set("note_${i - 1}", next)
            set("note_colour_${i - 1}", get("note_colour_$i", 0))
        }
        interfaces.sendVisibility("notes", "add", true)
    }
}
