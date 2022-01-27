import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.Clan
import world.gregs.voidps.engine.entity.character.player.chat.LeaveClanChat
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.community.clan.ownClan
import java.util.concurrent.TimeUnit

on<InterfaceOption>({ id == "clan_chat" && component == "loot_share" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (clan.lootRank == Rank.None) {
        player.message("LootShare is disabled by the clan owner.", ChatType.ClanChat)
        return@on
    }
    if (!clan.hasRank(player, clan.lootRank)) {
        player.message("Only ${clan.lootRank.name.lowercase()}s can share loot.", ChatType.ClanChat)
        return@on
    }
    player.setVar("loading_loot_share", true)
    player.start("clan_loot_update", ticks = TimeUnit.MINUTES.toTicks(2))
    val lootShare = player.getVar("loot_share", false)
    player.message("You will ${if (lootShare) "stop sharing" else "be able to share"} loot in 2 minutes.", ChatType.ClanChat)
}

on<LeaveClanChat>(priority = Priority.HIGH) { player: Player ->
    val clan: Clan = player.clan ?: return@on
    update(player, clan, lootShare = false)
}

on<EffectStop>({ effect == "clan_loot_update" }) { player: Player ->
    player.setVar("loading_loot_share", false)
    val clan = player.clan ?: return@on
    val lootShare = player.toggleVar("loot_share")
    update(player, clan, lootShare)
}

on<EffectStop>({ effect == "clan_loot_rank_update" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    clan.lootRank = Rank.valueOf(player["clan_loot_rank", "None"])
    for (member in clan.members) {
        if (!clan.hasRank(member, clan.lootRank) && member.getVar("loot_share", false)) {
            update(player, clan, lootShare = false)
        }
    }
}

on<EffectStop>({ effect == "clan_coin_share_update" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    clan.coinShare = player.getVar("coin_share_setting", false)
    for (member in clan.members) {
        member.setVar("coin_share", clan.coinShare)
        member.message("CoinShare has been switched ${if (clan.coinShare) "on" else "off"}.", ChatType.ClanChat)
    }
}

fun update(player: Player, clan: Clan, lootShare: Boolean) {
    player.setVar("loot_share", lootShare)
    player.setVar("coin_share", clan.coinShare)
    if (lootShare) {
        player.message("LootShare is now active. The CoinShare option is ${if (clan.coinShare) "on" else "off"}.", ChatType.ClanChat)
    } else {
        player.message("LootShare is no longer active.", ChatType.ClanChat)
    }
}