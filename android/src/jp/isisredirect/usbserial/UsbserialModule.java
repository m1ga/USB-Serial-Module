/*
Copyright 2013 Katsumi ISHIDA. All rights reserved.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 USA.

 Project home page: https://github.com/isis/USB-Serial-Module

 */
package jp.isisredirect.usbserial;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ti.modules.titanium.BufferProxy;


@Kroll.module(name = "Usbserial", id = "jp.isisredirect.usbserial")
public class UsbserialModule extends KrollModule {
    @Kroll.constant
    public static final String CONNECTED = "connected";
    @Kroll.constant
    public static final String DISCONNECTED = "disconnected";
    @Kroll.constant
    public static final String RECEIVED = "received";
    @Kroll.constant
    public static final String DATA = "data";
    @Kroll.constant
    public static final int DATABITS_5 = 5;
    @Kroll.constant
    public static final int DATABITS_6 = 6;
    @Kroll.constant
    public static final int DATABITS_7 = 7;
    @Kroll.constant
    public static final int DATABITS_8 = 8;
    @Kroll.constant
    public static final int FLOWCONTROL_NONE = 0;
    @Kroll.constant
    public static final int FLOWCONTROL_RTSCTS_IN = 1;
    @Kroll.constant
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;
    @Kroll.constant
    public static final int FLOWCONTROL_XONXOFF_IN = 4;
    @Kroll.constant
    public static final int FLOWCONTROL_XONXOFF_OUT = 8;
    @Kroll.constant
    public static final int PARITY_NONE = 0;
    @Kroll.constant
    public static final int PARITY_ODD = 1;
    @Kroll.constant
    public static final int PARITY_EVEN = 2;
    @Kroll.constant
    public static final int PARITY_MARK = 3;
    @Kroll.constant
    public static final int PARITY_SPACE = 4;
    @Kroll.constant
    public static final int STOPBITS_1 = 1;
    @Kroll.constant
    public static final int STOPBITS_1_5 = 3;
    @Kroll.constant
    public static final int STOPBITS_2 = 2;
    @Kroll.constant
    private static final int WRITE_WAIT_MILLIS = 2000;
    @Kroll.constant
    private static final int READ_WAIT_MILLIS = 2000;


    private static final String LCAT = "UsbserialModule";
    private static final boolean DBG = TiConfig.LOGD;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onRunError(Exception e) {
            Log.d(LCAT, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            Log.d(LCAT, "onNewData." + data.length);
            BufferProxy rec_buffer = new BufferProxy(data.length);
            rec_buffer.write(0, data, 0, data.length);

            KrollDict ret_data = new KrollDict();
            ret_data.put(TiC.EVENT_PROPERTY_SOURCE, UsbserialModule.this);
            ret_data.put(DATA, rec_buffer);
            fireEvent(RECEIVED, ret_data);
        }
    };

    private UsbSerialDriver mSerialDevice;
    private UsbSerialPort port;
    private SerialInputOutputManager mSerialIoManager;

    public UsbserialModule() {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        Log.d(LCAT, "inside onAppCreate");
    }

    @Override
    public void onStart(Activity activity) {
        super.onStart(activity);
    }


    @Override
    public void onResume(Activity activity) {
        super.onResume(activity);
    }


    @Override
    public void onPause(Activity activity) {
        super.onPause(activity);
    }

    @Override
    public void onStop(Activity activity) {
        super.onStop(activity);
    }

    @Override
    public void onDestroy(Activity activity) {
        super.onDestroy(activity);
        close();
        //mUsbManager = null;
    }


    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(LCAT, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (port != null) {
            Log.i(LCAT, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(port, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }


    // Methods
    @Kroll.method
    public boolean open(
            @Kroll.argument(optional = true) int baudRate,
            @Kroll.argument(optional = true) int dataBits,
            @Kroll.argument(optional = true) int stopBits,
            @Kroll.argument(optional = true) int parity) {

        KrollDict kd = new KrollDict();
        UsbManager manager = (UsbManager) TiApplication.getAppCurrentActivity().getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            kd.put("message","no driver");
            fireEvent("error", kd);
            Log.e(LCAT, "No driver");
            return false;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

        if (connection == null) {
            // no serial device
            Log.e(LCAT, "No connection");
            kd.put("message","no connection");
            fireEvent("error", kd);
            onDeviceStateChange();
            return false;
        } else {
            try {

                port = driver.getPorts().get(0); // Most devices have just one port (port 0)
                port.open(connection);
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                if (-1 != baudRate && -1 != dataBits && -1 != stopBits && -1 != parity) {
                    port.setParameters(baudRate, dataBits, stopBits, parity);
                }

            } catch (IOException e) {
                Log.e(LCAT, "Error setting up device: " + e.getMessage(), e);
                kd.put("message","setup: + " +e.getMessage());
                fireEvent("error", kd);

                try {
                    port.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                port = null;
                return false;
            }
        }
        onDeviceStateChange();
        return true;
    }

    @Kroll.method
    public void close() {
        stopIoManager();
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                // Ignore.
            } finally {
                port = null;
            }
        }
    }

    @Kroll.method
    public String getDeviceName() {
        if (port != null) {
            return port.getClass().getName()
                    .replace("com.hoho.android.usbserial.driver", "")
                    .replace("SerialDriver", "");
        } else {
            return "";
        }
    }

    @Kroll.method
    public boolean setParameters(int baudRate, int dataBits, int stopBits, int parity) {
        if (port != null) {
            try {
                port.setParameters(baudRate, dataBits, stopBits, parity);
            } catch (IOException e) {
                e.printStackTrace();
                close();
                return false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                close();
                return false;
            }
            return true;
        }
        return false;
    }


    @Kroll.method
    public void sendData(BufferProxy buffer) {
        if (port != null) {
            try {
                port.write(buffer.getBuffer(), WRITE_WAIT_MILLIS);
            } catch (IOException e) {
                Log.e(LCAT, "Error: " + e.getMessage());
            }
        }
    }

    // Properties
    @Kroll.getProperty
    @Kroll.method
    public boolean getIsConnected() {
        return (mSerialDevice != null);
    }
}
