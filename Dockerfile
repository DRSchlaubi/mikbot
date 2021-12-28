FROM ibm-semeru-runtimes:open-17-jre-focal

WORKDIR /usr/app
COPY build/install/mikmusic ./

LABEL org.opencontainers.image.source = "https://github.com/DRSchlaubi/mikmusic"

ENTRYPOINT ["bin/mikmusic"]
