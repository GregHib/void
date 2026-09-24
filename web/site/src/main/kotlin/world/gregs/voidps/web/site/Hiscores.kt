package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.web.site.components.*

/**
 * The player hiscores: overall/skill/boss leaderboards, a head-to-head comparison, and a
 * player profile view. The tile grid, filter chips and pagination controls are plain Kotlin +
 * [xToggleStyle] since the skill/boss/mode names are known at build time; the ranked tables
 * themselves are fetched from the real hiscores and players endpoints under `/api/v1` by
 * `js/hiscores.js` (`hiscoresApp()`), so those rows are emitted as `<template x-for>` blocks
 * instead of being rendered server-side. Skill/boss names and order come from [GameData], which
 * this page also serializes onto `window.VOID_SKILLS`/`VOID_BOSSES` for `hiscores.js` to read.
 */
object Hiscores {

    fun page(gameData: GameData): String = voidPage(
        title = "Void — hiscores",
        description = "Live overall, skill and boss leaderboards for Void, with head-to-head player comparisons.",
        head = {
            script { unsafe { raw(gameData.script()) } }
            script(src = "js/hiscores.js") {}
        },
    ) {
        ui.siteHeader(Website.pages, active = "hiscores", communityPages = Website.communityPages)

        div {
            xData("hiscoresApp()")

            ui.pageHeader(
                eyebrow = "Hiscores",
                title = "Player hiscores",
                description = "Compare skills and kills to your friends and fellow adventurers.",
                backgroundImage = "images/bg/hiscores.jpg",
                actions = {
                    div {
                        style = "display:flex;align-items:flex-end;gap:var(--space-4)"
                        ui.textInput(
                            "hs-search", "Find a player", model = "query", placeholder = "Name",
                            icon = Icons.SEARCH, onEnter = "search()",
                        )
                        ui.button("Search", size = ButtonSize.Medium, onClick = "search()")
                    }
                },
            )

            div {
                style = "max-width:var(--container-wide);width:100%;margin:0 auto;padding:0 var(--space-7);" +
                    "display:flex;align-items:center;justify-content:space-between;gap:var(--space-6);flex-wrap:wrap"
                ui.tabs(
                    model = "view",
                    items = listOf(
                        TabItem("overall", "Overall"),
                        TabItem("skills", "Skills"),
                        TabItem("compare", "Compare"),
                        TabItem("bosses", "Bosses"),
                    ),
                    filled = false,
                    onSelect = { id -> "navigate({ view: '$id' })" },
                )
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);white-space:nowrap"
                    attributes["x-text"] = "updatedLabel"
                }
            }

