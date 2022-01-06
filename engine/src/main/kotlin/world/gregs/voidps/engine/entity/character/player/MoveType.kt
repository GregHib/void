package world.gregs.voidps.engine.entity.character.player

enum class MoveType(val id: Int) {
    None(0),
    Walk(1),
    Run(2),
    Crawl(-1),
    Teleport(127)
}