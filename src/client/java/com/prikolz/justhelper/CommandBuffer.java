package com.prikolz.justhelper;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CommandBuffer {
    public static List<String> buffer = new ArrayList<>();
    public static long cd = 700;
    public static Timer timer = null;

    public static void clear() { buffer.clear(); }

    public static void add(String command) { buffer.add(command); }

    public static void runTimer() {
        if (timer != null) timer.cancel();
        cd = Config.get().commandBufferCD.value;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Minecraft.getInstance().level == null) {
                    buffer.clear();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                if (buffer.isEmpty()) return;
                var command = buffer.removeFirst();
                var connection =  Minecraft.getInstance().getConnection();
                if (connection == null) {
                    buffer.clear();
                    return;
                }
                connection.sendCommand(command);
                try {
                    Thread.sleep(cd);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5, 5);
    }
}
