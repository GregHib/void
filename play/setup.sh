#!/bin/bash
set -e
cd "$(dirname "$0")"

# ──────────────────────────────────────────────────────────────────────────────
# Void RSPS — Quick Setup
# ──────────────────────────────────────────────────────────────────────────────

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

info()  { echo -e "${CYAN}[setup]${NC} $*"; }
ok()    { echo -e "${GREEN}[  ok ]${NC} $*"; }
warn()  { echo -e "${YELLOW}[ warn]${NC} $*"; }
error() { echo -e "${RED}[error]${NC} $*"; exit 1; }

echo -e "${BOLD}"
echo "  ╔══════════════════════════════════╗"
echo "  ║    Void RSPS — Play Server       ║"
echo "  ╚══════════════════════════════════╝"
echo -e "${NC}"

# Check Docker
command -v docker &>/dev/null || error "Docker not found. Install from https://docs.docker.com/get-docker/"
docker compose version &>/dev/null || error "Docker Compose v2 not found. Update Docker Desktop or install the plugin."
ok "Docker found"

# Check cache files exist
CACHE_DIR="../data/cache"
if [ ! -d "$CACHE_DIR" ] || [ -z "$(ls -A "$CACHE_DIR" 2>/dev/null)" ]; then
    echo ""
    warn "Cache files missing at data/cache/"
    echo ""
    echo "  The game needs cache files to run. Download them from:"
    echo "  https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A"
    echo ""
    echo "  Extract the contents into:  $(realpath "$CACHE_DIR" 2>/dev/null || echo "$(pwd)/../data/cache/")"
    echo ""
    read -rp "  Press Enter once done, or Ctrl+C to cancel..."
fi

info "Building and starting all services..."
docker compose up -d --build

echo ""

# Get public IP
PUBLIC_IP=$(curl -s --max-time 5 https://api.ipify.org 2>/dev/null || echo "YOUR_PUBLIC_IP")
LOCAL_IP=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "localhost")

echo -e "${GREEN}${BOLD}  ✓ Server is running!${NC}"
echo ""
echo -e "  ${BOLD}Share these links with friends:${NC}"
echo ""
echo -e "  ${CYAN}Browser play (zero install):${NC}"
echo -e "    Local:   http://${LOCAL_IP}:8080"
echo -e "    Public:  http://${PUBLIC_IP}:8080  (if port 8080 is forwarded)"
echo ""
echo -e "  ${CYAN}Downloadable client:${NC}"
echo -e "    Direct connect to:  ${PUBLIC_IP}:43594  (if port 43594 is forwarded)"
echo ""
echo -e "  ${YELLOW}Home network note:${NC} For friends outside your home to connect, you"
echo -e "  need to forward ports ${BOLD}8080${NC}${YELLOW} and ${BOLD}43594${NC}${YELLOW} on your router."
echo -e "  Or use a free tunnel — see below."
echo ""
echo -e "  ${CYAN}Free tunnel options (no port forwarding needed):${NC}"
echo -e "    • Web page:  ${BOLD}cloudflared tunnel --url http://localhost:8080${NC}"
echo -e "      Install:   https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/"
echo -e "    • Game port: ${BOLD}playit.gg${NC}  (free TCP tunnels designed for game servers)"
echo -e "      Install:   https://playit.gg"
echo ""
echo -e "  ${CYAN}Useful commands:${NC}"
echo -e "    Logs:      docker compose logs -f"
echo -e "    Stop:      docker compose down"
echo -e "    Restart:   docker compose restart"
echo ""
