package world.gregs.voidps.engine.view

/*

internal class NPCTrackingSetTest : KoinMock() {
    lateinit var set: NPCTrackingSet

    override val modules = listOf(eventModule, module {
        single(createdAtStart = true) { mockk<NPCDefinitions>(relaxed = true) }
    })

    @BeforeEach
    fun setup() {
        set = NPCTrackingSet()
    }

    @Test
    fun `Preparation fills removal set`() {
        // Given
        val npc = NPC(index = 1)
        set.locals.add(npc.index)
        set.total = 1
        // When
        set.start(null)
        // Then
        assert(set.remove(npc.index))
        assertEquals(0, set.total)
    }

    @Test
    fun `Tracking tracks self in total`() {
        // Given
        val client = NPC(index = 1)
        set.state.setRemoving(client.index)
        // When
        set.track(IntArrayList.of(client.index), client.index)
        // Then
        assertFalse(set.remove(client.index))
        assertEquals(1, set.total)
        assertEquals(0, set.addCount)
    }

    @Test
    fun `Update current sets`() {
        // Given
        val toAdd = NPC(index = 1)
        val toRemove = NPC(index = 2)
        val npc1 = NPC(index = 3)
        val npc2 = NPC(index = 4)
        set.locals.addAll(listOf(npc1.index, toRemove.index, npc2.index))
        set.state.setRemoving(toRemove.index)
        set.add[set.addCount++] = toAdd.index
        set.state.setAdding(toAdd.index)
        set.total = 3
        val npcs: NPCs = mockk()
        val indexer: IndexAllocator = mockk()
        every { indexer.cap } returns 5
        every { npcs.indexer } returns indexer
        // When
        set.update(npcs)
        // Then
        assertEquals(0, set.addCount)
        assertFalse(set.remove(toRemove.index))
        assertTrue(set.locals.contains(toAdd.index))
        assertFalse(set.locals.contains(toRemove.index))
        assertEquals(3, set.total)
    }

    @Test
    fun `Previously unseen entity is added`() {
        // Given
        val npc = NPC(index = 1)
        val entities = IntArrayList.of(npc.index)
        // When
        set.track(entities, -1)
        // Then
        assertTrue(set.add.contains(npc.index))
    }

    @Test
    fun `Tracked and seen entity is not removed`() {
        // Given
        val npc = NPC(index = 1)
        set.state.setRemoving(npc.index)
        val entities = IntArrayList.of(npc.index)
        // When
        set.track(entities, -1)
        // Then
        assertFalse(set.remove(npc.index))
        assertFalse(set.add.contains(npc.index))
    }

    @Test
    fun `Track exceeding maximum entities`() {
        // Given
        val npc = NPC(index = 11, tile = Tile(0))
        set.total = 10
        val entities = IntArrayList.of(npc.index)
        // When
        set.track(entities, -1)
        // Then
        assertFalse(set.add.contains(npc.index))
    }

    @Test
    fun `Track within view`() {
        // Given
        val npc = NPC(index = 1, tile = Tile(15, 15, 0))
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertTrue(set.add.contains(npc.index))
    }

    @Test
    fun `Track outside of view`() {
        // Given
        val npc = NPC(index = 1, tile = Tile(16, 16, 0))
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(npc.index))
    }

    @Test
    fun `Track within exceeding maximum entities`() {
        // Given
        val npc = NPC(index = 11, tile = Tile(15, 15, 0))
        set.total = 10
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(npc.index))
    }

    @Test
    fun `Track within exceeding maximum tick entities`() {
        // Given
        val npc = NPC(index = 5, tile = Tile(15, 15, 0))
        repeat(4) {
            set.add[set.addCount++] = it
        }
        val entities = setOf(npc)
        // When
        set.track(entities, null, 0, 0)
        // Then
        assertFalse(set.add.contains(npc.index))
    }

    @Test
    fun `Refresh all entities`() {
        // Given
        set.add[set.addCount++] = NPC(index = 1, tile = Tile(0)).index
        set.locals.add(NPC(index = 2, tile = Tile(0)).index)
        set.state.setRemoving(NPC(index = 3, tile = Tile(0)).index)
        set.total = 2
        // When
        set.clear()
        // Then
        assert(set.locals.isEmpty)
        assertTrue(set.remove(3))
        assertEquals(2, set.addCount)
        assertEquals(0, set.total)
    }
}*/
