package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Clan
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.set

val Player.clan: Clan?
    get() = getOrNull("clan")

var Player.chatType: String
    get() = get("chat_type", "public")
    set(value) = set("chat_type", value)