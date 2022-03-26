import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitInterface
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.dialogue.type.statement

on<Registered>(priority = Priority.HIGHEST) { player: Player ->
    player.message("Welcome to Void.", ChatType.Welcome)
    if (System.currentTimeMillis() - player["creation", 0L] < 2000) {
        player.action(ActionType.Makeover) {
            try {
                delay(1)
                if (!player.isBot) {
                    player.open("character_creation")
                    awaitInterface("character_creation")
                }
            } finally {
                player.close("character_creation")
                player.flagAppearance()
                setup(player)
            }
        }
    }
}

fun setup(player: Player) {
    player.dialogue {
        statement("""
            Welcome to Lumbridge! To get more help, simply click on the
            Lumbridge Guide or one of the Tutors - these can be found by
            looking for the question mark icon on your minimap. If you find you
            are lost at any time, look for a signpost or use the Lumbridge Home
            Teleport spell.
        """)
    }
    player.bank.add("coins", 25)
    player.inventory.add("bronze_hatchet")
    player.inventory.add("tinderbox")
    player.inventory.add("small_fishing_net")
    player.inventory.add("shrimp")
    player.inventory.add("empty_bucket")
    player.inventory.add("empty_pot")
    player.inventory.add("bread")
    player.inventory.add("bronze_pickaxe")
    player.inventory.add("bronze_dagger")
    player.inventory.add("bronze_sword")
    player.inventory.add("wooden_shield")
    player.inventory.add("shortbow")
    player.inventory.add("bronze_arrow", 25)
    player.inventory.add("air_rune", 25)
    player.inventory.add("mind_rune", 15)
    player.inventory.add("water_rune", 6)
    player.inventory.add("earth_rune", 4)
    player.inventory.add("body_rune", 2)
}