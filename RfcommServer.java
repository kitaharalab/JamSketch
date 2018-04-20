//package com.example.mizun.kitaharasystem;

/**
 * Created by korona on 2017/04/11.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 * クライアントからの文字列を受信するBluetooth サーバ。
 */
public class RfcommServer implements MotionController {
    /**
     * スマホ側との通信部分(見る必要なし)
     UUIDは乱数生成したものを使用
     */
    static final String serverUUID = "17fcf242f86d4e35805e546ee3040b84";

    private StreamConnectionNotifier server = null;
    private List<Long> time;
    private List<Float> position;
    private List<Integer> attacktiming_switch;
    private List<Session> sessions = null;
    private TargetMover tm;
    
//    static int user = 1;

    public RfcommServer() throws IOException {
        // RFCOMMベースのサーバの開始。
        // - btspp:は PRCOMM 用なのでベースプロトコルによって変わる。
        server = (StreamConnectionNotifier) Connector.open(
                "btspp://localhost:" + serverUUID,
                Connector.READ_WRITE, true//読み書きモードでサーバースタート
        );
        // ローカルデバイスにサービスを登録。
        ServiceRecord record = LocalDevice.getLocalDevice().getRecord(server);
        LocalDevice.getLocalDevice().updateRecord(record);
	    time = Collections.synchronizedList(new LinkedList<Long>());
        position = Collections.synchronizedList(new LinkedList<Float>());
        attacktiming_switch = Collections.synchronizedList(new LinkedList<Integer>());
        sessions = Collections.synchronizedList(new ArrayList<Session>());
    }

    public void setTarget(TargetMover tm) {
	   this.tm = tm;
    }
    
    public void start() {
        if (sessions.isEmpty()) {
            throw new IllegalStateException("init() must be called in advance");
        } else {
            for (Session session : sessions) {
                new Thread(session, session.getRemoteDeviceName()).start();
            }    
        }
    }

    /*
      クライアントからの接続待ち。
     @return 接続されたたセッションを返す。*/
    public void init() throws IOException {
        for (int user = 1; user <= 2; user++) {
            System.out.println("userの接続を待っています");
            StreamConnection channel = server.acceptAndOpen();//クライアントがくるまでここで待機
            Session session = new Session(channel);
            sessions.add(session);
            System.out.println(session.getRemoteDeviceName() + "の接続が完了しました");
        }
    }

    /*
     接続したクライアントとの間にIn,Outのストリームを構築
     */
    class Session implements Runnable {
        private StreamConnection channel = null;
        private InputStream btIn = null;
        private OutputStream btOut = null;
        private RemoteDevice rd = null;

        public Session(StreamConnection channel) throws IOException {
            this.channel = channel;
            this.btIn = channel.openInputStream();
            this.btOut = channel.openOutputStream();
            rd = RemoteDevice.getRemoteDevice(channel);
        }

        public String getRemoteDeviceName() {
            String name = null;
            try {
                name = rd.getFriendlyName(false);
                
            } catch (IOException e) {
                e.printStackTrace();
                //TODO: handle exception
            }
            return name;
        }

        /**
         * - 受信したデータ処理を実行するスレッド
         */
        public void run() {
            try {
                byte[] buff = new byte[2048];
                int n = 0;
                String[] element;
                while (true) {
                    n = btIn.read(buff);
                    /*
                     **受信データの分割セクション
                     */

                    if(n != 0) {
                        String data = new String(buff, 0, n);
                        //1レコード分ごとに分割
                        String[] samples = data.split("\r\n", 0);
                        //各レコードを要素ごとに分割
                        for (int i = 0; i < samples.length; i++) {
                            System.err.println(samples[i]);
                                        element = samples[i].split(",", 0);
                            long t = Long.parseLong(element[0]);
                            float p = Float.parseFloat(element[1]);
                            int evt = Integer.parseInt(element[2]);
                            //int user_number = Integer.parseInt(name);
                            System.err.println(getRemoteDeviceName() + ": (time=" + t + ", position=" + p + ",attacktiming_switch=" + evt + ")");
			      /*各ユーザのandroidから送信されたデータの格納処理
			      user_numberの値に応じて各ユーザごとに分けてデータを処理すること
			      は可能なのですが将来的にもっと多人数を想定する場合もう少し効率的な書き方をする必要が
			      あるかもしれません*/
                            // test -- only P01T_6 can set target
                            if (getRemoteDeviceName().equals("P01T_6")) {
                                tm.setTarget(0, tm.height() * (1.0 - p));
                                if (evt != TargetMover.NO_EVENT) {
                                    tm.sendEvent(evt);
                                }                                    
                            }
                        }
                    }
                    try{
                        Thread.sleep(50);
                    } catch (InterruptedException e){
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                close();
            }
        }

        //接続が切れた場合ストリームを閉じる
        public void close() {
            System.out.println("Session Close");
            if (btIn    != null)
                try {
                    btIn.close();
            } catch (Exception e) {}
            if (btOut   != null)
                try {
                    btOut.close();
                } catch (Exception e) {}
            if (channel != null)
                try {
                    channel.close();
                } catch (Exception e) {}
        }
    }

}
