package content.minigame.clan_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.type.Tile

class ClanWarsFreeForAll : Script {

    val outside = Tile(3272, 3692, 0)
    val safeArena = Tile(2815, 5511, 0)
    val dangerousArena = Tile(3007, 5511, 0)

    init {
        // Entry portals - varbit 5279 selects safe (0) or dangerous (1) so the client shows the correct text
        objectOperate("Enter", "clan_wars_portal_ffa_safe") {
            if (combatLevel < 30) {
                message("You need a combat level of at least 30 to enter this portal.")
                return@objectOperate
            }
            set("clan_wars_ffa_portal", 0)
            if (get("warning_clan_wars_ffa_safe", 0) == 1) {
                tele(safeArena)
                return@objectOperate
            }
            open("warning_clan_wars_ffa_safe")
        }

        objectOperate("Enter", "clan_wars_portal_ffa_dangerous") {
            if (combatLevel < 30) {
                message("You need a combat level of at least 30 to enter this portal.")
                return@objectOperate
            }
            set("clan_wars_ffa_portal", 1)
            if (get("warning_clan_wars_ffa_dangerous", 0) == 1) {
                tele(dangerousArena)
                return@objectOperate
            }
            open("warning_clan_wars_ffa_safe")
        }

        // "Go in" button - reads varbit 5279 to pick the arena
        interfaceOption("Go in", "warning_clan_wars_ffa_safe:yes") {
            close("warning_clan_wars_ffa_safe")
            tele(if (get("clan_wars_ffa_portal", 0) == 1) dangerousArena else safeArena)
        }

        // "Cancel" button
        interfaceOption("Cancel", "warning_clan_wars_ffa_safe:no") {
            close("warning_clan_wars_ffa_safe")
        }

        // "Don't show again" checkbox - toggles the varbit for whichever portal opened the dialog
        interfaceOption("Toggle warning", "warning_clan_wars_ffa_safe:dont_ask") {
            val key = if (get("clan_wars_ffa_portal", 0) == 1) "warning_clan_wars_ffa_dangerous" else "warning_clan_wars_ffa_safe"
            set(key, if (get(key, 0) == 1) 0 else 1)
        }

        // Clan Wars challenge portal - not yet implemented
        objectOperate("Enter", "clan_wars_challenge_portal") {
            message("Clan Wars is still under construction.")
        }

        // Exit portal - reads varbit 5279 to determine which arena the player entered from
        objectOperate("Leave", "clan_wars_portal_ffa_safe_exit") {
            tele(outside)
            val type = if (get("clan_wars_ffa_portal", 0) == 1) "Dangerous" else "Safe"
            message("You have left the Clan Wars Free-For-All ($type).", ChatType.Filter)
        }

        // Overlay: covers both arenas including the lobby at y=5511
        entered("clan_wars_ffa") {
            open("clan_wars")
        }

        exited("clan_wars_ffa") {
            close("clan_wars")
        }

        // PvP - safe arena
        entered("clan_wars_ffa_safe_arena") {
            set("in_pvp", true)
            options.set(1, "Attack")
        }

        exited("clan_wars_ffa_safe_arena") {
            clear("in_pvp")
            options.remove("Attack")
        }

        // PvP - dangerous arena
        entered("clan_wars_ffa_dangerous_arena") {
            set("in_pvp", true)
            options.set(1, "Attack")
        }

        exited("clan_wars_ffa_dangerous_arena") {
            clear("in_pvp")
            options.remove("Attack")
        }

        // On death in safe arena: keep items, respawn outside
        playerDeath {
            if (tile !in Areas["clan_wars_ffa_safe_arena"]) return@playerDeath
            it.dropItems = false
            it.teleport = outside
        }

        // On death in dangerous arena: drop items, respawn outside
        playerDeath {
            if (tile !in Areas["clan_wars_ffa_dangerous_arena"]) return@playerDeath
            it.teleport = outside
        }
    }
}
