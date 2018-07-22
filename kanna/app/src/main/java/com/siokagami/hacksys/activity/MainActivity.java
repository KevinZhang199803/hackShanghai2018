package com.siokagami.hacksys.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rokid.voicerec.BearKidAdapter;
import com.rokid.voicerec.BearKidCallback;
import com.rokid.voicerec.CustomWord;
import com.siokagami.hacksys.utils.Logger;
import com.siokagami.hacksys.R;
import com.siokagami.hacksys.bean.NLPBean;
import com.siokagami.hacksys.utils.MyLog;

import cn.trinea.android.common.util.ShellUtils;

/**
 * @author siokagami
 */
public class MainActivity extends AppCompatActivity implements BearKidCallback {

    private static final String KEY_NLP = "rknlp";
    private static String SPEECH_VOICE = "speeckVoice";
    private static int PROCESS_LOGCAT = 1001;

    private Button btnArduinoControl;
    private Button btnLive2dDemo;
    private TextView tvRokidIntent;
    private Thread logcatThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        initSpeech();
    }

    private void initSpeech(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                BearKidAdapter bearKidAdapter = new BearKidAdapter();
                boolean initialize = bearKidAdapter.initialize(getApplicationContext(), "com.rokid.openvoice.VoiceService", MainActivity.this);
                Log.d("siokagami", "run: "+initialize);
                int i = bearKidAdapter.addCustomWord(CustomWord.TYPE_VT_WORD, "若琪", "ruo4qi2");
            }
        }).start();
    }

    private void initView() {
        btnArduinoControl = findViewById(R.id.btn_arduino_control);
        btnLive2dDemo = findViewById(R.id.btn_live2d_demo);

        tvRokidIntent = findViewById(R.id.tv_rokid_intent);

        btnArduinoControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ArduinoControlActivity.class));
            }
        });

        btnLive2dDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Live2dActivity.class));
            }
        });
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        voiceLocation();
//        parseIntent(intent);
//    }

    private void voiceLocation(){
        String s = MyLog.MLog.getSystemLog();
        String[] sResult = s.split("\n");
        String bfResult = sResult[sResult.length-1];
        String[] tempLo = bfResult.split("BF: ");
        String[] secLo = tempLo[1].split(" ");
        String res = secLo[0];

        Integer rad = Integer.parseInt(res);
        Log.d("nimabi",res);
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
        tvRokidIntent.setText(nlp);
    }


    @Override
    public void onVoiceEvent(int i, int i1, double v) {
        Log.d(SPEECH_VOICE, "onVoiceEvent: "+i+" "+i1);


    }

    @Override
    public void onIntermediateResult(String s) {
        Log.d(SPEECH_VOICE, "onIntermediateResult: "+s);

    }

    @Override
    public void onRecognizeResult(String s, String s1, String s2) {
        Log.d(SPEECH_VOICE, "onRecognizeResult: "+s+" "+s1+" "+s2   );
    }

    @Override
    public void onException(int i) {

    }
}