            main {
                style = "max-width:var(--container-wide);width:100%;margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-12);" +
                    "display:flex;flex-direction:column;gap:var(--space-8)"

                overallView()
                skillsView()
                compareView()
                bossesView(gameData)
                searchView()
                playerView()
            }
        }

        ui.siteFooter()
    }

    private fun FlowContent.paginationFooter(pagerExpr: String, key: String) {
        div {
            style = "display:flex;align-items:center;justify-content:space-between;gap:var(--space-6);" +
                "padding:var(--space-5) var(--space-6);background:var(--umber-900);flex-wrap:wrap"
            span {
                style = "font:var(--type-body-sm);color:var(--text-faint)"
                attributes["x-text"] = "$pagerExpr.label"
            }
            div {
                style = "display:flex;gap:var(--space-4)"
                ui.button(
                    "← Previous", variant = ButtonVariant.Secondary, size = ButtonSize.Small,
                    onClick = "prevPage('$key')", disabledExpression = "$pagerExpr.prevDisabled",
                )
                ui.button(
                    "Next →", variant = ButtonVariant.Secondary, size = ButtonSize.Small,
                    onClick = "nextPage('$key')", disabledExpression = "$pagerExpr.nextDisabled",
                )
            }
        }
    }

    private fun FlowContent.skillTileGrid() {
        div {
            attributes["class"] = "void-grid"
            style = "grid-template-columns:repeat(2,minmax(0,1fr));gap:1px;background:var(--umber-900)"
            for ((index, skill) in Skill.entries.withIndex()) {
                val name = skill.name
                val isLastAlone = index == Skill.entries.lastIndex && Skill.entries.size % 2 != 0
                button {
                    onClick("navigate({ skill: '$name', skillPage: 0, view: 'skills' })")
                    xToggleStyle(
                        condition = "view === 'skills' && skill === '$name'",
                        whenTrue = "background:var(--surface-active);color:var(--gold-200);border-left-color:var(--gold-400)",
                        whenFalse = "background:var(--surface-panel);color:var(--parch-200);border-left-color:transparent",
                    )
                    val span = if (isLastAlone) "grid-column:1 / -1;" else ""
                    style = "$span display:flex;align-items:center;gap:var(--space-4);padding:9px 10px;cursor:pointer;text-align:left;" +
                        "border:none;border-left:2px solid transparent;background:var(--surface-panel);color:var(--parch-200);" +
                        "font:var(--weight-semibold) var(--text-xs)/1.2 var(--font-ui);letter-spacing:var(--tracking-wide)"
                    span {
                        style = "width:18px;height:18px;flex:none;display:flex;align-items:center;justify-content:center"
                        img(src = "images/skills/${skill.name.lowercase()}.png", alt = "") {
                            style = "max-width:100%;max-height:100%;width:auto;height:auto;display:block"
                        }
                    }
                    span {
                        style = "white-space:nowrap;overflow:hidden;text-overflow:ellipsis"
                        +name
                    }
                }
            }
        }
    }

    private fun FlowContent.modeFilterRow() {
        div {
            style = "display:flex;align-items:center;gap:var(--space-5);flex-wrap:wrap"
            span {
                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                +"Account type"
            }
            div {
                style = "display:flex;gap:var(--space-2);flex-wrap:wrap"
                for ((id, label) in listOf("all" to "All", "main" to "Main", "skiller" to "Skiller", "pure" to "Pure")) {
                    ui.filterChip(active = "mode === '$id'", onClickExpr = "mode = '$id'; page = 0; fetchOverall()", label = label)
                }
            }
        }
    }

    private fun FlowContent.teamChipsRow() {
        div {
            style = "display:flex;align-items:center;gap:var(--space-4);flex-wrap:wrap;padding:var(--space-4) var(--space-6);" +
                "background:var(--surface-header);border-bottom:1px solid var(--border-panel)"
            span {
                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                +"Team size"
            }
            div {
                style = "display:flex;gap:var(--space-2);flex-wrap:wrap"
                for (t in listOf("All", "Solo", "2 players", "3 players", "4 players")) {
                    ui.filterChip(active = "team === '$t'", onClickExpr = "team = '$t'; timePage = 0; fetchBossTimes()", label = t)
                }
            }
        }
    }

    private fun FlowContent.bossTileGrid(gameData: GameData) {
        div {
            attributes["class"] = "void-grid"
            style = "grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:1px;background:var(--umber-900)"
            for ((id, name) in gameData.bosses) {
                button {
                    onClick("navigate({ boss: '${id}', kcPage: 0, timePage: 0, view: 'bosses' })")
                    xToggleStyle(
                        condition = "boss === '${id}'",
                        whenTrue = "background:var(--surface-active);color:var(--gold-200);border-top-color:var(--gold-400)",
                        whenFalse = "background:var(--surface-panel);color:var(--text-strong);border-top-color:transparent",
                    )
                    style = "display:flex;align-items:center;gap:var(--space-4);padding:var(--space-5);cursor:pointer;text-align:left;" +
                        "border:none;border-top:2px solid transparent;background:var(--surface-panel);color:var(--text-strong)"
                    span {
                        style = "flex:0 0 auto;width:32px;height:32px;display:flex;align-items:center;justify-content:center;" +
                            "background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);" +
                            "font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint);position:relative;overflow:hidden"
                        span { +GameData.bossAbbr(name) }
                        img(src = GameData.bossIcon(name), alt = "") {
                            attributes["loading"] = "lazy"
                            attributes["onerror"] = "this.remove()"
                            style = "position:absolute;inset:0;width:100%;height:100%;object-fit:contain;background:var(--surface-inset)"
                        }
                    }
                    span {
                        style = "font:var(--weight-semibold) var(--text-base)/1.2 var(--font-display)"
                        +name
                    }
                }
            }
        }
    }

    private fun FlowContent.comboInput(side: String, label: String) {
        div {
            style = "position:relative;display:flex;flex-direction:column;gap:var(--space-3);min-width:0"
            span {
                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                +label
            }
            input {
                attributes["x-bind:value"] = "comboValue('$side')"
                attributes["@input"] = "comboQ = \$event.target.value; combo = '$side'; comboSearch('$side')"
                attributes["@focus"] = "comboFocus('$side')"
                attributes["@blur"] = "comboBlur()"
                placeholder = "Search players"
                style = "width:100%;height:36px;padding:0 10px;background:var(--surface-inset);border:1px solid var(--border-strong);" +
                    "border-radius:var(--radius-sm);box-shadow:var(--bevel-down);color:var(--text-strong);font:var(--type-body-sm);outline:none"
            }
            div {
                xShow("combo === '$side'")
                attributes["class"] = "void-flex"
                style = "flex-direction:column;position:absolute;top:100%;left:0;right:0;z-index:30;margin-top:var(--space-2);" +
                    "max-height:280px;overflow:auto;background:var(--surface-panel);border:1px solid var(--border-panel);" +
                    "border-radius:var(--radius-md);box-shadow:var(--shadow-md)"
                unsafe {
                    raw(
                        """
                        <template x-for="o in comboResults('$side')" :key="o.name">
                          <div @mousedown.prevent="pickCombo('$side', o.name)" style="display:flex;align-items:center;justify-content:space-between;gap:var(--space-5);padding:var(--space-4) var(--space-5);cursor:pointer;border-bottom:1px solid var(--umber-900)">
                            <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-strong)" x-text="o.name"></span>
                            <span style="font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint);white-space:nowrap" x-text="o.meta"></span>
                          </div>
                        </template>
                        """.trimIndent(),
                    )
                }
            }
        }
    }

    private fun FlowContent.overallView() {
        div {
            xShow("view === 'overall'")
            attributes["class"] = "void-grid hiscores-split"
            style = "grid-template-columns:minmax(0,260px) minmax(0,1fr);gap:var(--space-8);align-items:start"

            ui.panel(title = "Skills", padded = false) {
                skillTileGrid()
            }

            div {
                style = "display:flex;flex-direction:column;gap:var(--space-6);min-width:0"
                modeFilterRow()
                ui.panel(title = "Overall", action = eyebrowText("overallEyebrow"), padded = false) {
                    tableScroll(
                        Column("Rank", "76px"), Column("Player", "minmax(0,1fr)"),
                        Column("Total lvl", "130px", "right"), Column("Total xp", "170px", "right"),
                    ) {
                        unsafe {
                            raw(
                                """
                                <template x-for="(row,i) in overallRows" :key="row.name">
                                  <div @click="open(row.name)" :style="{ background: row.bg }" style="display:grid;grid-template-columns:76px minmax(0,1fr) 130px 170px;align-items:center;padding:var(--space-4) var(--space-6);cursor:pointer;border-bottom:1px solid var(--umber-900);transition:background var(--dur-fast) var(--ease-standard)">
                                    <span :style="{ color: row.rankColor }" style="font:var(--weight-bold) var(--text-lg)/1 var(--font-display)" x-text="row.rank"></span>
                                    <span style="display:flex;align-items:center;gap:var(--space-4);min-width:0">
                                      <span style="font:var(--weight-semibold) var(--text-base)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="row.name"></span>
                                      <span x-show="row.showBadge" class="void-flex" :style="{ background: row.badgeBg, color: row.badgeFg, borderColor: row.badgeBd }" style="align-items:center;gap:var(--space-3);padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="row.mode"></span>
                                    </span>
                                    <span style="text-align:right;font:var(--type-code);color:var(--text-body)" x-text="row.totalLevel"></span>
                                    <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="row.totalXp"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                    paginationFooter("overallPager", "page")
                }
            }
        }
    }

    private fun FlowContent.skillsView() {
        div {
            xShow("view === 'skills'")
            attributes["class"] = "void-grid hiscores-split"
            style = "grid-template-columns:minmax(0,260px) minmax(0,1fr);gap:var(--space-8);align-items:start"

            ui.panel(title = "Skills", padded = false) { skillTileGrid() }

            ui.panel(title = "Skill leaderboard", action = eyebrowText("skillEyebrow"), padded = false) {
                tableScroll(
                    Column("Rank", "76px"), Column("Player", "minmax(0,1fr)"),
                    Column("Level", "100px", "right"), Column("Xp", "170px", "right"),
                ) {
                    unsafe {
                        raw(
                            """
                            <template x-for="(row,i) in skillRows" :key="row.name">
                              <div @click="open(row.name)" :style="{ background: row.bg }" style="display:grid;grid-template-columns:76px minmax(0,1fr) 100px 170px;align-items:center;padding:var(--space-4) var(--space-6);cursor:pointer;border-bottom:1px solid var(--umber-900)">
                                <span :style="{ color: row.rankColor }" style="font:var(--weight-bold) var(--text-lg)/1 var(--font-display)" x-text="row.rank"></span>
                                <span style="font:var(--weight-semibold) var(--text-base)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="row.name"></span>
                                <span style="text-align:right;font:var(--type-code);color:var(--text-body)" x-text="row.level"></span>
                                <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="row.xp"></span>
                              </div>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
                paginationFooter("skillPager", "skillPage")
            }
        }
    }

    private fun FlowContent.compareView() {
        div {
            xShow("view === 'compare'")
            attributes["class"] = "void-flex"
            style = "flex-direction:column;gap:var(--space-8)"

            ui.panel(title = "Compare") {
                div {
                    attributes["class"] = "void-grid hiscores-compare-grid"
                    style = "grid-template-columns:minmax(0,1fr) 64px minmax(0,1fr);gap:var(--space-6);align-items:end"
                    comboInput("a", "Player one")
                    span {
                        style = "font:var(--weight-bold) var(--text-xl)/1 var(--font-display);color:var(--text-faint);" +
                            "text-align:center;padding-bottom:9px"
                        +"vs"
                    }
                    comboInput("b", "Player two")
                }
                div {
                    xShow("!compareReady")
                    style = "margin-top:var(--space-7);padding:var(--space-7);text-align:center;background:var(--surface-inset);" +
                        "border:1px solid var(--border-panel);border-radius:var(--radius-md);box-shadow:var(--bevel-down)"
                    span {
                        style = "font:var(--type-body-sm);color:var(--text-muted)"
                        attributes["x-text"] = "compareBlankText"
                    }
                }
                div {
                    xShow("compareReady")
                    attributes["class"] = "void-grid"
                    style = "grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:var(--space-5);margin-top:var(--space-7)"
                    unsafe {
                        raw(
                            """
                            <template x-for="card in compareSummary" :key="card.label">
                              <div style="padding:var(--space-5);background:var(--surface-inset);border:1px solid var(--border-panel);border-radius:var(--radius-md);box-shadow:var(--bevel-down);display:flex;flex-direction:column;gap:var(--space-3)">
                                <span style="font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)" x-text="card.label"></span>
                                <span style="font:var(--weight-bold) var(--text-xl)/1.1 var(--font-display);color:var(--gold-300)" x-text="card.leader"></span>
                                <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)" x-text="card.detail"></span>
                              </div>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
            }

            div {
                xShow("compareReady")
                ui.panel(title = "Skill by skill", action = eyebrowText("compareEyebrow"), padded = false) {
                    tableScroll(
                        Column("Skill", "minmax(0,1.1fr)"), Column("Lvl", "70px", "right"), Column("Xp", "130px", "right"),
                        Column("Ahead by", "190px", "center"), Column("Xp", "130px"), Column("Lvl", "70px"),
                    ) {
                        unsafe {
                            raw(
                                """
                                <template x-for="(row,i) in compareRows" :key="row.skill">
                                  <div :style="{ background: row.bg }" style="display:grid;grid-template-columns:minmax(0,1.1fr) 70px 130px 190px 130px 70px;align-items:center;padding:var(--space-3) var(--space-6);border-bottom:1px solid var(--umber-900)">
                                    <span style="display:flex;align-items:center;gap:var(--space-4);min-width:0">
                                      <span style="width:16px;height:16px;flex:none;display:flex;align-items:center;justify-content:center">
                                        <img :src="row.icon" alt="" style="max-width:100%;max-height:100%;width:auto;height:auto;display:block">
                                      </span>
                                      <span style="font:var(--weight-semibold) var(--text-xs)/1.2 var(--font-ui);letter-spacing:var(--tracking-wide);text-transform:uppercase;color:var(--parch-200)" x-text="row.skill"></span>
                                    </span>
                                    <span :style="{ color: row.aColor }" style="text-align:right;font:var(--type-code)" x-text="row.aLevel"></span>
                                    <span :style="{ color: row.aColor }" style="text-align:right;font:var(--type-code);font-size:var(--text-2xs)" x-text="row.aXp"></span>
                                    <span style="display:flex;align-items:center;justify-content:center;padding:0 10px">
                                      <span :style="{ background: row.deltaBg, borderColor: row.deltaBd, color: row.deltaFg }" style="display:inline-flex;align-items:center;gap:var(--space-3);height:22px;padding:0 10px;border-radius:var(--radius-pill);border:1px solid;font:var(--type-code);font-size:var(--text-3xs);white-space:nowrap" x-text="row.deltaText"></span>
                                    </span>
                                    <span :style="{ color: row.bColor }" style="font:var(--type-code);font-size:var(--text-2xs)" x-text="row.bXp"></span>
                                    <span :style="{ color: row.bColor }" style="font:var(--type-code)" x-text="row.bLevel"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                }
            }

            div {
                xShow("compareReady")
                ui.panel(title = "Boss kills", action = eyebrowText("compareEyebrow"), padded = false) {
                    tableScroll(listOf("minmax(0,1fr)", "90px", "190px", "90px")) {
                        unsafe {
                            raw(
                                """
                                <template x-for="(row,i) in compareBossRows" :key="row.key">
                                  <div :style="{ background: row.bg }" style="display:grid;grid-template-columns:minmax(0,1fr) 90px 190px 90px;align-items:center;padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--umber-900)">
                                    <span style="display:flex;align-items:center;gap:var(--space-4);min-width:0">
                                      <span style="flex:0 0 auto;width:28px;height:28px;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint);position:relative;overflow:hidden">
                                        <span x-text="row.abbr"></span>
                                        <img :src="row.icon" alt="" loading="lazy" @error="${'$'}el.remove()" style="position:absolute;inset:0;width:100%;height:100%;object-fit:contain;background:var(--surface-inset)">
                                      </span>
                                      <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-body)" x-text="row.boss"></span>
                                    </span>
                                    <span :style="{ color: row.aColor }" style="text-align:right;font:var(--type-code)" x-text="row.aKc"></span>
                                    <span style="display:flex;align-items:center;justify-content:center;padding:0 10px">
                                      <span :style="{ background: row.deltaBg, borderColor: row.deltaBd, color: row.deltaFg }" style="display:inline-flex;align-items:center;height:22px;padding:0 10px;border-radius:var(--radius-pill);border:1px solid;font:var(--type-code);font-size:var(--text-3xs);white-space:nowrap" x-text="row.deltaText"></span>
                                    </span>
                                    <span :style="{ color: row.bColor }" style="font:var(--type-code)" x-text="row.bKc"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.bossesView(gameData: GameData) {
        div {
            xShow("view === 'bosses'")
            attributes["class"] = "void-flex"
            style = "flex-direction:column;gap:var(--space-8)"

            ui.panel(title = "Bosses", action = eyebrowText("'Pick an encounter'"), padded = false) {
                bossTileGrid(gameData)
            }

            div {
                attributes["class"] = "void-grid hiscores-boss-panels-grid"
                style = "grid-template-columns:repeat(auto-fit,minmax(340px,1fr));gap:var(--space-8);align-items:start"

                ui.panel(title = "Top kill counts", action = eyebrowText("bossName"), padded = false) {
                    tableScroll(Column("Rank", "56px"), Column("Player", "minmax(0,1fr)"), Column("Kc", "110px", "right")) {
                        unsafe {
                            raw(
                                """
                                <template x-for="(row,i) in bossKcRows" :key="row.name">
                                  <div @click="open(row.name)" :style="{ background: row.bg }" style="display:grid;grid-template-columns:56px minmax(0,1fr) 110px;align-items:center;padding:var(--space-4) var(--space-6);cursor:pointer;border-bottom:1px solid var(--umber-900)">
                                    <span :style="{ color: row.rankColor }" style="font:var(--weight-bold) var(--text-base)/1 var(--font-display)" x-text="row.rank"></span>
                                    <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="row.name"></span>
                                    <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="row.kc"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                    paginationFooter("bossKcPager", "kcPage")
                }

                ui.panel(title = "Fastest kills", action = eyebrowText("bossName"), padded = false) {
                    teamChipsRow()
                    tableScroll(
                        Column("Rank", "56px"), Column("Player", "minmax(0,1fr)"),
                        Column("Team", "90px", "right"), Column("Time", "110px", "right"),
                    ) {
                        unsafe {
                            raw(
                                """
                                <template x-for="(row,i) in bossTimeRows" :key="row.name">
                                  <div @click="open(row.name)" :style="{ background: row.bg }" style="display:grid;grid-template-columns:56px minmax(0,1fr) 90px 110px;align-items:center;padding:var(--space-4) var(--space-6);cursor:pointer;border-bottom:1px solid var(--umber-900)">
                                    <span :style="{ color: row.rankColor }" style="font:var(--weight-bold) var(--text-base)/1 var(--font-display)" x-text="row.rank"></span>
                                    <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="row.name"></span>
                                    <span style="text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="row.team"></span>
                                    <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="row.time"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                    paginationFooter("bossTimePager", "timePage")
                }
            }
        }
    }

    private fun FlowContent.searchSortRow() {
        div {
            style = "display:flex;align-items:center;gap:var(--space-5);flex-wrap:wrap"
            span {
                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                +"Sort by"
            }
            div {
                style = "display:flex;gap:var(--space-2);flex-wrap:wrap"
                for ((id, label) in listOf("level" to "Total level", "rank" to "Rank", "name" to "Name")) {
                    ui.filterChip(active = "searchSort === '$id'", onClickExpr = "setSearchSort('$id')", label = label)
                }
            }
        }
    }

    private fun FlowContent.searchView() {
        div {
            xShow("view === 'search'")
            attributes["class"] = "void-flex"
            style = "flex-direction:column;gap:var(--space-6)"

            searchSortRow()

            ui.panel(title = "Search results", action = eyebrowText("searchEyebrow"), padded = false) {
                div {
                    xShow("searchSorted.length")
                    tableScroll(
                        Column("Rank", "76px"), Column("Player", "minmax(0,1fr)"),
                        Column("Total lvl", "130px", "right"), Column("Total xp", "170px", "right"),
                    ) {
                        unsafe {
                            raw(
                                """
                                <template x-for="(row,i) in searchRows" :key="row.name">
                                  <div @click="open(row.name)" :style="{ background: row.bg }" style="display:grid;grid-template-columns:76px minmax(0,1fr) 130px 170px;align-items:center;padding:var(--space-4) var(--space-6);cursor:pointer;border-bottom:1px solid var(--umber-900);transition:background var(--dur-fast) var(--ease-standard)">
                                    <span :style="{ color: row.rankColor }" style="font:var(--weight-bold) var(--text-lg)/1 var(--font-display)" x-text="row.rank"></span>
                                    <span style="display:flex;align-items:center;gap:var(--space-4);min-width:0">
                                      <span style="font:var(--weight-semibold) var(--text-base)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="row.name"></span>
                                      <span x-show="row.showBadge" class="void-flex" :style="{ background: row.badgeBg, color: row.badgeFg, borderColor: row.badgeBd }" style="align-items:center;gap:var(--space-3);padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="row.mode"></span>
                                    </span>
                                    <span style="text-align:right;font:var(--type-code);color:var(--text-body)" x-text="row.totalLevel"></span>
                                    <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="row.totalXp"></span>
                                  </div>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                }
                div {
                    xShow("!searchSorted.length")
                    style = "padding:var(--space-8) var(--space-6);text-align:center"
                    span {
                        style = "font:var(--type-body-sm);color:var(--text-muted)"
                        attributes["x-text"] = "'No players found matching “' + searchQuery + '”'"
                    }
                }
                div {
                    xShow("searchSorted.length")
                    paginationFooter("searchPager", "searchPage")
                }
            }
        }
    }

    private fun FlowContent.playerView() {
        div {
            xShow("view === 'player'")
            attributes["class"] = "void-flex"
            style = "flex-direction:column;gap:var(--space-8)"

            div {
                attributes["class"] = "void-flex"
                style = "align-items:flex-end;justify-content:space-between;gap:var(--space-6);flex-wrap:wrap"
                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-3)"
                    span {
                        style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                        attributes["x-text"] = "'Rank ' + profilePlayer.rank + ' overall · ' + profilePlayer.mode"
                    }
                    h2 {
                        style = "margin:0;font:var(--type-section);color:var(--text-strong)"
                        attributes["x-text"] = "profilePlayer.name"
                    }
                    span {
                        style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)"
                        attributes["x-text"] = "profilePlayer.joined"
                    }
                }
                div {
                    style = "display:flex;gap:var(--space-4)"
                    a {
                        attributes["class"] = "void-btn void-btn-secondary"
                        attributes["x-bind:href"] = "'log.html?player=' + encodeURIComponent(profilePlayer.name)"
                        style = "display:inline-flex;align-items:center;justify-content:center;gap:var(--space-4);height:28px;" +
                            "padding:0 var(--space-5);border-radius:var(--radius-md);font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);" +
                            "letter-spacing:0.06em;cursor:pointer;text-decoration:none;" +
                            "transition:background var(--dur-fast) var(--ease-standard)"
                        +"Adventurers log"
                    }
                    ui.button("Compare", size = ButtonSize.Small, onClick = "compareThis()")
                    ui.button("← Back", variant = ButtonVariant.Secondary, size = ButtonSize.Small, onClick = "back()")
                }
            }

            ui.panel(
                title = "Skills",
                action = eyebrowText("'Total level ' + profilePlayer.totalLevel + ' · ' + fmtXp(profilePlayer.totalXp) + ' xp'"),
            ) {
                div {
                    attributes["class"] = "void-grid"
                    style = "grid-template-columns:repeat(auto-fill,minmax(265px,1fr));gap:var(--space-4)"
                    unsafe {
                        raw(
                            """
                            <template x-for="s in profileSkills" :key="s.name">
                              <div style="display:flex;align-items:center;gap:var(--space-5);padding:var(--space-4) var(--space-5);background:var(--surface-panel-raised);border:1px solid var(--border-panel);border-radius:var(--radius-sm);box-shadow:var(--bevel-up)">
                                <div style="flex:1;min-width:0;display:flex;flex-direction:column;gap:var(--space-2)">
                                  <div style="display:flex;justify-content:space-between;gap:10px;align-items:baseline">
                                    <span style="display:flex;align-items:center;gap:var(--space-3);min-width:0;flex:1">
                                      <span style="width:16px;height:16px;flex:none;display:flex;align-items:center;justify-content:center">
                                        <img :src="s.icon" alt="" style="max-width:100%;max-height:100%;width:auto;height:auto;display:block">
                                      </span>
                                      <span style="font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-200);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="s.name"></span>
                                    </span>
                                    <span style="flex:none;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);white-space:nowrap"><span x-text="s.level"></span> / <span x-text="s.max"></span></span>
                                  </div>
                                  <div style="height:6px;background:var(--surface-inset);border:1px solid var(--border-subtle);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden">
                                    <div :style="{ width: s.percent + '%' }" style="height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500))"></div>
                                  </div>
                                </div>
                                <span style="font:var(--weight-bold) var(--text-xl)/1 var(--font-display);color:var(--gold-300);min-width:34px;text-align:right" x-text="s.level"></span>
                              </div>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
            }

            ui.panel(title = "Boss log", padded = false) {
                tableScroll(
                    Column("Boss", "minmax(0,1fr)"), Column("Rank", "120px", "right"),
                    Column("Kc", "110px", "right"), Column("Best time", "110px", "right"),
                ) {
                    unsafe {
                        raw(
                            """
                            <template x-for="(row,i) in profileBosses" :key="row.key">
                              <div :style="{ background: row.bg }" style="display:grid;grid-template-columns:minmax(0,1fr) 120px 110px 110px;align-items:center;padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--umber-900)">
                                <span style="display:flex;align-items:center;gap:var(--space-4);min-width:0">
                                  <span style="flex:0 0 auto;width:28px;height:28px;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint);position:relative;overflow:hidden">
                                    <span x-text="row.abbr"></span>
                                    <img :src="row.icon" alt="" loading="lazy" @error="${'$'}el.remove()" style="position:absolute;inset:0;width:100%;height:100%;object-fit:contain;background:var(--surface-inset)">
                                  </span>
                                  <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-body)" x-text="row.boss"></span>
                                </span>
                                <span style="text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="row.rank"></span>
                                <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="row.kc"></span>
                                <span style="text-align:right;font:var(--type-code);color:var(--text-body)" x-text="row.best"></span>
                              </div>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
            }
        }
    }
}
