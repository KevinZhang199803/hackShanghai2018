package com.siokagami.hacksys.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MyLog {
    public static class MLog    //静态类
    {
        private static final String TAG = "siokagami";

        public static String getSystemLog() {

            Process process = null;
            StringBuilder debugLog=new StringBuilder();
            try {
                process = Runtime.getRuntime().exec("logcat -d -s RKUPL_r2audio");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    if(line.contains("--------------Second BF")){
                        debugLog.append(line);
                        debugLog.append("\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process!=null)
                    process.destroy();
            }
            return debugLog.toString();
        }

        public static void getLog() {
            System.out.println("--------func start--------"); // 方法启动
            try {
                String[] running = new String[]{"logcat", "-s", "adb logcat *: I"};
                Process exec = Runtime.getRuntime().exec(running);
                final InputStream is = exec.getInputStream();

                new Thread() {
                    @Override
                    public void run() {
                        FileOutputStream os = null;
                        try {
                            //新建一个路径信息
                            os = new FileOutputStream("/sdcard/Log/Log.txt");
                            int len = 0;
                            byte[] buf = new byte[1024];
                            while (-1 != (len = is.read(buf))) {
                                os.write(buf, 0, len);
                                os.flush();
                            }
                        } catch (Exception e) {
                            Log.d("writelog",
                                    "read logcat process failed. message: "
                                            + e.getMessage());
                        } finally {
                            if (null != os) {
                                try {
                                    os.close();
                                    os = null;
                                } catch (IOException e) {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.d("writelog",
                        "open logcat process failed. message: " + e.getMessage());
            }
        }

    }
}