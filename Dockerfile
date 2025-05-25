FROM --platform=$TARGETOS/$TARGETARCH eclipse-temurin:23-jre-alpine

WORKDIR /usr/app
COPY runtime/build/install/mikmusic ./

LABEL org.opencontainers.image.source="https://github.com/DRSchlaubi/mikbot"

ENTRYPOINT ["bin/mikmusic"]
