FROM eclipse-temurin:20-jre-alpine

WORKDIR /usr/app
COPY runtime/build/install/runtime ./

LABEL org.opencontainers.image.source = "https://github.com/DRSchlaubi/mikbot"

ENTRYPOINT ["bin/runtime"]
