package dev.ripple.api.utils.verification;


import dev.ripple.api.utils.verification.logging.Logger;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;

public class TimeChecker {
    private static final String[] NTP_SERVERS = {
            "ntp.aliyun.com",
            "ntp1.aliyun.com",
            "ntp2.aliyun.com",
            "ntp3.aliyun.com",
            "ntp4.aliyun.com",
            "ntp5.aliyun.com",
            "ntp6.aliyun.com",
            "ntp7.aliyun.com"
    };
    private static final long MAX_TIME_DIFFERENCE_MS = 500;

    public static boolean checkTime() {
        NTPUDPClient timeClient = new NTPUDPClient();
        try {
            timeClient.setDefaultTimeout(5000);
            String ntpServer = NTP_SERVERS[(int) (Math.random() * NTP_SERVERS.length)];
            TimeInfo timeInfo = timeClient.getTime(InetAddress.getByName(ntpServer));
            long ntpTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            long localTime = System.currentTimeMillis();
            long timeDifference = ntpTime - localTime;

            Logger.info("NTP Server: " + ntpServer);
            Logger.info(String.format("Time Check: NTP=%d, Local=%d, Difference=%+d ms",
                    ntpTime, localTime, timeDifference));

            return Math.abs(timeDifference) <= MAX_TIME_DIFFERENCE_MS;
        } catch (Exception e) {
            Logger.error("Time synchronization failed: " + e.getMessage());
        } finally {
            timeClient.close();
        }
        return false;
    }
}