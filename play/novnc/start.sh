#!/bin/bash
set -e

GAME_HOST="${GAME_HOST:-void}"
GAME_PORT="${GAME_PORT:-43594}"
DISPLAY_NUM="${DISPLAY_NUM:-99}"
SCREEN_RES="${SCREEN_RES:-1280x800x24}"
SESSION_NAME="${SESSION_NAME:-Player}"

echo "[noVNC] Session: $SESSION_NAME"
echo "[noVNC] Waiting for game server at $GAME_HOST:$GAME_PORT..."

# Wait up to 60s for game server to be ready
timeout=60
while ! nc -z "$GAME_HOST" "$GAME_PORT" 2>/dev/null; do
    timeout=$((timeout - 2))
    if [ $timeout -le 0 ]; then
        echo "[noVNC] ERROR: Game server not reachable after 60s"
        exit 1
    fi
    sleep 2
done
echo "[noVNC] Game server ready!"

# Proxy game server to localhost so the client can connect normally
socat TCP-LISTEN:43594,fork,reuseaddr "TCP:$GAME_HOST:$GAME_PORT" &

# Virtual display
Xvfb ":$DISPLAY_NUM" -screen 0 "$SCREEN_RES" &
sleep 1

# VNC server on localhost:5900 (no password — nginx handles access)
x11vnc -display ":$DISPLAY_NUM" -nopw -listen localhost -forever -shared -bg -quiet
sleep 1

# noVNC WebSocket server on 0.0.0.0:6080
websockify --web=/usr/share/novnc/ 6080 localhost:5900 &

echo "[noVNC] Ready at port 6080"

# Launch the game client
DISPLAY=":$DISPLAY_NUM" java \
    -Dhost="$GAME_HOST" \
    -Dport="$GAME_PORT" \
    -jar /app/client.jar \
    "$GAME_HOST" "$GAME_PORT" 2>&1 || \
DISPLAY=":$DISPLAY_NUM" java -jar /app/client.jar
