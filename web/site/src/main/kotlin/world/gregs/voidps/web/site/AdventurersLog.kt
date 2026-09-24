package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The public adventurer's log. Two views share one page, toggled by `logApp()`'s `view` state
 * (`overview`, `profile`) and seeded from the `?player=` query string: an overview of top players
 * with a "Find a log" search, and a player's hero band, recent activity feed, and
 * skills/quests/bosses panels. `js/log.js` fetches the real `/api/v1/players` and
 * `/api/v1/hiscores` endpoints (see `HiscoresRoutes.kt`) for every view - the only synthesized
 * data left is the xp-history chart's day-by-day distribution, since daily xp snapshots aren't
 * tracked yet; its totals still add up to the account's real per-skill xp. Skill and boss rows on
 * the player view link out to the matching `hiscores.html` leaderboard.
 */
object AdventurersLog {

    /** The xp-history chart's day-by-day distribution is synthesized (see class doc) - flip this off to hide it. */
    private const val SHOW_XP_CHART = false

    fun page(gameData: GameData): String = voidPage(
        title = "Void — adventurer's log",
        description = "Skills, quests, boss kills and recent activity for any Void account.",
        head = {
            script { unsafe { raw(gameData.script()) } }
            script(src = "js/log.js") {}
        },
    ) {
        ui.siteHeader(Website.pages, active = "log", communityPages = Website.communityPages)

        div {
            xData("logApp()")

            div {
                xShow("view === 'overview'")
                ui.pageHeader(
                    eyebrow = "Adventurer's log",
                    title = "Player Adventurer Logs",
                    description = "View recent notable achievements and on going progress.",
                    backgroundImage = "images/bg/logs.jpg",
                )
            }

            main {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-12);" +
                    "display:flex;flex-direction:column;gap:var(--space-8)"

                div {
                    style = "display:flex;flex-wrap:wrap;align-items:flex-start;gap:var(--space-8)"

                    div {
                        style = "flex:1 1 560px;min-width:0;display:flex;flex-direction:column;gap:var(--space-8)"

                        overviewSection()

                        div {
                            xShow("view === 'profile'")
                            attributes["class"] = "void-flex"
                            style = "flex-direction:column;gap:var(--space-8)"

                            div {
                                style = "display:flex;justify-content:flex-start"
                                ui.button(
                                    "← Overview", variant = ButtonVariant.Secondary, size = ButtonSize.Small,
                                    onClick = "backToOverview()",
                                )
                            }

                            heroBand()
                            recentActivity()
                            skillsPanel()
                            if (SHOW_XP_CHART) xpChartPanel()
                            questsPanel()
                            bossesPanel()
                        }
                    }

                    aside {
                        style = "flex:1 1 300px;min-width:280px;max-width:340px;display:flex;" +
                            "flex-direction:column;gap:var(--space-8);position:sticky;top:80px"
                        findLogPanel()
                        div {
                            xShow("view === 'profile'")
                            milestonesPanel()
                        }
                    }
                }
            }
        }

