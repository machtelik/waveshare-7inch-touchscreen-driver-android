# WaveShare 7-inch user space driver

AndroidThings driver for old version of the [WaveShare 7-inch HDMI LCD](http://www.waveshare.net/shop/7inch-HDMI-LCD-B.htm) that did not use the standard HID protocol.

This driver was inspired by the work of derekhe  https://github.com/derekhe/waveshare-7inch-touchscreen-driver

# Usage

Add the following lines to the config.txt so the display is setup correctly

    max_usb_current=1
    hdmi_group=2
    hdmi_mode=1
    hdmi_mode=87
    hdmi_cvt 800 480 60 6 0 0 0

After that simply start the service when starting your app

    startService(new Intent(getApplicationContext(), TouchscreenDriverService.class))

