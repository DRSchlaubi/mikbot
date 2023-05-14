FROM eclipse-temurin:20-jre-alpine

WORKDIR /usr/app
COPY build/install/mikmusic ./

LABEL org.opencontainers.image.source = "https://github.com/DRSchlaubi/mikbot"

ENTRYPOINT ["bin/mikmusic"]
