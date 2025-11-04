package content.social.clan

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

class LootShare : Script {

    init {
        playerSpawn {
            sendVariable("loot_share")
        }

        interfaceOption(id = "clan_chat:loot_share") {
            val clan = clan ?: return@interfaceOption
            if (clan.lootRank == ClanRank.None) {
                message("LootShare is disabled by the clan owner.", ChatType.ClanChat)
                return@interfaceOption
            }
            if (!clan.hasRank(this, clan.lootRank)) {
                message("Only ${clan.lootRank.name.lowercase()}s can share loot.", ChatType.ClanChat)
                return@interfaceOption
            }
            set("loading_loot_share", true)
            softTimers.start("clan_loot_update")
            val lootShare = get("loot_share", false)
            message("You will ${if (lootShare) "stop sharing" else "be able to share"} loot in 2 minutes.", ChatType.ClanChat)
        }

        timerStart("clan_loot_update") { TimeUnit.MINUTES.toTicks(2) }

        timerTick("clan_loot_update") {
            this["loading_loot_share"] = false
            val clan = clan ?: return@timerTick Timer.CANCEL
            val lootShare = toggle("loot_share")
            ClanLootShare.update(this, clan, lootShare)
            return@timerTick Timer.CANCEL
        }

        timerStart("clan_loot_rank_update") { TimeUnit.SECONDS.toTicks(30) }

        timerTick("clan_loot_rank_update") {
            val clan = clan ?: ownClan ?: return@timerTick Timer.CANCEL
            clan.lootRank = ClanRank.valueOf(this["clan_loot_rank", "None"])
            for (member in clan.members) {
                if (clan.hasRank(member, clan.lootRank) || !member["loot_share", false]) {
                    continue
                }
                ClanLootShare.update(this, clan, lootShare = false)
            }
            return@timerTick Timer.CANCEL
        }

        timerStart("clan_coin_share_update") { TimeUnit.SECONDS.toTicks(30) }

        timerTick("clan_coin_share_update") {
            val clan = clan ?: ownClan ?: return@timerTick Timer.CANCEL
            clan.coinShare = this["coin_share_setting", false]
            for (member in clan.members) {
                member["coin_share"] = clan.coinShare
                member.message("CoinShare has been switched ${if (clan.coinShare) "on" else "off"}.", ChatType.ClanChat)
            }
            return@timerTick Timer.CANCEL
        }
    }
}
