package com.prikolz.justhelper;

import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class CommandBuffer {
    public static Queue<String> buffer = new ConcurrentLinkedQueue<>() {};
    public static long currentCd = 0;

    public static void tick(long delta) {
        var connection =  Minecraft.getInstance().getConnection();
        if (Minecraft.getInstance().level == null || connection == null) {
            buffer.clear();
            return;
        }
        currentCd -= delta;
        if (currentCd > 0) return;
        currentCd = 0;
        var command = buffer.poll();
        if (command == null) return;
        currentCd = Config.get().commandBufferCD.value;
        connection.sendUnattendedCommand(command, null);
    }

    public static void clear() { buffer.clear(); }

    public static void add(String command) {
        buffer.add(command);
    }
}
