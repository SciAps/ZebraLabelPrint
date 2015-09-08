package com.sciaps.android.zebra;


import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class Printer {


    private static final Logger logger = LoggerFactory.getLogger(Printer.class);

    public static Printer createPrinter(DiscoveredPrinter printer) {
        return new Printer(printer.getConnection());
    }

    private final Connection mConnection;

    private Printer(Connection connection) {
        mConnection = connection;
    }

    public void getPrinterInfo() throws Exception {
        if(!mConnection.isConnected()) {
            mConnection.open();
        }

        if(mConnection.isConnected()) {
            ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(mConnection);
            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(genericPrinter);


            if(linkOsPrinter != null) {
                logger.info("Available Settings for myDevice");
                Set<String> availableSettings = linkOsPrinter.getAvailableSettings();
                for (String setting : availableSettings) {
                    logger.info(setting + ": Range = (" + linkOsPrinter.getSettingRange(setting) + ")");
                }

                logger.info("\nCurrent Setting Values for myDevice");
                Map<String, String> allSettingValues = linkOsPrinter.getAllSettingValues();
                for (String settingName : allSettingValues.keySet()) {
                    logger.info(settingName + ":" + allSettingValues.get(settingName));
                }

                String darknessSettingId = "print.tone";
                String newDarknessValue = "10.0";
                if (availableSettings.contains(darknessSettingId) &&
                        linkOsPrinter.isSettingValid(darknessSettingId, newDarknessValue) &&
                        linkOsPrinter.isSettingReadOnly(darknessSettingId) == false) {
                    linkOsPrinter.setSetting(darknessSettingId, newDarknessValue);
                }

                logger.info("\nNew " + darknessSettingId + " Value = " + linkOsPrinter.getSettingValue(darknessSettingId));
            }

            mConnection.close();
        }
    }
}
