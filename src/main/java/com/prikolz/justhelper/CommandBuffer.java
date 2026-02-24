package com.prikolz.justhelper;

import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class CommandBuffer {
    public static Queue<String> buffer = new ConcurrentLinkedQueue<>() {
    };
    public static long cd = 700;
    public static Thread thread = new Thread(() -> {
        while ( !Thread.currentThread().isInterrupted() ) {
            cd = Config.get().commandBufferCD.value;
            var connection =  Minecraft.getInstance().getConnection();
            if (Minecraft.getInstance().level == null || connection == null) {
                buffer.clear();
                waitThread(10);
                continue;
            }
            var command = buffer.poll();
            if (command == null) {
                waitThread(10);
                continue;
            }
            connection.sendCommand(command);
            waitThread(cd);
        }
    });

    public static void clear() { buffer.clear(); }

    public static void add(String command) {
        buffer.add(command);
    }

    private static void waitThread(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runTimer() {
        if (!thread.isAlive()) thread.start();
    }
}
