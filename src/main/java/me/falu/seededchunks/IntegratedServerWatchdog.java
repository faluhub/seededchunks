package me.falu.seededchunks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class IntegratedServerWatchdog implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IntegratedServer server;

    public IntegratedServerWatchdog(IntegratedServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            long l = this.server.getServerStartTime();
            long m = Util.getMeasuringTimeMs();
            long n = m - l;
            if (MinecraftClient.getInstance().options.keyAdvancements.wasPressed()) {
                LOGGER.fatal("A single server tick took {} seconds (should be max {})", String.format(Locale.ROOT, "%.2f", (float) n / 1000.0F), String.format(Locale.ROOT, "%.2f", 0.05F));
                LOGGER.fatal("Considering it to be crashed, server will forcibly shutdown.");
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
                StringBuilder stringBuilder = new StringBuilder();
                Error error = new Error();
                for (ThreadInfo threadInfo : threadInfos) {
                    if (threadInfo.getThreadId() == this.server.getThread().getId()) {
                        error.setStackTrace(threadInfo.getStackTrace());
                    }
                    stringBuilder.append(threadInfo);
                    stringBuilder.append("\n");
                }
                CrashReport crashReport = new CrashReport("Watching Server", error);
                this.server.populateCrashReport(crashReport);
                CrashReportSection crashReportSection = crashReport.addElement("Thread Dump");
                crashReportSection.add("Threads", stringBuilder);
                File file = new File(new File(this.server.getRunDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
                if (crashReport.writeToFile(file)) {
                    LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }
                this.shutdown();
            }
        }
    }

    private void shutdown() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Runtime.getRuntime().halt(1);
                }
            }, 10_000L);
            System.exit(1);
        } catch (Throwable throwable) {
            Runtime.getRuntime().halt(1);
        }
    }
}
