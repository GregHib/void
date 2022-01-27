package world.gregs.voidps.engine.entity.character.player.chat

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin

data class Clan(
    val owner: String,
    var ownerDisplayName: String,
    var name: String = ownerDisplayName,
    var friends: Map<String, Rank>,
    var ignores: List<String>,
    var joinRank: Rank = Rank.Friend,
    var talkRank: Rank = Rank.Anyone,
    var kickRank: Rank = Rank.Owner,
    var lootRank: Rank = Rank.None,
    var coinShare: Boolean = false,
    val members: MutableList<Player> = mutableListOf()
) {

    fun getRank(player: Player): Rank = when {
        player.accountName == owner -> Rank.Owner
        player.isAdmin() -> Rank.Admin
        else -> friends.getOrDefault(player.accountName, Rank.None)
    }

    fun hasRank(player: Player, rank: Rank, inclusive: Boolean = true): Boolean {
        val playerRank = getRank(player)
        return inclusive && playerRank.value >= rank.value || playerRank.value > rank.value
    }

}