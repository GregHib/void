import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.Orange
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.data.Tanning
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

val itemDefs: ItemDefinitions by inject()

on<NPCOption>({ npc.id == "ellis" && option == "Talk-to" }) { player: Player ->
    npc("talk", "Greetings friend. I am a manufacturer of leather.")
    if (player.inventory.items.none { it.id == "cowhide" || it.id.startsWith("snake_hide") || it.id.endsWith("dragonhide") }) {
        leather()
        return@on
    }
    npc("talk", """
        I see you have bought me some hides.
        Would you like me to tan them for you?
    """)
    val choice = choice("""
        Yes please.
        No thanks.
    """)
    if (choice == 1) {
        player("talk", "Yes please.")
        player.open("tanner")
    } else if (choice == 2) {
        player("sad", "No thanks.")
        npc("talk", "Very well, ${if (player.male) "sir" else "madam"}, as you wish.")
    }
}

on<NPCOption>({ npc.id == "ellis" && option == "Trade" }) { player: Player ->
    player.open("tanner")
}

suspend fun NPCOption.leather() {
    val choice = choice(
        title = "What would you like to say?",
        text = """
            Can I buy some leather then?
            Leather is rather weak stuff.
        """
    )
    if (choice == 1) {
        player("unsure", "Can I buy some leather then?")
        npc("talk", """
            I make leather from animal hides. Bring me some
            cowhides and one gold coin per hide, and I'll tan them
            into soft leather for you.
        """)
    } else if (choice == 2) {
        player("talk", "Leather is rather weak stuff.")
        npc("talk", """
            Normal leather may be quite weak, but it's very heap -
            I make it from cowhides for only 1 gp per hide - and
            it's so easy to craft that anyone can work with it.
        """)
        npc("talk", """
            Alternatively you could try hard leather. It's not so
            easy to craft, but I only charge 3 gp per cowhide to
            prepare it, and it makes much sturdier armour.
        """)
        npc("cheerful", """
            I can also tan snake hides and dragonhides, suitable for
            crafting into the highest quality armour for rangers.
        """)
        player("talk", "Thanks, I'll bear it in mind.")
    }
}

on<InterfaceOption>({ id == "tanner" && option.lowercase() == "tan ${Orange.open("X")}" }) { player: Player ->
    val amount = intEntry("Enter amount:")
    player.setVar("last_bank_amount", amount)
    tan(player, component, amount)
}

on<InterfaceOption>({ id == "tanner" && option.startsWith("Tan") && !option.endsWith("X") }) { player: Player ->
    val amount = when (option.lowercase()) {
        "tan ${Orange.open("1")}" -> 1
        "tan ${Orange.open("5")}" -> 5
        "tan ${Orange.open("10")}" -> 10
        "tan ${Orange.open("all")}" -> player.inventory.count(component.removeSuffix("_1"))
        else -> return@on
    }
    tan(player, component, amount)
}

fun tan(player: Player, type: String, amount: Int) {
    val item = type.removeSuffix("_1")
    if (!player.hasItem(item)) {
        player.message("You don't have any ${item.toLowerSpaceCase()} to tan.")
        return
    }
    val tanning: Tanning = itemDefs.get(item)["tanning"]
    val (leather, cost) = tanning.prices[if (type.endsWith("_1")) 1 else 0]
    var tanned = 0
    var noHides = false
    for (i in 0 until amount) {
        if (!player.inventory.transaction {
                replace(item, leather)
                if (failed) {
                    noHides = true
                }
                remove("coins", cost)
            }) {
            break
        }
        tanned++
    }
    if (tanned == 1) {
        player.message("The tanner tans your ${item.toLowerSpaceCase()}.")
    } else if (tanned > 0) {
        player.message("The tanner tans $tanned ${item.toLowerSpaceCase().plural(tanned)} for you.")
    }
    if (noHides) {
        player.message("You have run out of ${item.plural().toLowerSpaceCase()}.")
    } else if (tanned < amount) {
        player.message("You haven't got enough coins to pay for ${if (tanned == 0) "" else "more "}${leather.toLowerSpaceCase()}.")
    }
}