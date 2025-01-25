package world.gregs.voidps.world.community.clan

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.chat.clan.LeaveClanChat
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.network.login.protocol.encode.Member
import world.gregs.voidps.network.login.protocol.encode.leaveClanChat
import world.gregs.voidps.network.login.protocol.encode.updateClanChat
import world.gregs.voidps.world.community.friend.world
import world.gregs.voidps.world.community.friend.worldName
import content.entity.player.dialogue.type.stringEntry

interfaceOption("Clan Setup", "settings", "clan_chat") {
    if (player.hasMenuOpen()) {
        player.message("Please close the interface you have open before using Clan Chat setup.")
        return@interfaceOption
    }
    player.open("clan_chat_setup")
}

interfaceOpen("clan_chat_setup") { player ->
    val clan = player.clan ?: player.ownClan ?: return@interfaceOpen
    player.interfaces.apply {
        sendText(id, "name", clan.name.ifBlank { "Chat disabled" })
        sendText(id, "enter", clan.joinRank.string)
        sendText(id, "talk", clan.talkRank.string)
        sendText(id, "kick", clan.kickRank.string)
        sendText(id, "loot", clan.lootRank.string)
    }
    player.sendVariable("coin_share_setting")
}

interfaceOption(component = "enter", id = "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    val rank = ClanRank.from(option)
    if (rank == ClanRank.None) {
        return@interfaceOption
    }
    clan.joinRank = rank
    player["clan_join_rank"] = rank.name
    player.interfaces.sendText(id, component, option)
    for (member in clan.members) {
        if (!clan.hasRank(member, rank)) {
            member.emit(LeaveClanChat(forced = true))
        }
    }
}

interfaceOption(component = "talk", id = "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    val rank = ClanRank.from(option)
    if (rank == ClanRank.None) {
        return@interfaceOption
    }
    clan.talkRank = rank
    player["clan_talk_rank"] = rank.name
    player.interfaces.sendText(id, component, option)
}

interfaceOption(component = "kick", id = "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    val rank = ClanRank.from(option)
    if (rank.value <= ClanRank.Recruit.value) {
        return@interfaceOption
    }
    clan.kickRank = rank
    player["clan_kick_rank"] = rank.name
    player.interfaces.sendText(id, component, option)
    updateUI(clan)
}

interfaceOption(component = "loot", id = "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    val rank = ClanRank.from(option)
    if (rank == ClanRank.Anyone || rank == ClanRank.Owner) {
        return@interfaceOption
    }
    clan.lootRank = rank
    player["clan_loot_rank"] = rank.name
    player.interfaces.sendText(id, component, option)
    player.softTimers.start("clan_loot_rank_update")
    player.message("Changes will take effect on your clan in the next 60 seconds.", ChatType.ClanChat)
}

interfaceOption(component = "coin_share", id = "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    player.toggle("coin_share_setting")
    player.softTimers.start("clan_coin_share_update")
    player.message("Changes will take effect on your clan in the next 60 seconds.", ChatType.ClanChat)
}

interfaceOption("Set prefix", "name", "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    val name = stringEntry("Enter chat prefix:")
    if (name.length > 12) {
        player.message("Name too long. A channel name cannot be longer than 12 characters.", ChatType.ClanChat)
        return@interfaceOption
    }
    if (name.contains("mod", true) || name.contains("staff", true) || name.contains("admin", true)) {
        player.message("Name contains a banned word. Please try another name.", ChatType.ClanChat)
        return@interfaceOption
    }
    clan.name = name
    player["clan_name"] = name
    player.interfaces.sendText(id, component, name)
    updateUI(clan)
}

interfaceClose("clan_chat_setup") { player ->
    player.sendScript("clear_dialogues")
}

interfaceOption("Disable", "name", "clan_chat_setup") {
    val clan = player.clan ?: player.ownClan ?: return@interfaceOption
    if (!clan.hasRank(player, ClanRank.Owner)) {
        player.message("Only the clan chat owner can do this.", ChatType.ClanChat)
        return@interfaceOption
    }
    clan.name = ""
    player["clan_name"] = ""
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
    val membersList = clan.members.map { Member(it.name, Settings.world, clan.getRank(it).value, Settings.worldName) }
    for (member in clan.members) {
        member.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, membersList)
    }
}