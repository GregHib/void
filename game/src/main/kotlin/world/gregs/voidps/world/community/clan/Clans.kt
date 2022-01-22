package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Clan
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.set

var Player.clan: Clan?
    get() = getOrNull("clan")
    set(value) {
        set("clan", value ?: return)
    }

var Player.ownClan: Clan?
    get() = getOrNull("own_clan")
    set(value) {
        set("own_clan", value ?: return)
    }

var Player.chatType: String
    get() = get("chat_type", "public")
    set(value) = set("chat_type", value)