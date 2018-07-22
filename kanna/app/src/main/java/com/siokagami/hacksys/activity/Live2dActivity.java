package com.siokagami.hacksys.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rokid.voicerec.BearKidAdapter;
import com.rokid.voicerec.BearKidCallback;
import com.rokid.voicerec.CustomWord;
import com.siokagami.hacksys.R;
import com.siokagami.hacksys.bean.NLPBean;
import com.siokagami.hacksys.live2d.LAppDefine;
import com.siokagami.hacksys.live2d.LAppLive2DManager;
import com.siokagami.hacksys.live2d.LAppView;
import com.siokagami.hacksys.live2d.android.FileManager;
import com.siokagami.hacksys.live2d.android.SoundManager;
import com.siokagami.hacksys.utils.Logger;
import com.siokagami.hacksys.utils.MyLog;
import com.siokagami.hacksys.utils.MyThread;

import java.nio.ByteBuffer;

/**
 * @author siokagami
 */
public class Live2dActivity extends AppCompatActivity implements Runnable {

    //  Live2Dの管理

    private LAppLive2DManager live2DMgr;
    static private Activity instance;

    private UsbManager usbManager;
    private UsbRequest request;
    private UsbDevice deviceFound;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterfaceFound = null;
    private UsbEndpoint endpointOut = null;
    private UsbEndpoint endpointIn = null;

    private static final String KEY_NLP = "rknlp";

    public Live2dActivity() {
        instance = this;
        if (LAppDefine.DEBUG_LOG) {
            Log.d("", "==============================================\n");
            Log.d("", "   Live2D Sample  \n");
            Log.d("", "==============================================\n");
        }

        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        voiceLocation();
        parseIntent(intent);
    }

    private void voiceLocation() {
        try {
            String s = MyLog.MLog.getSystemLog();
            String[] sResult = s.split("\n");
            String bfResult = sResult[sResult.length - 1];
            String[] tempLo = bfResult.split("BF: ");
            String[] secLo = tempLo[1].split(" ");
            final String res = secLo[0];
            Log.d("nimabi", res);

            final Double rad = Double.parseDouble(res);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (rad > 0 && rad < 180) {
                        live2DMgr.startRightMotion();
                    } else {
                        live2DMgr.startLeftMotion();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void parseIntent(Intent intent) {
        if (intent == null) {
            Logger.d("intent null !");
            return;
        }
        String nlp = intent.getStringExtra(KEY_NLP);
        if (TextUtils.isEmpty(nlp)) {
            Logger.d("nlp invalidate !");
            return;
        }
        NLPBean nlpBean = new Gson().fromJson(nlp, NLPBean.class);
        String intentEvent = nlpBean.getIntent();
    }


    static public void exit() {
        SoundManager.release();
        instance.finish();
    }


    /*
     * Activityが作成されたときのイベント
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // GUIを初期化
        setupGUI();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        new Thread(new MyThread(1,usbManager)).start();

        FileManager.init(this.getApplicationContext());
    }


    /*
     * GUIの初期化
     * activity_main.xmlからViewを作成し、そこにLive2Dを配置する
     */
    void setupGUI() {
        setContentView(R.layout.activity_live2d);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //  Viewの初期化
        LAppView view = live2DMgr.createView(this);

        // activity_main.xmlにLive2DのViewをレイアウトする
        FrameLayout layout = (FrameLayout) findViewById(R.id.live2DLayout);
        layout.addView(view, 0, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


        // モデル切り替えボタン
        ImageButton iBtn = (ImageButton) findViewById(R.id.imageButton1);
        ClickListener listener = new ClickListener();
        iBtn.setOnClickListener(listener);
    }

    private void setDevice(UsbDevice device) {
        usbInterfaceFound = null;
        endpointOut = null;
        endpointIn = null;

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbif = device.getInterface(i);

            UsbEndpoint tOut = null;
            UsbEndpoint tIn = null;

            int tEndpointCnt = usbif.getEndpointCount();
            if (tEndpointCnt >= 2) {
                for (int j = 0; j < tEndpointCnt; j++) {
                    if (usbif.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT) {
                            tOut = usbif.getEndpoint(j);
                        } else if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN) {
                            tIn = usbif.getEndpoint(j);
                        }
                    }
                }

                if (tOut != null && tIn != null) {
                    // This interface have both USB_DIR_OUT
                    // and USB_DIR_IN of USB_ENDPOINT_XFER_BULK
                    usbInterfaceFound = usbif;
                    endpointOut = tOut;
                    endpointIn = tIn;
                }
            }

        }

        if (usbInterfaceFound == null) {
            return;
        }

        deviceFound = device;

        if (device != null) {
            UsbDeviceConnection connection =
                    usbManager.openDevice(device);
            if (connection != null &&
                    connection.claimInterface(usbInterfaceFound, true)) {
                usbDeviceConnection = connection;
                Thread thread = new Thread(this);
                thread.start();

            } else {
                usbDeviceConnection = null;
            }
        }
    }

    private void sendCommand(int control) {
        synchronized (this) {

            if (usbDeviceConnection != null) {
                byte[] message = new byte[1];
                message[0] = (byte) control;
                usbDeviceConnection.bulkTransfer(endpointOut,
                        message, message.length, 0);
            }
        }
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        request = new UsbRequest();
        request.initialize(usbDeviceConnection, endpointIn);
        while (true) {
            request.queue(buffer, 1);
            if (usbDeviceConnection.requestWait() == request) {
                byte rxCmd = buffer.get(0);
                if (rxCmd != 0) {
                    if((int)rxCmd==2){
                        live2DMgr.startShineMotion();
                    }
                    Log.d("nimabi", "run: " + (int) rxCmd);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }

    }


    // ボタンを押した時のイベント
    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "change model", Toast.LENGTH_SHORT).show();
//            live2DMgr.startTestMotion();
            //live2DMgr.changeModel();//Live2D Event
        }
    }


    /*
     * Activityを再開したときのイベント。
     */
    @Override
    protected void onResume() {
        //live2DMgr.onResume() ;
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();

        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            setDevice(device);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            if (deviceFound != null && deviceFound.equals(device)) {
                setDevice(null);
            }
        }
    }


    /*
     * Activityを停止したときのイベント。
     */
    @Override
    protected void onPause() {
        live2DMgr.onPause();
        super.onPause();
    }
}
