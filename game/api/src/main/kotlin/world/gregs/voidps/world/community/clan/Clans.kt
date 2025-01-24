package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan

var Player.clan: Clan?
    get() = get("clan")
    set(value) {
        set("clan", value ?: return)
    }

var Player.ownClan: Clan?
    get() = get("own_clan")
    set(value) {
        set("own_clan", value ?: return)
    }

var Player.chatType: String
    get() = get("chat_type", "public")
    set(value) = set("chat_type", value)