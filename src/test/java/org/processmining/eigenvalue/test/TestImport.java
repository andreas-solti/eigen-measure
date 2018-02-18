package org.processmining.eigenvalue.test;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.junit.Assert;
import org.junit.Test;
import org.xeslite.external.XFactoryExternalStore;
import org.xeslite.lite.factory.XFactoryLiteImpl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collection;

public class TestImport {

    protected MemoryUsage getMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getHeapMemoryUsage();
    }

    private void measureMemory() {
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        System.out.println("Memory Used: " + getMemoryUsage().getUsed() / 1024 / 1024 + " MB ");
    }

    public static Collection<XLog> getBPILogs() throws Exception {
        XUniversalParser parser = new XUniversalParser();
        File logFile = new File("test/logs/BPI Challenge 2017.xes.gz");
        Collection<XLog> logs = parser.parse(logFile);

        for(XLog log : logs){
            System.out.println("Loaded log: " + XConceptExtension.instance().extractName(log));
        }
        return logs;
    }

    @Test
    public void testLoadLog() throws Exception {
        XFactoryRegistry.instance().setCurrentDefault(new XFactoryNaiveImpl());
        Collection<XLog> logs = getBPILogs();
        Assert.assertTrue(logs.size() > 0);

        measureMemory();
    }

    @Test
    public void testLoadLite() throws Exception {
        XFactoryRegistry.instance().setCurrentDefault(new XFactoryLiteImpl());

        Collection<XLog> logs = getBPILogs();
        Assert.assertTrue(logs.size() > 0);

        measureMemory();
    }

    @Test
    public void testLoadLiteInMemoryStore() throws Exception {
        XFactoryRegistry.instance().setCurrentDefault(new XFactoryExternalStore.InMemoryStoreImpl());

        Collection<XLog> logs = getBPILogs();
        Assert.assertTrue(logs.size() > 0);

        measureMemory();
    }


}