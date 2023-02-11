package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.chat.clan.LeaveClanChat
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.encode.Member
import world.gregs.voidps.network.encode.leaveClanChat
import world.gregs.voidps.network.encode.updateClanChat
import world.gregs.voidps.world.interact.dialogue.type.stringEntry
import java.util.concurrent.TimeUnit

on<InterfaceOption>({ id == "clan_chat" && component == "settings" && option == "Clan Setup" }) { player: Player ->
    if (player.hasScreenOpen()) {
        player.message("Please close the interface you have open before using Clan Chat setup.")
        return@on
    }
    player.open("clan_chat_setup")
}

on<InterfaceOpened>({ id == "clan_chat_setup" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    player.interfaces.apply {
        sendText(id, "name", clan.name.ifBlank { "Chat disabled" })
        sendText(id, "enter", clan.joinRank.string)
        sendText(id, "talk", clan.talkRank.string)
        sendText(id, "kick", clan.kickRank.string)
        sendText(id, "loot", clan.lootRank.string)
    }
    player.sendVar("coin_share_setting")
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "enter" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = ClanRank.of(option)
    if (rank == ClanRank.None) {
        return@on
    }
    clan.joinRank = rank
    player["clan_join_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
    for (member in clan.members) {
        if (!clan.hasRank(member, rank)) {
            member.events.emit(LeaveClanChat(forced = true))
        }
    }
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "talk" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = ClanRank.of(option)
    if (rank == ClanRank.None) {
        return@on
    }
    clan.talkRank = rank
    player["clan_talk_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "kick" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = ClanRank.of(option)
    if (rank.value <= ClanRank.Recruit.value) {
        return@on
    }
    clan.kickRank = rank
    player["clan_kick_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
    updateUI(clan)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "loot" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val rank = ClanRank.of(option)
    if (rank == ClanRank.Anyone || rank == ClanRank.Owner) {
        return@on
    }
    player["clan_loot_rank", true] = rank.name
    player.interfaces.sendText(id, component, option)
    player.start("clan_loot_rank_update", ticks = TimeUnit.SECONDS.toTicks(30))
    player.message("Changes will take effect on your clan in the next 60 seconds.", ChatType.ClanChat)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "coin_share" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    player.toggleVar("coin_share_setting")
    player.start("clan_coin_share_update", ticks = TimeUnit.SECONDS.toTicks(30))
    player.message("Changes will take effect on your clan in the next 60 seconds.", ChatType.ClanChat)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "name" && option == "Set prefix" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    val name = stringEntry("Enter chat prefix:")
    if (name.length > 12) {
        player.message("Name too long. A channel name cannot be longer than 12 characters.", ChatType.ClanChat)
        return@on
    }
    if (name.contains("mod", true) || name.contains("staff", true) || name.contains("admin", true)) {
        player.message("Name contains a banned word. Please try another name.", ChatType.ClanChat)
        return@on
    }
    clan.name = name
    player["clan_name", true] = name
    player.interfaces.sendText(id, component, name)
    updateUI(clan)
}

on<InterfaceOption>({ id == "clan_chat_setup" && component == "name" && option == "Disable" }) { player: Player ->
    val clan = player.clan ?: player.ownClan ?: return@on
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@on
    }
    clan.name = ""
    player["clan_name", true] = ""
    player.interfaces.sendText(id, component, "Chat disabled")
    for (member in clan.members) {
        member.remove<Clan>("clan")
        member.clear("clan_chat")
        member.message("You have been kicked from the channel.", ChatType.ClanChat)
        member.client?.leaveClanChat()
    }
    clan.members.clear()
}

fun updateUI(clan: Clan) {
    val membersList = clan.members.map { Member(it.name, World.id, clan.getRank(it).value, World.name) }
    for (member in clan.members) {
        member.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, membersList)
    }
}