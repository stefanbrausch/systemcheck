package org.jenkinsci.plugins.systemcheck;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.UnprotectedRootAction;
import hudson.remoting.Callable;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Extension
public class SystemCheckRootAction implements UnprotectedRootAction {

    private int oks;
    private int warnings;
    private int failures;
    private int unkowns;
    private ArrayList<CheckDetails> list = new ArrayList<CheckDetails>();

    public String getDisplayName() {

        return null;
    }

    public String getIconFileName() {

        return null;
    }

    public String getUrlName() {

        return "status";
    }

    public SystemCheckRootAction() {
        super();
    }

    public boolean check() {
        failures = 0;
        oks = 0;
        warnings = 0;
        unkowns = 0;

        list.clear();
        // Slave offline check
        Date d = new Date();
        int count = getNumberOfOfflineSlaves();
        
        list.add(new CheckDetails("SlaveOfflineCheck", count == 0 ? "OK" : "WARNING", d.toString(), count + " of " + getNumbersOfSlaves() + " Slave(s) is/are offline"));
        if (count == 0)
            oks++;
        else
            warnings++;
        d = new Date();
        Computer c;
        c = Hudson.getInstance().getComputer("(master)");
        String load = "0";
        boolean loadCheckOkay = true;
        try {
            load = c.getChannel().call(new MonitorTask());
        } catch (IOException e) {
            unkowns++;
            loadCheckOkay = false;
            e.printStackTrace();
        } catch (RuntimeException e) {
            unkowns++;
            loadCheckOkay = false;
            e.printStackTrace();
        } catch (InterruptedException e) {
            unkowns++;
            loadCheckOkay = false;
            e.printStackTrace();
        }
        if (loadCheckOkay) {
            float fLoad = Float.valueOf(load);
            list.add(new CheckDetails("MasterLoadCheck", fLoad < 4 ? "OK" : "WARNING", d.toString(), "System Load of Master is: "
                    + load));
            if (fLoad < 2)
                oks++;
            else
                warnings++;
        } else {
            list.add(new CheckDetails("MasterLoadCheck", "UNKOWN", d.toString(), "System Load of Master is unkown"));
        }

        // URL connection speed test
        URL url;
        try {
            url = new URL(Hudson.getInstance().getRootUrl());

            HttpURLConnection connection;
       
            
            connection = (HttpURLConnection) url.openConnection();
            int status;
            Date start = new Date();
            status = connection.getResponseCode();

            Date end = new Date();
            d = new Date();
            long loadingTime = end.getTime() - start.getTime();
            list.add(new CheckDetails("FrontendLoadingTime", loadingTime < 1000 ? "OK" : "WARNING", d.toString(),
                    "Frontend Loading Time is: " + loadingTime+ " ms"));
            if (loadingTime < 100)
                oks++;
            else
                warnings++;

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Queue length check
        d = new Date();
        int queueLength = Hudson.getInstance().getQueue().getBuildableItems().size();
        list.add(new CheckDetails("WaitingJobsForFreeExecutor", queueLength == 0 ? "OK" : "WARNING", d.toString(),
                queueLength + " Jobs are waiting for a free executor"));
        if (queueLength == 0)
            oks++;
        else
            warnings++;

        return true;
    }

    public String getSummary() {
        return failures == 0 ? "OK" : "FAILURE";
    }

    public String getOks() {
        return String.valueOf(oks);

    }

    public String getWarnings() {
        return String.valueOf(warnings);
    }

    public String getUnkowns() {
        return String.valueOf(unkowns);
    }

    public String getFailures() {
        return String.valueOf(failures);
    }

    public ArrayList<CheckDetails> getDetails() {
        return list;
    }

    public int getNumberOfOfflineSlaves() {
        int count = 0;
        for (Node n : Hudson.getInstance().getNodes()) {
            if (n.toComputer().isOffline()) {
                count++;
            }
        }
        return count;
    }
    
    public int getNumbersOfSlaves() {
        
        List <Node> slaves = Hudson.getInstance().getNodes();
        
        return slaves.size();
    }

    /**
     * Task which returns the SystemLoadAverage.
     */
    static final class MonitorTask implements Callable<String, RuntimeException> {
        private static final long serialVersionUID = 1L;

        /**
         * Detect the System Load Average.
         */
        public String call() {
            final OperatingSystemMXBean opsysMXbean = ManagementFactory.getOperatingSystemMXBean();
            return String.valueOf(opsysMXbean.getSystemLoadAverage());
        }
    }

}
