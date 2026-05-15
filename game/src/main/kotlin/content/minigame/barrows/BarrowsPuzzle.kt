package content.minigame.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.pauseInt
import world.gregs.voidps.type.random

class BarrowsPuzzle : Script {
    init {
        interfaceOption("Choose", "barrows_puzzle:choice*") {
            val int = it.component.removePrefix("choice").toInt()
            (suspension as? Suspension.IntEntry)?.resume(int)
        }
    }
}

suspend fun Player.puzzle(): Boolean {
    val id = "barrows_puzzle"
    val puzzle = Tables.get("barrows_puzzles").rows().random(random)
    check(open(id)) { "Unable to open puzzle dialogue for $this" }
    val options = puzzle.intList("options")
    for (i in 0 until 3) {
        interfaces.sendModel(id, "option${i + 1}", options[i])
    }
    val choices = puzzle.intList("choices").shuffled()
    for (i in 0 until 3) {
        interfaces.sendModel(id, "choice${i + 1}", choices[i])
    }
    val result = pauseInt()
    close(id)
    val pick = choices[result - 1]
    return pick == puzzle.int("answer")
}
