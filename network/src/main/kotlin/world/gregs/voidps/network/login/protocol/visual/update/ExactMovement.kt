package world.gregs.voidps.network.login.protocol.visual.update

import world.gregs.voidps.network.login.protocol.Visual

data class ExactMovement(
    var startX: Int = 0,
    var startY: Int = 0,
    var startDelay: Int = 0,
    var endX: Int = 0,
    var endY: Int = 0,
    var endDelay: Int = 0,
    var direction: Int = 0
) : Visual