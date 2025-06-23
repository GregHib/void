package content.entity.player.inv.item

import world.gregs.voidps.engine.entity.item.Item

val Item.tradeable: Boolean
    get() = def["tradeable", true]
