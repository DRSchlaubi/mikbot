FROM gradle:jdk18 as builder
WORKDIR /usr/app
COPY . .
RUN gradle --no-daemon :votebot:plugin:installVoteBotArchive

FROM ibm-semeru-runtimes:open-18-jre-focal

LABEL org.opencontainers.image.source = "https://github.com/DRSchlaubi/mikbot"

WORKDIR /usr/app
COPY --from=builder /usr/app/votebot/plugin/build/installVoteBot .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
