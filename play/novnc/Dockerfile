FROM ubuntu:24.04
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-21-jre \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    socat \
    netcat-openbsd \
    wget \
    ca-certificates \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Download latest void client (--retry in case of transient failure)
RUN wget -L -q --tries=5 -O /app/client.jar \
    "https://github.com/GregHib/void-client/releases/latest/download/client.jar" \
    && echo "Client downloaded: $(du -sh /app/client.jar)"

COPY start.sh .
RUN chmod +x start.sh

EXPOSE 6080

CMD ["./start.sh"]
