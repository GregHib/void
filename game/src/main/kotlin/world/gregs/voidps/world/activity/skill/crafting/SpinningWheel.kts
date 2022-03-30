import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.makeAmountIndex


on<ObjectOption>({ obj.id.startsWith("spinning_wheel") && option == "Spin" }) { player: Player ->
    player.dialogue {
        val items = listOf(
            "ball_of_wool",
            "golden_fleece",
            "bow_string",
            "crossbow_string",
            "crossbow_string",
            "magic_string",
            "rope"
        )
        val (index, amount) = makeAmountIndex(
            items = items,
            names = listOf(
                "Ball of wool<br>(Wool)",
                "Golden fleece<br>(Golden wool)",
                "Bow string<br>(Flax)",
                "C'bow string<br>(Sinew)",
                "C'bow string<br>(Tree roots)",
                "Magic string<br>(Magic roots)",
                "Rope<br>(Yak hair)",
            ),
            type = "Make",
            maximum = 28,
            text = "How many would you like to make?"
        )
        val list = listOf(
            "wool",
            "golden_wool",
            "flax",
            "sinew",
            "tree_roots",
            "magic_roots",
            "yak_hair"
        )

        val consume = items[index]
        val product = list[index]

        println("Selected $index $amount")
    }
}