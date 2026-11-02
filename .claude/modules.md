# Modules

Dependency direction: `game` → `engine`, `cache`, `network`, `config`, `types`, `web:server`.

|        Module         |                                                                 Responsibility                                                                  |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| **engine**            | Game loop, entity model (Player, NPC, GameObject, FloorItem), event system, inventory transactions, pathfinding, all `Script` mix-in interfaces |
| **game**              | All content (`content/`), `Main.kt`, `WorldTest.kt`                                                                                             |
| **cache**             | Decoders and encoders of binary RS cache files                                                                                                  |
| **config**            | Reader for simplified toml file reading (string IDs, shops, drop tables, etc.)                                                                  |
| **network**           | Ktor server for connections, login, packet encoding/decoding                                                                                    |
| **web:server**        | Ktor webserver: site, webclient host, WebSocket proxy to the game port, public `/api/v1` and the account REST api                               |
| **buffer**            | Binary read/write utilities                                                                                                                     |
| **types**             | Shared value types (`Tile`, `Item`, `Direction`, etc.)                                                                                          |
| **database**          | Optional PostgreSQL via Exposed ORM (`-PincludeDb` only)                                                                                        |
| **tools / tools:app** | Dev utilities and Compose cache viewer — not part of the server                                                                                 |

