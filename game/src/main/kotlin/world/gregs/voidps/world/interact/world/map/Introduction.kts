package world.gregs.voidps.world.interact.world.map

import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.dialogue.type.statement

on<Registered>(priority = Priority.HIGHER) { player: Player ->
    player.message("Welcome to Void.", ChatType.Welcome)
    if (!player.contains("creation")) {
        if (!player.isBot) {
            player.start("delay", -1)
            World.run("welcome_${player.name}", 1) {
                player.open("character_creation")
            }
        }
    }
}

on<InterfaceClosed>({ id == "character_creation" }) { player: Player ->
    player.flagAppearance()
    setup(player)
}

fun setup(player: Player) {
    player.queue("welcome") {
        statement("""
            Welcome to Lumbridge! To get more help, simply click on the
            Lumbridge Guide or one of the Tutors - these can be found by
            looking for the question mark icon on your minimap. If you find you
            are lost at any time, look for a signpost or use the Lumbridge Home
            Teleport spell.
        """)
    }
    player.stop("delay")
    player["creation"] = System.currentTimeMillis()
    player.bank.add("coins", 25)
    player.inventory.apply {
        add("bronze_hatchet")
        add("tinderbox")
        add("small_fishing_net")
        add("shrimp")
        add("bucket")
        add("empty_pot")
        add("bread")
        add("bronze_pickaxe")
        add("bronze_dagger")
        add("bronze_sword")
        add("wooden_shield")
        add("shortbow")
        add("bronze_arrow", 25)
        add("air_rune", 25)
        add("mind_rune", 15)
        add("water_rune", 6)
        add("earth_rune", 4)
        add("body_rune", 2)
    }
}