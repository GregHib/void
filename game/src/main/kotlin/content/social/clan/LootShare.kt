package content.social.clan

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import world.gregs.voidps.engine.event.Script
@Script
class LootShare {

    init {
        playerSpawn { player ->
            player.sendVariable("loot_share")
        }

        interfaceOption(component = "loot_share", id = "clan_chat") {
            val clan = player.clan ?: return@interfaceOption
            if (clan.lootRank == ClanRank.None) {
                player.message("LootShare is disabled by the clan owner.", ChatType.ClanChat)
                return@interfaceOption
            }
            if (!clan.hasRank(player, clan.lootRank)) {
                player.message("Only ${clan.lootRank.name.lowercase()}s can share loot.", ChatType.ClanChat)
                return@interfaceOption
            }
            player["loading_loot_share"] = true
            player.softTimers.start("clan_loot_update")
            val lootShare = player["loot_share", false]
            player.message("You will ${if (lootShare) "stop sharing" else "be able to share"} loot in 2 minutes.", ChatType.ClanChat)
        }

        timerStart("clan_loot_update") {
            interval = TimeUnit.MINUTES.toTicks(2)
        }

        timerStart("clan_loot_update") { player ->
            cancel()
            player["loading_loot_share"] = false
            val clan = player.clan ?: return@timerStart
            val lootShare = player.toggle("loot_share")
            ClanLootShare.update(player, clan, lootShare)
        }

        timerStart("clan_loot_rank_update", "clan_coin_share_update") {
            interval = TimeUnit.SECONDS.toTicks(30)
        }

        timerTick("clan_loot_rank_update") { player ->
            cancel()
            val clan = player.clan ?: player.ownClan ?: return@timerTick
            clan.lootRank = ClanRank.valueOf(player["clan_loot_rank", "None"])
            for (member in clan.members) {
                if (clan.hasRank(member, clan.lootRank) || !member["loot_share", false]) {
                    continue
                }
                ClanLootShare.update(player, clan, lootShare = false)
            }
        }

        timerTick("clan_coin_share_update") { player ->
            cancel()
            val clan = player.clan ?: player.ownClan ?: return@timerTick
            clan.coinShare = player["coin_share_setting", false]
            for (member in clan.members) {
                member["coin_share"] = clan.coinShare
                member.message("CoinShare has been switched ${if (clan.coinShare) "on" else "off"}.", ChatType.ClanChat)
            }
        }

    }

}
