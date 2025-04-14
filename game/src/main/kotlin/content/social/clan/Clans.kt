package content.social.clan

import content.social.friend.world
import content.social.friend.worldName
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.network.login.protocol.encode.Member

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

object ClanLootShare {
    fun update(player: Player, clan: Clan, lootShare: Boolean) {
        player["loot_share"] = lootShare
        player["coin_share"] = clan.coinShare
        if (lootShare) {
            player.message("LootShare is now active. The CoinShare option is ${if (clan.coinShare) "on" else "off"}.", ChatType.ClanChat)
        } else {
            player.message("LootShare is no longer active.", ChatType.ClanChat)
        }
    }
}

object ClanMember {
    fun of(player: Player, rank: ClanRank) = Member(
        player.name, Settings.world, rank.value, Settings.worldName
    )
}