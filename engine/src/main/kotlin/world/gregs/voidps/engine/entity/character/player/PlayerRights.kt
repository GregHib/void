package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.ui.chat.toSentenceCase
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set

enum class PlayerRights {
    None,
    Mod,
    Admin
}

var Player.rights: PlayerRights
    get() = PlayerRights.valueOf(get("rights", "none").toSentenceCase())
    set(value) = set("rights", true, value.name.lowercase())

fun Player.isAdmin() = hasRights(PlayerRights.Admin)

fun Player.isMod() = hasRights(PlayerRights.Mod)

private fun Player.hasRights(rights: PlayerRights) = this.rights.ordinal >= rights.ordinal