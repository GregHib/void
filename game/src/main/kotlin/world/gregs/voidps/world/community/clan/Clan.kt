package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.getOrNull
import world.gregs.voidps.engine.entity.set

data class Clan(
    val owner: String,
    var ownerDisplayName: String,
    var name: String = ownerDisplayName,
    val friends: Map<String, Rank>,
    val ignores: List<String>,
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


val Player.clan: Clan?
    get() = getOrNull("clan")

var Player.chatType: String
    get() = get("chat_type", "public")
    set(value) = set("chat_type", value)