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

class ClanWarsFreeForAllSafe : Script {

    val outside = Tile(3272, 3692, 0)
    val arena = Tile(2815, 5511, 0)

    init {
        // Entry portal — skip warning if player has dismissed it via doomsayer, otherwise show it
        objectApproach("Enter", "clan_wars_portal_ffa_safe") {
            approachRange(1)
            if (combatLevel < 30) {
                message("You need a combat level of at least 30 to enter this portal.")
                return@objectApproach
            }
            if (get("warning_clan_wars_ffa_safe", 0) == 7) {
                tele(arena)
                return@objectApproach
            }
            open("warning_clan_wars_ffa_safe")
        }

        // "Go in" button (component 15). Warning.kt handles the "dont_ask" toggle (component 9)
        // and Doomsayer.kt increments the view count via interfaceOpened("warning_*").
        interfaceOption("Go in", "warning_clan_wars_ffa_safe:yes") {
            close("warning_clan_wars_ffa_safe")
            tele(arena)
        }

        // "Cancel" button — close without entering
        interfaceOption("Cancel", "warning_clan_wars_ffa_safe:no") {
            close("warning_clan_wars_ffa_safe")
        }

        // "Don't show again" checkbox
        interfaceOption("Toggle warning", "warning_clan_wars_ffa_safe:dont_ask") {
            set("warning_clan_wars_ffa_safe", if (get("warning_clan_wars_ffa_safe", 0) == 7) 0 else 7)
        }

        // Exit portal
        objectApproach("Leave", "clan_wars_portal_ffa_safe_exit") {
            approachRange(1)
            tele(outside)
            message("You have left the Clan Wars Free-For-All (Safe).", ChatType.Filter)
        }

        // Overlay: covers the full zone including the lobby at y=5511
        entered("clan_wars_ffa") {
            open("clan_wars")
        }

        exited("clan_wars_ffa") {
            close("clan_wars")
        }

        // PvP: only inside the fighting area (y >= 5512)
        entered("clan_wars_ffa_safe_arena") {
            set("in_pvp", true)
            options.set(1, "Attack")
        }

        exited("clan_wars_ffa_safe_arena") {
            clear("in_pvp")
            options.remove("Attack")
        }

        // On death: keep items, respawn outside
        playerDeath {
            if (!get("in_pvp", false) && tile !in Areas["clan_wars_ffa"]) {
                return@playerDeath
            }
            it.dropItems = false
            it.teleport = outside
        }

        // On login: restore overlay and pvp state if still inside
        playerSpawn {
            if (tile in Areas["clan_wars_ffa"]) {
                open("clan_wars")
                if (tile in Areas["clan_wars_ffa_safe_arena"]) {
                    set("in_pvp", true)
                    options.set(1, "Attack")
                }
            }
        }
    }
}
