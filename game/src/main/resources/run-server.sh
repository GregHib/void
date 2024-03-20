#!/usr/bin/env bash
title="Void Game Server"
echo -e '\033]2;'$title'\007'
# Early exit on cancel
cleanup() {
	echo ""
    exit 1
}
trap cleanup INT
java -jar void-server-dev.jar
# Stop console closing
if [ $? -ne 0 ]; then
    echo "Error: The Java application exited with a non-zero status."
    read -p "Press enter to continue..."
fi 