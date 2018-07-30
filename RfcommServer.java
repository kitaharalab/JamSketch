//package com.example.mizun.kitaharasystem;

/**
 * Created by korona on 2017/04/11.
 */

import java.awt.Color;
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
    static private StreamConnectionNotifier server = null;
    private TargetMover tm;
    private Session session = null;
    
    static private StreamConnectionNotifier getStreamConnectionNotifier() throws IOException {
        if (server == null) {
            server = (StreamConnectionNotifier) Connector.open(
                "btspp://localhost:" + serverUUID,
                Connector.READ_WRITE, true);
            ServiceRecord record = LocalDevice.getLocalDevice().getRecord(server);
            LocalDevice.getLocalDevice().updateRecord(record);
        }
        return server;
    }

    public void setTargetMover(TargetMover tm) {
	   this.tm = tm;
    }
    
    public void start() {
        if (session == null) {
            throw new IllegalStateException("init() must be called in advance");
        } else {
            new Thread(session).start();
        }
    }

    /*
      クライアントからの接続待ち。
     @return 接続されたたセッションを返す。*/
    public void init() throws IOException {
        StreamConnectionNotifier server = getStreamConnectionNotifier();
        System.out.println("clientの接続を待っています");
        StreamConnection channel = server.acceptAndOpen();//クライアントがくるまでここで待機
        session = new Session(channel);
        System.out.println("接続が完了しました");
    }

    /*
     接続したクライアントとの間にIn,Outのストリームを構築
     */
    class Session implements Runnable {
        private StreamConnection channel = null;
        private InputStream btIn = null;
        private OutputStream btOut = null;

        public Session(StreamConnection channel) throws IOException {
            this.channel = channel;
            this.btIn = channel.openInputStream();
            this.btOut = channel.openOutputStream();
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
                            System.err.println("(time=" + t + ", position=" + p + ", event=" + evt + ")");
			      /*各ユーザのandroidから送信されたデータの格納処理
			      user_numberの値に応じて各ユーザごとに分けてデータを処理すること
			      は可能なのですが将来的にもっと多人数を想定する場合もう少し効率的な書き方をする必要が
                  あるかもしれません*/
                            tm.setTargetXY(0, (tm.height() * (1.0 - p)));
                            if (evt != TargetMover.NO_EVENT) {
                                tm.sendEvent(evt);
                            }                                    
                        }
                    }
                    try{
                        Thread.sleep(50);
                    } catch (InterruptedException e){
                        e.printStackTrace();
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
            if (btIn != null)
                try {
                    btIn.close();
            } catch (Exception e) {}
            if (btOut != null)
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
