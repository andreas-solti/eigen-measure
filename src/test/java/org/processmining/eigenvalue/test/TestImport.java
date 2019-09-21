package org.processmining.eigenvalue.test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

//import org.xeslite.external.XFactoryExternalStore;
//import org.xeslite.lite.factory.XFactoryLiteImpl;

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



//    @Test
//    public void testLoadLog() throws Exception {
//        XFactoryRegistry.instance().setCurrentDefault(new XFactoryNaiveImpl());
//        Collection<XLog> logs = getBPILogs();
//        Assert.assertTrue(logs.size() > 0);
//
//        measureMemory();
//    }
//
//    @Test
//    public void testLoadLite() throws Exception {
//        XFactoryRegistry.instance().setCurrentDefault(new XFactoryLiteImpl());
//
//        Collection<XLog> logs = getBPILogs();
//        Assert.assertTrue(logs.size() > 0);
//
//        measureMemory();
//    }
//
//    @Test
//    public void testLoadLiteInMemoryStore() throws Exception {
//        XFactoryRegistry.instance().setCurrentDefault(new XFactoryExternalStore.InMemoryStoreImpl());
//
//        Collection<XLog> logs = getBPILogs();
//        Assert.assertTrue(logs.size() > 0);
//
//        measureMemory();
//    }


}
