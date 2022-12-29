package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.path.strat.TileTargetStrategy

interface InteractiveEntity : Entity {
    var interactTarget: TileTargetStrategy
}