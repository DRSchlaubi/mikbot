FROM --platform=$TARGETOS/$TARGETARCH eclipse-temurin:22-jre-alpine

WORKDIR /usr/app
COPY build/install/bot-kubernetes .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
