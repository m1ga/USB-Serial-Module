# Step by step configuration to use the USB Serial Module in your app and USB device

## Description

The USB Serial Module provides a simple support for USB serial communication using *usb-serial-for-android*.

For details of usb-serial-for-android*, see below site  :
[*usb-serial-for-android*](https://code.google.com/p/usb-serial-for-android/)

This document will show how to configure a Titanium project for using ADK step by step.

## Prepare
### Setup Google Android SDK and NDK

This library requires Google API Level 10 or later and you will the Android NDK (Native Development Kit) so you can build and package it as an Android Module in the Titanium Studio.  

Note : General issues of Titanium mobile for Android setup are [here](https://wiki.appcelerator.org/display/guides/Quick+Start)

## Build the Titanium Module

1. Setup the build.properties file according to your environment;
2. Build and package it as an Android Module for the Titanium SDK so it can be available for all projects in your workspace.

## Create your Titanium project

When you create your new Titanium project, you must setup Android SDK configuration to support ADK.  

1.	Select [*File > New > Titanium Mobile Project*]
1.	Select [*Set-up/Configure SDKs*] in "*New Titanium Mobile Project*"
2.	"*Android SDK Home*" is set to your Android SDK home according to your own environment.
3.	Select "*Google APIs Android 2.3.3*" or later in "*Default Android SDK*" (sure that "Google APIs .." , not "Andeoid ..")
4.	Setup other elements if needed, and click [Finish] button. At that time, "*Titanium SDK version*" must be set to "*2.1.0 GA*" or later.

## Modify your Titanium project settings

### Modify "Tiapp.xml"

#### Add &lt;module&gt; tag

To use USB Serial Module, add the "jp.isisredirect.usbserial" module to your project editing the "tiapp.xml" file using the Overview tab in the Titanium Studio IDE, or directly adding the flowing &lt;module&gt; tag in &lt;modules&gt; session.

     <modules>
        <module platform="android">jp.isisredirect.usbserial</module>
     </modules>

#### Example "Tiapp.xml" resulted by above

	<?xml version="1.0" encoding="UTF-8"?>
	<ti:app xmlns:ti="http://ti.appcelerator.org">
      	... skipped ....
         <modules>
             <module platform="android">jp.isisredirect.usbserial</module>
         </modules>
     </ti:app>

### Add device_filter.xml
To determine what USB device can associate with your Titanium app, some values (vendor-id and/or product-id) must be specified by &lt;usb-device&gt; tag in &lt;resources&gt; session of the device_filter.xml file.

The device_filter.xml file must be located in

     <project_dir>/platform/android/res/xml/

and must be named just "device_filter.xml".

For example, device_filter.xml is below :  

     <?xml version="1.0" encoding="utf-8"?>
     <resources>
		<!--  0x0403 / 0x6001: FTDI FT232R UART  -->
		<usb-device vendor-id="1027" product-id="24577"/>
		<!--  0x2341 / Arduino  -->
		<usb-device vendor-id="9025"/>
		<!--  0x16C0 / 0x0483: Teensyduino   -->
		<usb-device vendor-id="5824" product-id="1155"/>
		<!--  0x10C4 / 0xEA60: CP210x UART Bridge  -->
		<usb-device vender-id="4292" product-id="60000"/>
     </resources>

These values may be specified by the vender id and the product id of USB device.
Currently supported USB devices vender id and product id are below:

FTDI FT232R UART	vendor-id="1027" product-id="24577"
CP210x UART Bridge	vender-id="4292" product-id="60000"
Arduino vendor-id="9025"
Teensyduino	vendor-id="5824" product-id="1155"


You should determine your values that specifies your USB device to communicate.

### Add intent filter and metadata
To notify device_filter infomation to Android OS and to let Android OS to launch your app when USB device is connected, you must add &lt;activity&gt; tag and intent filter to your "tiapp.xml".

Here is an example: (replace &lt;your app name&gt; with your app name and &lt;Yourappname&gt; with your app name *without spaces and with only the first letter in uppercase*)

	<?xml version="1.0" encoding="UTF-8"?>
	<ti:app xmlns:ti="http://ti.appcelerator.org">
      	... skipped ....
	    <android xmlns:android="http://schemas.android.com/apk/res/android">
	        <manifest>
	            <application>
			 		<activity android:configChanges="keyboardHidden|orientation"
			 			android:label="<your app name>"
			 			android:name=".<Yourappname>Activity"
			 			android:launchMode="singleTask"
			 			android:theme="@style/Theme.Titanium">
						<intent-filter>
							<action android:name="android.intent.action.MAIN"/>
							<category android:name="android.intent.category.LAUNCHER"/>
						</intent-filter>
						<intent-filter>
							<action android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" />
						</intent-filter>
						<meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" android:resource="@xml/device_filter"/>
					</activity>
	           </application>
	        </manifest>
	    </android>
      	... skipped ....
         <modules>
             <module platform="android">jp.isisredirect.usbserial</module>
         </modules>
     </ti:app>


### That's all.
Next, write your Titanium app by JavaScript!

## Author

Kastumi ISHIDA (isis re-direct) in k.i.office.

isis.redirect4@gmail.com


## License
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

## License of third party
[*usb-serial-for-android*](https://code.google.com/p/usb-serial-for-android/)

usb-serial-for-android is written and maintained by mike wakerly.

This library is licensed under LGPL Version 2.1. Please see LICENSE.txt for the complete license.

Copyright 2011-2012, Google Inc. All Rights Reserved.

Portions of this library are based on libftdi (http://www.intra2net.com/en/developer/libftdi). Please see FtdiSerialDriver.java for more information.
