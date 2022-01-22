package world.gregs.voidps.world.community.chat

import world.gregs.voidps.engine.client.privateStatus
import world.gregs.voidps.engine.client.publicStatus
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set

var Player.publicStatus: String
    get() = get("public_status", "on")
    set(value) {
        set("public_status", true, value)
        publicStatus(value, tradeStatus)
    }

var Player.privateStatus: String
    get() = get("private_status", "on")
    set(value) {
        set("private_status", true, value)
        privateStatus(value)
    }

var Player.tradeStatus: String
    get() = get("trade_status", "on")
    set(value) {
        set("trade_status", true, value)
        publicStatus(publicStatus, value)
    }