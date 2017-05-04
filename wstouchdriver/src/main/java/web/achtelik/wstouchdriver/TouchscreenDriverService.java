package web.achtelik.wstouchdriver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.google.android.things.userdriver.InputDriver;
import com.google.android.things.userdriver.UserDriverManager;

import java.util.Map;

public class TouchscreenDriverService extends Service {

    private static final String DRIVER_NAME = "WS 7inch HDMI LCD";
    private static final int DRIVER_VERSION = 1;

    private static final int VENDOR_ID = 3823;
    private static final int PRODUCT_ID = 5;

    private static final int DISPLAY_SIZE_X = 800;
    private static final int DISPLAY_SIZE_Y = 480;

    private InputDriver inputDriver;

    private UsbDevice touchscreen;
    private UsbDeviceConnection touchscreenConnection;
    private UsbInterface touchscreenInterface;
    private UsbEndpoint touchscreenInputEndpoint;

    private Thread inputThread;

    @Override
    public void onCreate() {
        super.onCreate();

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        Map<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) {
                touchscreen = device;
                break;
            }
        }

        if (touchscreen == null) {
            throw new IllegalStateException("No touchscreen detected");
        }

        touchscreenConnection = usbManager.openDevice(touchscreen);
        touchscreenInterface = touchscreen.getInterface(0);
        if (!touchscreenConnection.claimInterface(touchscreenInterface, true)) {
            throw new IllegalStateException("Could not claim interface");
        }

        for (int i = 0; i < touchscreenInterface.getEndpointCount(); i++) {
            UsbEndpoint usbEndpoint = touchscreenInterface.getEndpoint(i);
            if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                touchscreenInputEndpoint = usbEndpoint;
                break;
            }
        }

        if (touchscreenInputEndpoint == null) {
            throw new IllegalStateException("Input endpoint not found");
        }

        inputDriver = InputDriver.builder(InputDevice.SOURCE_TOUCHSCREEN)
                .setName(DRIVER_NAME)
                .setVersion(DRIVER_VERSION)
                .setAbsMax(MotionEvent.AXIS_X, DISPLAY_SIZE_X)
                .setAbsMax(MotionEvent.AXIS_Y, DISPLAY_SIZE_Y)
                .build();

        UserDriverManager.getManager().registerInputDriver(inputDriver);

        inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[25];
                while (!inputThread.isInterrupted()) {
                    if (touchscreenConnection.bulkTransfer(touchscreenInputEndpoint, buffer, buffer.length, 100) >= 0) {
                        boolean press  = buffer[1] != 0;
                        int x = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
                        int y = ((buffer[4] & 0xFF) << 8) | (buffer[5] & 0xFF);
                        inputDriver.emit(x, y, press);
                    }
                }
            }
        });

        inputThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        inputThread.interrupt();

        UserDriverManager.getManager().unregisterInputDriver(inputDriver);

        touchscreenConnection.releaseInterface(touchscreenInterface);
        touchscreenConnection.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
