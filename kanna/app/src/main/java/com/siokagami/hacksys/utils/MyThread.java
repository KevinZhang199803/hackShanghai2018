package com.siokagami.hacksys.utils;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Administrator on 2016/9/28 0028.
 */
public class MyThread implements Runnable {
    int count = 1, number;
    UsbManager mUsbManager;

    public MyThread(int num, UsbManager usbManager) {
        number = num;
        System.out.println("创建线程" + number);
        mUsbManager = usbManager;
    }

    public void run() {
        while (true) {
//            System.out.println("线程" + number + ":计数" + count);
            HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
            Iterator<UsbDevice> iterator = deviceHashMap.values().iterator();

//            System.out.println("deviceHashMap.isEmpty()" + deviceHashMap.isEmpty());

            while (iterator.hasNext()) {
                UsbDevice usbDevice = iterator.next();
                UsbInterface usbInterface = usbDevice.getInterface(0);//USBEndpoint为读写数据所需的节点
                UsbEndpoint inEndpoint = usbInterface.getEndpoint(0);  //读数据节点
                UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
                connection.claimInterface(usbInterface, true);

                //发送数据
                byte[] byte2 = new byte[1024];

//                //读取数据1   两种方法读取数据
                int ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 3000);
//                System.out.println("ret");
//                Log.i("ret", "ret:" + ret);
//                Log.i("ret", "bat is ok  length:" + byte2.length);
                for (Byte byte1 : byte2) {
//                    Log.i("ret", "byte1 :" + byte1);
                }
            }
        }
    }
}
