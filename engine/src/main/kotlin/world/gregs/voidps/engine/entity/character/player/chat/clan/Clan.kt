package world.gregs.voidps.engine.entity.character.player.chat.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin

data class Clan(
    val owner: String,
    var ownerDisplayName: String,
    var name: String = ownerDisplayName,
    var friends: Map<String, ClanRank>,
    var ignores: List<String>,
    var joinRank: ClanRank = ClanRank.Friend,
    var talkRank: ClanRank = ClanRank.Anyone,
    var kickRank: ClanRank = ClanRank.Owner,
    var lootRank: ClanRank = ClanRank.None,
    var coinShare: Boolean = false,
    val members: MutableList<Player> = mutableListOf(),
) {

    fun getRank(player: Player): ClanRank = when {
        player.accountName == owner -> ClanRank.Owner
        player.isAdmin() -> ClanRank.Admin
        else -> friends.getOrDefault(player.accountName, ClanRank.None)
    }

    fun hasRank(player: Player, rank: ClanRank, inclusive: Boolean = true): Boolean {
        val playerRank = getRank(player)
        return inclusive && playerRank.value >= rank.value || playerRank.value > rank.value
    }
}
