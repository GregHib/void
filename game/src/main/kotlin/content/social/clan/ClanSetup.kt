package content.social.clan

import content.entity.player.dialogue.type.stringEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.chat.clan.LeaveClanChat
import world.gregs.voidps.network.login.protocol.encode.leaveClanChat
import world.gregs.voidps.network.login.protocol.encode.updateClanChat

class ClanSetup : Script {

    init {
        interfaceOption("Clan Setup", "clan_chat:settings") {
            if (hasMenuOpen()) {
                message("Please close the interface you have open before using Clan Chat setup.")
                return@interfaceOption
            }
            open("clan_chat_setup")
        }

        interfaceOpen("clan_chat_setup") { id ->
            val clan = clan ?: ownClan ?: return@interfaceOpen
            interfaces.apply {
                sendText(id, "name", clan.name.ifBlank { "Chat disabled" })
                sendText(id, "enter", clan.joinRank.string)
                sendText(id, "talk", clan.talkRank.string)
                sendText(id, "kick", clan.kickRank.string)
                sendText(id, "loot", clan.lootRank.string)
            }
            sendVariable("coin_share_setting")
        }

        interfaceOption(id = "clan_chat_setup:enter") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            val rank = ClanRank.from(it.option)
            if (rank == ClanRank.None) {
                return@interfaceOption
            }
            clan.joinRank = rank
            set("clan_join_rank", rank.name)
            interfaces.sendText(it.id, it.component, it.option)
            for (member in clan.members) {
                if (!clan.hasRank(member, rank)) {
                    member.emit(LeaveClanChat(forced = true))
                }
            }
        }

        interfaceOption(id = "clan_chat_setup:talk") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            val rank = ClanRank.from(it.option)
            if (rank == ClanRank.None) {
                return@interfaceOption
            }
            clan.talkRank = rank
            set("clan_talk_rank", rank.name)
            interfaces.sendText(it.id, it.component, it.option)
        }

        interfaceOption(id = "clan_chat_setup:kick") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            val rank = ClanRank.from(it.option)
            if (rank.value <= ClanRank.Recruit.value) {
                return@interfaceOption
            }
            clan.kickRank = rank
            set("clan_kick_rank", rank.name)
            interfaces.sendText(it.id, it.component, it.option)
            updateUI(clan)
        }

        interfaceOption(id = "clan_chat_setup:loot") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            val rank = ClanRank.from(it.option)
            if (rank == ClanRank.Anyone || rank == ClanRank.Owner) {
                return@interfaceOption
            }
            clan.lootRank = rank
            set("clan_loot_rank", rank.name)
            interfaces.sendText(it.id, it.component, it.option)
            softTimers.start("clan_loot_rank_update")
            message("Changes will take effect on your clan in the next 60 seconds.", ChatType.ClanChat)
        }

        interfaceOption(id = "clan_chat_setup:coin_share") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            toggle("coin_share_setting")
            softTimers.start("clan_coin_share_update")
            message("Changes will take effect on your clan in the next 60 seconds.", ChatType.ClanChat)
        }

        interfaceOption("Set prefix", "clan_chat_setup:name") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            val name = stringEntry("Enter chat prefix:")
            if (name.length > 12) {
                message("Name too long. A channel name cannot be longer than 12 characters.", ChatType.ClanChat)
                return@interfaceOption
            }
            if (name.contains("mod", true) || name.contains("staff", true) || name.contains("admin", true)) {
                message("Name contains a banned word. Please try another name.", ChatType.ClanChat)
                return@interfaceOption
            }
            clan.name = name
            set("clan_name", name)
            interfaces.sendText(it.id, it.component, name)
            updateUI(clan)
        }

        interfaceClose("clan_chat_setup") {
            sendScript("clear_dialogues")
        }

        interfaceOption("Disable", "clan_chat_setup:name") {
            val clan = clan ?: ownClan ?: return@interfaceOption
            if (!clan.hasRank(this, ClanRank.Owner)) {
                message("Only the clan chat owner can do this.", ChatType.ClanChat)
                return@interfaceOption
            }
            clan.name = ""
            set("clan_name", "")
            interfaces.sendText(it.id, it.component, "Chat disabled")
            for (member in clan.members) {
                member.remove<Clan>("clan")
                member.clear("clan_chat")
                member.message("You have been kicked from the channel.", ChatType.ClanChat)
                member.client?.leaveClanChat()
            }
            clan.members.clear()
        }
    }

    fun updateUI(clan: Clan) {
        val membersList = clan.members.map { ClanMember.of(it, clan.getRank(it)) }
        for (member in clan.members) {
            member.client?.updateClanChat(clan.ownerDisplayName, clan.name, clan.kickRank.value, membersList)
        }
    }
}
