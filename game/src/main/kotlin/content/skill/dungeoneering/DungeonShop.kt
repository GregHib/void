package content.skill.dungeoneering

import content.entity.npc.shop.buy.ShopBuy
import content.entity.npc.shop.sell.ShopSell
import content.entity.npc.shop.stock.ItemInfo
import content.entity.player.inv.item.drop.ItemDropping
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class DungeonShop : Script {
    init {
        interfaceOpened("dungeon_shop") {
            open("dungeon_shop_side")
        }

        interfaceRefresh("dungeon_shop") {

            interfaceOptions.unlockAll("dungeon_shop", "stock", 0 until 500)
        }

        interfaceRefresh("dungeon_shop_side") {
            interfaceOptions.send("dungeon_shop_side", "inventory")
            interfaceOptions.unlockAll("dungeon_shop_side", "inventory", 0 until 28)
        }

        interfaceClosed("dungeon_shop") {
            close("dungeon_shop_side")
            close("item_info")
        }

        interfaceOption("Info", id = "dungeon_shop:stock") {
            val actualIndex = (it.itemSlot - 2) / 5
            val item = shopItem(actualIndex)
            set("info_sample", false)
            set("info_index", actualIndex)
            set("item_info_currency", "rusty_coins")
            val price = buyPrice(item)
            ItemInfo.showInfo(this, item, price)
        }

        interfaceOption(id = "dungeon_shop:stock") {
            val amount = when (it.option) {
                "Buy-1" -> 1
                "Buy-5" -> 5
                "Buy-10" -> 10
                "Buy-50" -> 50
                "Buy-250" -> 250
                else -> return@interfaceOption
            }
            val item = shopItem((it.itemSlot - 2) / 5)
            buy(this, item, amount)
        }

        interfaceOption("Value", "dungeon_shop_side:inventory") { (item) ->
            val (resources, tools) = shopEnums()
            if (!sellable(item)) {
                message("You can't sell this item to this shop.")
                return@interfaceOption
            }
            val price = sellPrice(item)
            message("${item.def.name}: shop will buy for $price coins.")
        }

        interfaceOption(id = "dungeon_shop_side:inventory") { (item, slot, option) ->
            val amount = when (option) {
                "Sell 1" -> 1
                "Sell 5" -> 5
                "Sell 10" -> 10
                "Sell 50" -> 50
                "Drop" -> return@interfaceOption ItemDropping.drop(this, item, slot)
                else -> return@interfaceOption
            }
            if (!sellable(item)) {
                message("You can't sell this item to this shop.")
                return@interfaceOption
            }
            set("shop_currency", "rusty_coins")
            ShopSell.sell(this, item, amount, 0.3)
        }
    }

    private fun sellable(item: Item): Boolean {
        // TODO journals + puzzle room items
        return item.def.contains(Params.DUNGEONEERING) &&
            item.id != "rusty_coins" &&
            item.id != "gatestone" &&
            item.id != "ground_gatestone" &&
            !item.def.contains(Params.DUNGEONEERING_BOUND_ITEM) &&
            !item.def.contains(Params.DUNGEONEERING_BOUND_AMMO)
    }

    companion object {
        fun buy(player: Player, item: Item, amount: Int) {
            val price = buyPrice(item)
            // https://youtu.be/tg1Kf4iAkN4?t=337
            player["shop_currency"] = "rusty_coins"
            ShopBuy.buy(player, item, amount, price, deficient = "You can't afford that many.")
        }

        private fun buyPrice(item: Item): Int = item.def.cost * item.def[Params.DUNGEONEERING_SHOP_MULTIPLIER, 100] / 100
        private fun sellPrice(item: Item): Int = (item.def.cost * 0.3).toInt()

        private fun Player.shopItem(index: Int): Item {
            val (resources, tools) = shopEnums()
            if (index < resources.length) {
                val int = resources.int(index)
                return Item(ItemDefinitions.get(int).stringId)
            } else {
                val id = tools.int(index - resources.length)
                return Item(ItemDefinitions.get(id).stringId)
            }
        }

        private fun Player.shopEnums(): Pair<EnumDefinition, EnumDefinition> {
            var resourceEnum = "rand_shop_sold_resources"
            var toolEnum = "rand_shop_sold_tools"
            if (!World.members) {
                resourceEnum += "_f2p"
                toolEnum += "_f2p"
            }
            when (get("rand_party_complexity_level_trans", 1)) {
                2 -> {
                    resourceEnum += "_c2"
                    toolEnum += "_c2"
                }
                3 -> {
                    resourceEnum += "_c3"
                    toolEnum += "_c3"
                }
                4 -> {
                    resourceEnum += "_c4"
                    toolEnum += "_c4"
                }
            }
            val resources = EnumDefinitions.get(resourceEnum)
            val tools = EnumDefinitions.get(toolEnum)
            return Pair(resources, tools)
        }
    }
}
