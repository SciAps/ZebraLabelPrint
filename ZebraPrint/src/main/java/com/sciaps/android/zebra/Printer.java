package com.sciaps.android.zebra;


import android.os.Environment;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionBuilder;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

public class Printer {

    public static final String PRINTER_WIDTH_FILE = "zebra_printer_width.cfg";
    private static final Logger logger = LoggerFactory.getLogger(Printer.class);

    String displayName;
    String connectionString;

    //assumes this is a RW220 (printer resolution of 203 dpi, printer head 2.25")
    int mPrinterWidth = (int) (203 * 2.84);
    private transient Connection mConnection;

    public static Printer createPrinter(DiscoveredPrinterBluetooth printer) {
        String connectionString = "BT:" + printer.address;
        String displayName = printer.friendlyName;
        return new Printer(connectionString, displayName);
    }

    public static Printer createPrinter(String displayName, String macAddress) {
        String connectionString = "BT:" + macAddress;
        return new Printer(connectionString, displayName);
    }

    private Printer() {
    }

    private Printer(String connectionString, String displayName) {
        this.connectionString = connectionString;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Connection getConnection() throws ConnectionException {
        if (mConnection == null) {
            mConnection = ConnectionBuilder.build(connectionString);
        }
        return mConnection;
    }

    public void getPrinterInfo() throws Exception {
        mConnection = getConnection();
        if (!mConnection.isConnected()) {
            mConnection.open();
        }

        if (mConnection.isConnected()) {
            ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(mConnection);
            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(genericPrinter);


            if (linkOsPrinter != null) {
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

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != Printer.class) {
            return false;
        }
        Printer other = (Printer) o;
        return connectionString.equals(other.connectionString);
    }

    @Override
    public int hashCode() {
        return connectionString.hashCode();
    }

    public int getPrinterWidth() {
        int savedWidth = mPrinterWidth;

        try {
            FileReader fileReader = new FileReader(Environment.getExternalStorageDirectory() + "/sciaps/" + PRINTER_WIDTH_FILE);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while((line = bufferedReader.readLine()) != null) {
                savedWidth = (int) (Double.parseDouble(line) * 203);
                break;
            }
            bufferedReader.close();
        } catch (Exception e) {
            logger.error("", e);
        }

        return savedWidth;
    }
}
