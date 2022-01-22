import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.Clan
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.Member
import world.gregs.voidps.network.encode.updateClanChat
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.interact.dialogue.type.stringEntry

on<InterfaceOption>({ id == "clan_chat" && component == "settings" && option == "Clan Setup" }) { player: Player ->
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before using Clan Chat setup.")
        return@on
    }
    player.open("clan_chat_setup")
}

on<InterfaceOpened>({ id == "clan_chat_setup" }) { player: Player ->
    val clan = player.clan!!
    player.interfaces.apply {
        sendText(id, "name", clan.name)
        sendText(id, "enter", clan.joinRank.string)
        sendText(id, "talk", clan.talkRank.string)
        sendText(id, "kick", clan.kickRank.string)
        sendText(id, "loot", clan.lootRank.string)
    }
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "enter" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (!clan.hasRank(player, Rank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = Rank.of(option)
    if (rank == Rank.None) {
        return@on
    }
    clan.joinRank = rank
    player["clan_join_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "talk" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (!clan.hasRank(player, Rank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = Rank.of(option)
    if (rank == Rank.None) {
        return@on
    }
    clan.talkRank = rank
    player["clan_talk_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "kick" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (!clan.hasRank(player, Rank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = Rank.of(option)
    if (rank.value <= Rank.Recruit.value) {
        return@on
    }
    clan.kickRank = rank
    player["clan_kick_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
    updateUI(clan)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "loot" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (!clan.hasRank(player, Rank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = Rank.of(option)
    if (rank == Rank.Anyone || rank == Rank.Owner) {
        return@on
    }
    clan.lootRank = rank
    player["clan_loot_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "coin_share" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (!clan.hasRank(player, Rank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val share: Boolean = player.toggleVar("coin_share")
    clan.coinShare = share
    player["clan_coin_share", true] = share
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "name" && option == "Set prefix" }) { player: Player ->
    val clan = player.clan ?: return@on
    if (!clan.hasRank(player, Rank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    player.dialogue {
        val name = stringEntry("Enter chat prefix:")
        if (name.length > 12) {
            player.message("Name too long. A channel name cannot be longer than 12 characters.", ChatType.ClanChat)
            return@dialogue
        }
        if (name.contains("mod", true) || name.contains("staff", true) || name.contains("admin", true)) {
            player.message("Name contains a banned word. Please try another name.", ChatType.ClanChat)
            return@dialogue
        }
        clan.name = name
        player["clan_name", true] = name
        updateUI(clan)
    }
}

fun updateUI(clan: Clan) {
    val membersList = clan.members.map { Member(it.name, World.id, clan.getRank(it).value, World.name) }
    for (member in clan.members) {
        member.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, membersList)
    }
}