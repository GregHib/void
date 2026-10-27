package content.entity.player.modal.book

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.get

object BooksLoader : Loader<Books> {
    val instance = Books()
    override val name = "books"
    override val label = "book"
    override val files = listOf("definitions.books")

    override fun load(context: LoadContext): Books = Books().load(context.paths(this))

    override fun total(state: Books) = state.titles.size

    override fun install(state: Books) = instance.copyFrom(state)
}
