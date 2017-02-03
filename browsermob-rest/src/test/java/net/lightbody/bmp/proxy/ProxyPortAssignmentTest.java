package net.lightbody.bmp.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarPage;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.exception.ProxyExistsException;
import net.lightbody.bmp.exception.ProxyPortsExhaustedException;
import net.lightbody.bmp.proxy.test.util.ProxyManagerTest;

public class ProxyPortAssignmentTest extends ProxyManagerTest {

    @Override
    public String[] getArgs() {
        return new String[] { "--proxyPortRange", "9091-9093" };
    }

    @Test
    public void testAutoAssignment() throws Exception {
        int[] ports = { 9091, 9092, 9093 };
        LegacyProxyServer p;
        for (int port : ports) {
            p = proxyManager.create(new HashMap<String, String>());
            assertEquals(port, p.getPort());
        }
        try {
            proxyManager.create(new HashMap<String, String>());
            fail();
        } catch (ProxyPortsExhaustedException e) {
            proxyManager.delete(9093);
            p = proxyManager.create(new HashMap<String, String>());
            assertEquals(9093, p.getPort());

            proxyManager.delete(9091);
            p = proxyManager.create(new HashMap<String, String>());
            assertEquals(9091, p.getPort());

            for (int port : ports) {
                proxyManager.delete(port);
            }
        }
    }

    @Test
    public void testManualAssignment() throws Exception {
        LegacyProxyServer p = proxyManager.create(new HashMap<String, String>(), 9094);
        assertEquals(9094, p.getPort());
        try {
            proxyManager.create(new HashMap<String, String>(), 9094);
            fail();
        } catch (ProxyExistsException e) {
            assertEquals(9094, e.getPort());
            proxyManager.delete(9094);
        }
    }

    @Test
    public void testHarEntries() throws Exception {
        LegacyProxyServer p = proxyManager.create(new HashMap<String, String>(), 9094);
        assertEquals(9094, p.getPort());

        p.newHar("1234");

        //Mock Entry dans le Har
        HarEntry entry = new HarEntry("1234");
        HarLog log = new HarLog();
        log.setEntry(entry);
        log.setPage(new HarPage("1234"));
        p.getHar().setLog(log);

        //Recuperer les entries du Har
        List<HarEntry> entries = p.getEntriesWithPageRef("1234");
        String pageref = entries.get(0).getPageref();
        System.out.println(entries.size());
        System.out.println(pageref);

        try {
            proxyManager.create(new HashMap<String, String>(), 9094);
            fail();
        } catch (ProxyExistsException e) {
            assertEquals(9094, e.getPort());
            proxyManager.delete(9094);
        }
    }

    @Test
    public void testHarEntriesContainingUrl() throws Exception {
        LegacyProxyServer p = proxyManager.create(new HashMap<String, String>(), 9094);
        String url = "https://test.url.fr/hit.xiti?p=parametre";

        p.newHar("1234");

        //Mock Entry dans le Har
        HarEntry entry = new HarEntry("1234");
        //Mock Request dans le HarEntry
        HarRequest request = new HarRequest();
        request.setUrl(url);

        entry.setRequest(request);
        HarLog log = new HarLog();
        log.setEntry(entry);
        log.setPage(new HarPage("1234"));
        p.getHar().setLog(log);

        //Recuperer les entries du Har
        List<HarEntry> entries = p.getEntriesWithPageRefContainingUrl("1234", "https://test.url.fr/hit.xiti?");
        String pageref = entries.get(0).getPageref();
        System.out.println(entries.size());
        System.out.println(pageref);

        try {
            proxyManager.create(new HashMap<String, String>(), 9094);
            fail();
        } catch (ProxyExistsException e) {
            assertEquals(9094, e.getPort());
            proxyManager.delete(9094);
        }
    }
}