        ui.siteFooter()
    }

    private fun FlowContent.heroBand() {
        section {
            style = "position:relative;border:1px solid var(--border-panel);border-radius:var(--radius-md);" +
                "box-shadow:var(--bevel-up),var(--shadow-sm);overflow:hidden;background:var(--umber-900)"

            img(src = "images/repository-bg.png", alt = "") {
                style = "position:absolute;top:50%;left:0;width:100%;height:auto;transform:translateY(-50%);display:block"
            }
            div {
                style = "position:absolute;inset:0;background:linear-gradient(90deg,rgba(23,18,13,.94) 0%," +
                    "rgba(23,18,13,.86) 45%,rgba(23,18,13,.42) 100%)"
            }

            div {
                style = "position:relative;padding:var(--space-8);display:flex;flex-wrap:wrap;" +
                    "align-items:flex-end;gap:var(--space-8)"

                div {
                    style = "display:flex;align-items:flex-end;gap:var(--space-6);min-width:0"
                    div {
                        style = "width:96px;height:96px;flex:0 0 auto;display:flex;align-items:center;justify-content:center;" +
                            "border:1px solid var(--gold-600);border-radius:var(--radius-sm);box-shadow:var(--bevel-down);" +
                            "background:var(--umber-950);color:var(--gold-300)"
                        icon(Icons.ACCOUNT, size = 44)
                    }
                    div {
                        style = "min-width:0;display:flex;flex-direction:column;gap:var(--space-4)"
                        span {
                            style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
                            +"Adventurer's log"
                        }
                        h1 {
                            style = "margin:0;font:var(--type-title);color:var(--parch-50);text-wrap:pretty"
                            xText("profile.name")
                            +"Thornwake"
                        }
                        div {
                            style = "display:flex;flex-wrap:wrap;align-items:center;gap:var(--space-3)"
                            unsafe {
                                raw(
                                    """
                                    <span style="display:inline-flex;align-items:center;padding:0 10px;height:20px;background:var(--umber-700);color:var(--parch-200);border:1px solid var(--border-strong);border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="profile.mode"></span>
                                    <template x-if="profile.rights !== 'none'">
                                      <span :style="{ background: profile.rights === 'admin' ? 'var(--feedback-danger-bg)' : 'rgba(224,174,60,.14)', color: profile.rights === 'admin' ? 'var(--feedback-danger)' : 'var(--gold-300)', borderColor: profile.rights === 'admin' ? 'var(--ember-600)' : 'var(--gold-600)' }" style="display:inline-flex;align-items:center;gap:var(--space-3);padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase">
                                        <span style="width:5px;height:5px;border-radius:50%;background:currentColor"></span>
                                        <span x-text="profile.rights === 'admin' ? 'Admin' : 'Moderator'"></span>
                                      </span>
                                    </template>
                                    """.trimIndent(),
                                )
                            }
                            span {
                                style = "font:var(--type-body-sm);color:var(--parch-300)"
                                +"Joined "
                                span { xText("profile.joined") }
                            }
                        }
                    }
                }

                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(104px,1fr));" +
                        "gap:var(--space-6);flex:1 1 320px;min-width:0"
                    heroStat("Total level", "profile.totalLevel", gold = true)
                    heroStat("Combat", "profile.combat")
                    heroStat("Total XP", "profile.totalXpLabel")
                    heroStat("Quest points", "profile.questPoints + ' / ' + profile.questPointsMax")
                }
            }
        }
    }

    private fun FlowContent.heroStat(label: String, expression: String, gold: Boolean = false) {
        div {
            style = "display:flex;flex-direction:column;gap:var(--space-2)"
            span {
                style = "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                    "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-400)"
                +label
            }
            span {
                style = "font:var(--weight-bold) var(--text-2xl)/1 var(--font-display);" +
                    "color:${if (gold) "var(--gold-300)" else "var(--parch-50)"}"
                xText(expression)
            }
        }
    }

    private fun FlowContent.recentActivity() {
        ui.panel(title = "Recent activity", action = eyebrowText("'last 30 days'")) {
            div {
                style = "display:flex;flex-wrap:wrap;gap:var(--space-5);margin:calc(var(--space-2) * -1) 0 var(--space-6)"
                unsafe {
                    raw(
                        """
                        <template x-for="t in filterTabs" :key="t.label">
                          <button type="button" @click="t.onClick()" :style="{ borderBottomColor: t.active ? 'var(--gold-400)' : 'transparent', color: t.active ? 'var(--gold-300)' : 'var(--parch-300)' }" style="background:transparent;border:none;border-bottom:2px solid transparent;padding:0 0 var(--space-3);cursor:pointer;font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="t.label"></button>
                        </template>
                        """.trimIndent(),
                    )
                }
            }
            ol {
                style = "margin:0;padding:0;list-style:none;display:flex;flex-direction:column;gap:1px;" +
                    "background:var(--border-panel)"
                unsafe {
                    raw(
                        """
                        <template x-for="e in visibleEvents" :key="e.key">
                          <li :style="{ background: e.band }" style="display:flex;flex-wrap:wrap;align-items:baseline;gap:var(--space-4);padding:var(--space-5)">
                            <span x-show="e.date" :title="e.exact" style="flex:0 0 auto;width:70px;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);cursor:default" x-text="e.date"></span>
                            <span style="flex:1 1 240px;min-width:0;display:flex;flex-direction:column;gap:var(--space-2)">
                              <span style="font:var(--type-body);color:var(--text-strong);text-wrap:pretty" x-text="e.text"></span>
                              <span x-show="e.description" style="font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint);text-wrap:pretty" x-text="e.description"></span>
                            </span>
                            <span x-show="e.kind" class="void-inline-flex" :style="{ background: e.tone === 'gold' ? 'rgba(224,174,60,.14)' : e.tone === 'success' ? 'var(--feedback-success-bg)' : e.tone === 'danger' ? 'var(--feedback-danger-bg)' : 'var(--feedback-info-bg)', color: e.tone === 'gold' ? 'var(--gold-300)' : e.tone === 'success' ? 'var(--feedback-success)' : e.tone === 'danger' ? 'var(--feedback-danger)' : 'var(--feedback-info)', borderColor: e.tone === 'gold' ? 'var(--gold-600)' : e.tone === 'success' ? 'var(--moss-600)' : e.tone === 'danger' ? 'var(--ember-600)' : 'var(--steel-600)' }" style="align-items:center;padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-xs);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="e.kind"></span>
                          </li>
                        </template>
                        """.trimIndent(),
                    )
                }
            }
        }
    }

    private fun FlowContent.skillsPanel() {
        ui.panel(title = "Skills", action = eyebrowText("profile.skills.length + ' skills · ' + profile.maxedCount + ' at 99'")) {
            div {
                style = "display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;" +
                    "gap:var(--space-5);margin-bottom:var(--space-6)"
                span {
                    style = "font:var(--type-body-sm);color:var(--text-faint)"
                    +"Total level "
                    span { xText("profile.totalLevel") }
                }
                div {
                    style = "display:flex;gap:var(--space-3)"
                    unsafe {
                        raw(
                            """
                            <template x-for="s in sortTabs" :key="s.key">
                              <button type="button" @click="s.onClick()" :style="{ background: s.active ? 'var(--surface-active)' : 'var(--surface-panel-raised)', color: s.active ? 'var(--gold-300)' : 'var(--parch-200)' }" style="border:1px solid var(--border-strong);border-radius:var(--radius-sm);box-shadow:var(--bevel-up);height:26px;padding:0 var(--space-5);cursor:pointer;font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="s.label"></button>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
            }
            div {
                style = "display:grid;grid-template-columns:repeat(auto-fill,minmax(238px,1fr));gap:var(--space-5)"
                unsafe {
                    raw(
                        """
                        <template x-for="sk in sortedSkills" :key="sk.name">
                          <div style="display:flex;flex-direction:column;gap:var(--space-2);min-width:0">
                            <a :href="'hiscores.html?view=skills&skill=' + encodeURIComponent(sk.name)" style="display:flex;align-items:center;gap:var(--space-5);padding:var(--space-4) var(--space-5);background:var(--surface-panel-raised);border:1px solid var(--border-panel);border-radius:var(--radius-sm);box-shadow:var(--bevel-up);text-decoration:none;cursor:pointer">
                              <div style="flex:1;min-width:0;display:flex;flex-direction:column;gap:var(--space-2)">
                                <div style="display:flex;justify-content:space-between;gap:10px;align-items:baseline">
                                  <span style="display:flex;align-items:center;gap:var(--space-3);min-width:0;flex:1">
                                    <span style="width:16px;height:16px;flex:none;display:flex;align-items:center;justify-content:center">
                                      <img :src="sk.icon" alt="" style="max-width:100%;max-height:100%;width:auto;height:auto;display:block">
                                    </span>
                                    <span style="font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-200);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="sk.name"></span>
                                  </span>
                                  <span style="flex:none;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);white-space:nowrap"><span x-text="sk.level"></span> / <span x-text="sk.max"></span></span>
                                </div>
                                <div style="height:6px;background:var(--surface-inset);border:1px solid var(--border-subtle);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden">
                                  <div :style="{ width: Math.round(sk.level * 100 / sk.max) + '%' }" style="height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500))"></div>
                                </div>
                              </div>
                              <span style="font:var(--weight-bold) var(--text-xl)/1 var(--font-display);color:var(--gold-300);min-width:34px;text-align:right" x-text="sk.level"></span>
                            </a>
                            <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);padding:0 var(--space-2);display:flex;justify-content:space-between;gap:var(--space-4)">
                              <span x-text="sk.xpLabel + ' xp'"></span>
                              <span x-text="sk.rankLabel"></span>
                            </span>
                          </div>
                        </template>
                        """.trimIndent(),
                    )
                }
            }
        }
    }

    private fun FlowContent.xpChartPanel() {
        ui.panel(title = "Experience gained", action = eyebrowText("xpChartData.eyebrow")) {
            div {
                style = "display:flex;flex-wrap:wrap;gap:var(--space-7);align-items:center;" +
                    "justify-content:space-between;margin-bottom:var(--space-6)"
                div {
                    style = "display:flex;flex-wrap:wrap;gap:var(--space-5)"
                    unsafe {
                        raw(
                            """
                            <template x-for="l in xpChartData.legend" :key="l.name">
                              <span style="display:flex;align-items:center;gap:var(--space-3);font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)">
                                <span :style="{ background: l.color }" style="width:8px;height:8px;border-radius:2px;display:inline-block"></span>
                                <span x-text="l.name"></span>
                              </span>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
                div {
                    style = "display:flex;gap:var(--space-3)"
                    unsafe {
                        raw(
                            """
                            <template x-for="t in xpRangeTabs" :key="t.key">
                              <button type="button" @click="t.onClick()" :style="{ background: t.active ? 'var(--surface-active)' : 'var(--surface-panel-raised)', color: t.active ? 'var(--gold-300)' : 'var(--parch-200)' }" style="border:1px solid var(--border-strong);border-radius:var(--radius-sm);box-shadow:var(--bevel-up);height:26px;padding:0 var(--space-5);cursor:pointer;font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="t.label"></button>
                            </template>
                            <button type="button" x-show="xpZoom" @click="resetXpZoom()" style="background:rgba(224,174,60,.14);color:var(--gold-300);border:1px solid var(--gold-600);border-radius:var(--radius-sm);height:26px;padding:0 var(--space-5);cursor:pointer;font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase">Reset zoom</button>
                            """.trimIndent(),
                        )
                    }
                }
            }
            unsafe {
                raw(
                    """
                    <p x-show="!xpZoom" style="margin:0 0 var(--space-4);font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)">Click and drag across the chart to zoom into a date range.</p>
                    <div style="position:relative;user-select:none;cursor:crosshair" @mousedown="onXpChartDown(${'$'}event)" @mousemove="onXpChartMove(${'$'}event)" @mouseup.window="onXpChartUp()" @mouseleave="onXpChartLeave()">
                      <svg viewBox="0 0 920 300" width="100%" preserveAspectRatio="xMidYMid meet" style="display:block;overflow:visible">
                        <g x-html="xpChartData.gridSvg"></g>
                        <g x-html="xpChartData.layersSvg"></g>
                        <g x-show="xpChartData.hovering">
                          <line :x1="xpChartData.hx" :x2="xpChartData.hx" y1="10" y2="266" style="stroke:var(--gold-300);stroke-width:1;stroke-dasharray:3 4"></line>
                          <g x-html="xpChartData.hoverDotsSvg"></g>
                        </g>
                        <g x-show="xpChartData.dragging" x-html="xpChartData.selectionSvg"></g>
                      </svg>
                      <template x-for="(g, gi) in xpChartData.grid" :key="'xg'+gi">
                        <div style="position:absolute;left:0;width:6.3%;text-align:right;transform:translateY(-50%);font:var(--type-code);font-size:11px;color:var(--text-faint);pointer-events:none" :style="{ top: g.top }" x-text="g.label"></div>
                      </template>
                      <template x-for="(l, li) in xpChartData.xlabels" :key="'xl'+li">
                        <div style="position:absolute;transform:translateX(-50%);font:var(--type-code);font-size:11px;color:var(--text-faint);pointer-events:none;white-space:nowrap;bottom:1%" :style="{ left: l.left }" x-text="l.label"></div>
                      </template>
                      <div x-show="xpChartData.hovering" style="position:absolute;top:10px;transform:translateX(-50%);z-index:5;padding:var(--space-3) var(--space-4);background:var(--umber-950);border:1px solid var(--border-gold);border-radius:var(--radius-xs);box-shadow:var(--shadow-md);font:var(--type-code);font-size:11px;white-space:nowrap;pointer-events:none;min-width:140px" :style="{ left: xpChartData.hoverLeft }">
                        <div style="color:var(--text-faint);margin-bottom:var(--space-2);display:flex;justify-content:space-between;gap:var(--space-4)">
                          <span x-text="xpChartData.stamp"></span>
                          <span style="color:var(--gold-300)" x-text="xpChartData.dayTotal"></span>
                        </div>
                        <template x-for="b in xpChartData.breakdown" :key="b.name">
                          <div style="display:flex;align-items:center;gap:var(--space-3)">
                            <span :style="{ background: b.color }" style="width:8px;height:2px;display:inline-block;flex:none"></span>
                            <span x-text="b.name"></span>
                            <span style="margin-left:auto;color:var(--text-strong)" x-text="'+' + b.xpLabel"></span>
                          </div>
                        </template>
                        <div x-show="xpChartData.breakdown.length === 0" style="color:var(--text-faint)">No experience gained</div>
                      </div>
                    </div>
                    """.trimIndent(),
                )
            }
        }
    }

    private fun FlowContent.questsPanel() {
        ui.panel(title = "Quests", action = eyebrowText("profile.quests.length + ' of ' + profile.questTotal + ' complete'")) {
            div {
                style = "margin-bottom:var(--space-6);display:flex;align-items:center;gap:var(--space-5);" +
                    "padding:var(--space-4) var(--space-5);background:var(--surface-panel-raised);" +
                    "border:1px solid var(--border-panel);border-radius:var(--radius-sm);box-shadow:var(--bevel-up)"
                div {
                    style = "flex:1;min-width:0;display:flex;flex-direction:column;gap:var(--space-2)"
                    div {
                        style = "display:flex;justify-content:space-between;gap:10px;align-items:baseline"
                        span {
                            style = "font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);" +
                                "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-200)"
                            +"Quest points"
                        }
                        span {
                            style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                            xText("profile.questPoints + ' / ' + profile.questPointsMax")
                        }
                    }
                    div {
                        style = "height:6px;background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                            "border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden"
                        unsafe { raw("""<div :style="{ width: Math.round(profile.questPoints * 100 / profile.questPointsMax) + '%' }" style="height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500))"></div>""") }
                    }
                }
                span {
                    style = "font:var(--weight-bold) var(--text-xl)/1 var(--font-display);color:var(--gold-300);" +
                        "min-width:34px;text-align:right"
                    xText("profile.questPoints")
                }
            }
            div {
                style = "overflow-x:auto"
                table {
                    style = "width:100%;min-width:460px;border-collapse:collapse"
                    thead {
                        tr {
                            questHeader("Quest", "left")
                            questHeader("Difficulty", "left")
                            questHeader("Time taken", "right")
                            questHeader("Completed", "right")
                        }
                    }
                    tbody {
                        unsafe {
                            raw(
                                """
                                <template x-for="q in profile.quests" :key="q.name">
                                  <tr :style="{ background: q.band }">
                                    <td style="padding:var(--space-4);font:var(--type-body);color:var(--text-strong)" x-text="q.name"></td>
                                    <td style="padding:var(--space-4)">
                                      <span :style="{ background: q.tone === 'danger' ? 'var(--feedback-danger-bg)' : q.tone === 'warning' ? 'var(--feedback-warning-bg)' : q.tone === 'success' ? 'var(--feedback-success-bg)' : 'var(--feedback-info-bg)', color: q.tone === 'danger' ? 'var(--feedback-danger)' : q.tone === 'warning' ? 'var(--feedback-warning)' : q.tone === 'success' ? 'var(--feedback-success)' : 'var(--feedback-info)', borderColor: q.tone === 'danger' ? 'var(--ember-600)' : q.tone === 'warning' ? 'var(--gold-700)' : q.tone === 'success' ? 'var(--moss-600)' : 'var(--steel-600)' }" style="display:inline-flex;align-items:center;padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="q.difficulty"></span>
                                    </td>
                                    <td style="padding:var(--space-4);text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--parch-200);white-space:nowrap" x-text="q.duration"></td>
                                    <td style="padding:var(--space-4);text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);white-space:nowrap" x-text="q.date"></td>
                                  </tr>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun TR.questHeader(label: String, align: String) {
        th {
            style = "text-align:$align;padding:0 var(--space-4) var(--space-3);" +
                "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-400)"
            +label
        }
    }

    private fun FlowContent.bossesPanel() {
        ui.panel(title = "Bosses", action = eyebrowText("profile.bosses.length + ' tracked · ' + profile.bossKills.toLocaleString() + ' kills'")) {
            div {
                style = "overflow-x:auto"
                table {
                    style = "width:100%;min-width:460px;border-collapse:collapse"
                    thead {
                        tr {
                            questHeader("Boss", "left")
                            questHeader("Kills", "right")
                            questHeader("Fastest kill", "right")
                            questHeader("Last kill", "right")
                        }
                    }
                    tbody {
                        unsafe {
                            raw(
                                """
                                <template x-for="b in profile.bosses" :key="b.key">
                                  <tr :style="{ background: b.band }">
                                    <td style="padding:var(--space-4)">
                                      <span style="display:flex;align-items:center;gap:var(--space-4);min-width:0">
                                        <span style="flex:0 0 auto;width:28px;height:28px;display:flex;align-items:center;justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint);position:relative;overflow:hidden">
                                          <span x-text="b.abbr"></span>
                                          <img :src="b.icon" alt="" loading="lazy" @error="${'$'}el.remove()" style="position:absolute;inset:0;width:100%;height:100%;object-fit:contain;background:var(--surface-inset)">
                                        </span>
                                        <a :href="'hiscores.html?view=bosses&boss=' + encodeURIComponent(b.name)" style="font:var(--type-body);color:var(--text-strong);text-decoration:none" x-text="b.name"></a>
                                      </span>
                                    </td>
                                    <td style="padding:var(--space-4);text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--parch-200)" x-text="b.kills"></td>
                                    <td style="padding:var(--space-4);text-align:right;font:var(--weight-semibold) var(--text-sm)/1 var(--font-mono);color:var(--gold-300);white-space:nowrap" x-text="b.fastest"></td>
                                    <td style="padding:var(--space-4);text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);white-space:nowrap" x-text="b.last"></td>
                                  </tr>
                                </template>
                                """.trimIndent(),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.findLogPanel() {
        ui.panel(title = "Find a log") {
            style = "display:flex;flex-direction:column;gap:var(--space-5)"
            ui.textInput(
                "log-search", "Account name", model = "query",
                placeholder = "Search adventurers", icon = Icons.SEARCH,
            )
            div {
                style = "display:flex;flex-direction:column;gap:1px;background:var(--border-panel);" +
                    "border:1px solid var(--border-panel);border-radius:var(--radius-sm)"
                unsafe {
                    raw(
                        """
                        <template x-for="p in results" :key="p.name">
                          <a href="#" @click.prevent="pick(p.name)" style="display:flex;align-items:center;justify-content:space-between;gap:var(--space-4);padding:var(--space-4) var(--space-5);background:var(--surface-panel);text-decoration:none">
                            <span style="min-width:0;display:flex;flex-direction:column;gap:var(--space-2)">
                              <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--gold-300);overflow:hidden;text-overflow:ellipsis;white-space:nowrap" x-text="p.name"></span>
                              <span style="font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)" x-text="p.meta"></span>
                            </span>
                            <span style="flex:0 0 auto;font:var(--weight-bold) var(--text-base)/1 var(--font-display);color:var(--parch-200)" x-text="p.total"></span>
                          </a>
                        </template>
                        """.trimIndent(),
                    )
                }
                p {
                    xShow("noResults")
                    style = "margin:0;padding:var(--space-6) var(--space-5);background:var(--surface-panel);" +
                        "font:var(--type-body-sm);color:var(--text-faint)"
                    +"No log matches that name. Names are 1–12 characters."
                }
            }
            span {
                style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)"
                +"Logs are public unless the account opts out in privacy settings."
            }
        }
    }

    private fun FlowContent.milestonesPanel() {
        ui.panel(title = "Milestones") {
            style = "display:flex;flex-direction:column;gap:var(--space-5)"
            unsafe {
                raw(
                    """
                    <template x-for="m in profile.milestones" :key="m.label">
                      <div style="display:flex;align-items:baseline;justify-content:space-between;gap:var(--space-4)">
                        <span style="font:var(--type-body-sm);color:var(--text-body)" x-text="m.label"></span>
                        <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--gold-300);white-space:nowrap" x-text="m.value"></span>
                      </div>
                    </template>
                    """.trimIndent(),
                )
            }
        }
    }

    private fun FlowContent.overviewSection() {
        div {
            xShow("view === 'overview'")
            attributes["class"] = "void-flex"
            style = "flex-direction:column;gap:var(--space-8)"

            ui.panel(title = "Players", action = eyebrowText("overviewPlayers.length + ' logged'"), padded = false) {
                tableScroll(
                    Column("Player", "minmax(0,1fr)"), Column("Mode", "120px", "right"),
                    Column("Total level", "120px", "right"),
                ) {
                    unsafe {
                        raw(
                            """
                            <template x-for="p in overviewPlayers" :key="p.name">
                              <a href="#" @click.prevent="pick(p.name)" style="display:grid;grid-template-columns:minmax(0,1fr) 120px 120px;align-items:center;padding:var(--space-4) var(--space-6);text-decoration:none;border-bottom:1px solid var(--umber-900)">
                                <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-strong);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="p.name"></span>
                                <span style="text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="p.mode"></span>
                                <span style="text-align:right;font:var(--type-code);color:var(--gold-300)" x-text="p.total"></span>
                              </a>
                            </template>
                            """.trimIndent(),
                        )
                    }
                }
            }
        }
    }
}
