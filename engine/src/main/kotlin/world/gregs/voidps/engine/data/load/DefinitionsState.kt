package world.gregs.voidps.engine.data.load

/**
 * Everything a definitions singleton holds after loading: the definitions by id and their string ids.
 */
class DefinitionsState<D : Any>(
    val definitions: Array<D>,
    val ids: Map<String, Int>,
)
