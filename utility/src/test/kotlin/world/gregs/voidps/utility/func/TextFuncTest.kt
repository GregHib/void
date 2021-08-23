package world.gregs.voidps.utility.func

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TextFuncTest {


    @Test
    fun `Title case to title case`() {
        assertEquals("Title Case 123", "Title Case 123".toTitleCase())
        assertEquals("123 Title Case", "123 Title Case".toTitleCase())
        assertEquals("Title  Case", "Title  Case".toTitleCase())
    }

    @Test
    fun `Pascal to title case`() {
        assertEquals("Pascal Case 123", "PascalCase123".toTitleCase())
        assertEquals("123 Pascal Case", "123PascalCase".toTitleCase())
        assertEquals("HTTP", "HTTP".toTitleCase())
    }

    @Test
    fun `Underscore to title case`() {
        assertEquals("Under Score 123", "under_score_123".toTitleCase())
        assertEquals("123 Under Score", "123_under_score".toTitleCase())
        assertEquals("Double  Underscore", "double__underscore".toTitleCase())
    }

    @Test
    fun `Lowercase to title case`() {
        assertEquals("Lower Case 123", "lower case 123".toTitleCase())
        assertEquals("123 Lower Case", "123 lower case".toTitleCase())
        assertEquals("Abc 123", "abc123".toTitleCase())
        assertEquals("123 Abc", "123abc".toTitleCase())
    }

    @Test
    fun `Hybrid case to title case`() {
        assertEquals("Pascal Title Under Lower Score", "PascalTitle under_lower score".toTitleCase())
    }


    @Test
    fun `Pascal case to pascal case`() {
        assertEquals("PascalCase123", "PascalCase123".toPascalCase())
        assertEquals("123PascalCase", "123PascalCase".toPascalCase())
    }

    @Test
    fun `Title case to pascal case`() {
        assertEquals("TitleCase123", "Title Case 123".toPascalCase())
        assertEquals("123TitleCase", "123 Title Case".toPascalCase())
        assertEquals("TitleCase", "Title  Case".toPascalCase())
    }

    @Test
    fun `Lowercase to pascal case`() {
        assertEquals("LowerCase123", "lower case 123".toPascalCase())
        assertEquals("123LowerCase", "123 lower case".toPascalCase())
        assertEquals("Abc123", "abc123".toPascalCase())
        assertEquals("123Abc", "123abc".toPascalCase(digitise = true))
        assertEquals("123abc", "123abc".toPascalCase(digitise = false))
    }

    @Test
    fun `Underscore to pascal case`() {
        assertEquals("UnderScore123", "under_score_123".toPascalCase())
        assertEquals("123UnderScore", "123_under_score".toPascalCase())
        assertEquals("DoubleUnderscore", "double__underscore".toPascalCase())
    }

    @Test
    fun `Hybrid case to pascal case`() {
        assertEquals("TitleCaseUnderLowerScore", "Title Case under_lower score".toPascalCase())
    }
}