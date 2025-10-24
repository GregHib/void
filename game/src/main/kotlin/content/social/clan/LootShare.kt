package content.social.clan

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

@Script
class LootShare : Api {

    override fun spawn(player: Player) {
        player.sendVariable("loot_share")
    }

    @Key("clan_loot_update,clan_loot_rank_update,clan_coin_share_update")
    override fun start(player: Player, timer: String, restart: Boolean) = when (timer) {
        "clan_loot_update" -> TimeUnit.MINUTES.toTicks(2)
        else -> TimeUnit.SECONDS.toTicks(30)
    }

    @Key("clan_loot_update,clan_loot_rank_update,clan_coin_share_update")
    override fun tick(player: Player, timer: String): Int {
        when (timer) {
            "clan_loot_update" -> {
                player["loading_loot_share"] = false
                val clan = player.clan ?: return Timer.CANCEL
                val lootShare = player.toggle("loot_share")
                ClanLootShare.update(player, clan, lootShare)
            }
            "clan_loot_rank_update" -> {
                val clan = player.clan ?: player.ownClan ?: return Timer.CANCEL
                clan.lootRank = ClanRank.valueOf(player["clan_loot_rank", "None"])
                for (member in clan.members) {
                    if (clan.hasRank(member, clan.lootRank) || !member["loot_share", false]) {
                        continue
                    }
                    ClanLootShare.update(player, clan, lootShare = false)
                }
            }
            "clan_coin_share_update" -> {
                val clan = player.clan ?: player.ownClan ?: return Timer.CANCEL
                clan.coinShare = player["coin_share_setting", false]
                for (member in clan.members) {
                    member["coin_share"] = clan.coinShare
                    member.message("CoinShare has been switched ${if (clan.coinShare) "on" else "off"}.", ChatType.ClanChat)
                }
            }
        }
        return Timer.CANCEL
    }

    init {
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
    }
}
