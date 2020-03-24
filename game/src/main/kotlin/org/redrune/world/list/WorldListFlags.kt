package org.redrune.world.list

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
object WorldListFlags {

    /**
     * The const value for Australia.
     */
    const val COUNTRY_AUSTRALIA = 16

    /**
     * The const value for Belgium.
     */
    const val COUNTRY_BELGIUM = 22

    /**
     * The const value for Brazil.
     */
    const val COUNTRY_BRAZIL = 31

    /**
     * The const value for Canada.
     */
    const val COUNTRY_CANADA = 38

    /**
     * The const value for Denmark.
     */
    const val COUNTRY_DENMARK = 58

    /**
     * The const value for Finland.
     */
    const val COUNTRY_FINLAND = 69

    /**
     * The const value for Ireland.
     */
    const val COUNTRY_IRELAND = 101

    /**
     * The const value for Mexico.
     */
    const val COUNTRY_MEXICO = 152

    /**
     * The const value for the Netherlands.
     */
    const val COUNTRY_NETHERLANDS = 161

    /**
     * The const value for Norway.
     */
    const val COUNTRY_NORWAY = 162

    /**
     * The const value for Sweden.
     */
    const val COUNTRY_SWEDEN = 191

    /**
     * The const value for the UK.
     */
    const val COUNTRY_UK = 77


    /**
     * If the world is free to play.
     */
    const val FLAG_NON_MEMBERS = 0x0

    /**
     * If the world is a members world.
     */
    const val FLAG_MEMBERS = 0x1

    /**
     * If the world is a PvP-world.
     */
    const val FLAG_PVP = 0x4

    /**
     * If the world is a lootshare world.
     */
    const val FLAG_LOOTSHARE = 0x8

    /**
     * If the world should be highlighted.
     */
    const val FLAG_HIGHLIGHT = 0x10

    /**
     * If the world is a high risk wilderness world
     */
    const val FLAG_HIGH_RISK = 0x400

    /**
     * The const value for USA.
     */
    const val COUNTRY_USA = 225
}