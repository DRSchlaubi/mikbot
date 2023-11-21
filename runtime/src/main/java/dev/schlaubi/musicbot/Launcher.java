package dev.schlaubi.musicbot;

import kotlin.coroutines.jvm.internal.RunSuspendKt;


public class Launcher {
    //K2 fails here for some reason, so we do it ourselves
    public static void main(String[] args) {
        RunSuspendKt.runSuspend(LauncherKt::main);
    }
}
