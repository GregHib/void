package world.gregs.voidps.cache

interface DefinitionLoader {

    fun <T : Definition> load(decoder: DefinitionDecoder<T>): Array<T>
}