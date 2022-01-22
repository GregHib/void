package world.gregs.voidps.engine.entity.character

import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.engine.map.TileMap

internal class CharacterListTest {

    private lateinit var list: CharacterList<Character>
    private lateinit var tileMap: TileMap<Character>

    @BeforeEach
    fun setup() {
        list = object : CharacterList<Character>(10) {}
    }

}