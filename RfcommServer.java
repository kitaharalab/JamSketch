//package com.example.mizun.kitaharasystem;

/**
 * Created by korona on 2017/04/11.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
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

    private Session session = null;

    private TargetMover tm;
    
    static int user = 1;
    static int connected_user = 1;

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
    }

    public void setTarget(TargetMover tm) {
	   this.tm = tm;
    }
    
    public void start() {
	if (session == null) {
	    throw new IllegalStateException("init() must be called in advance");
	}
        String user_name = String.valueOf(user);
	Thread thread = new Thread(session, user_name);
        thread.start();
    }

    /*
      クライアントからの接続待ち。
     @return 接続されたたセッションを返す。*/
    public void init() throws IOException {
        	System.out.println("user"+user+"の接続を待っています");
        	StreamConnection channel = server.acceptAndOpen();//クライアントがくるまでここで待機
        	System.out.println("user"+user+"の接続が完了しました");
        	session = new Session(channel);
		user++;
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
		Thread thread = new Thread(String.valueOf(connected_user));
		connected_user++;
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
			    int user_number = Integer.parseInt(thread.getName());
			    System.err.println("user"+user_number+": (time=" + t + ", position=" + p + ",attacktiming_switch=" + evt + ")");
			      /*各ユーザのandroidから送信されたデータの格納処理
			      user_numberの値に応じて各ユーザごとに分けてデータを処理すること
			      は可能なのですが将来的にもっと多人数を想定する場合もう少し効率的な書き方をする必要が
			      あるかもしれません*/
			    if(user_number == 1){
			    	tm.setTarget(0, tm.height() * (1.0 - p));
				if (evt != TargetMover.NO_EVENT) {
					tm.sendEvent(evt);
			    	}
			    }else{
				/*2人目以降のユーザのTargetMoverに関する処理*/	
			    }
			    
                        }


                    }
                    try{
                        Thread.sleep(50);
                    }catch (InterruptedException e){
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

    

    //メインメソッド
    public static void main(String[] args) throws Exception {
	RfcommServer server = new RfcommServer();
	String user_name = "user";
        //while (n < 3){ //接続するユーザ数 Todo:接続するユーザ数を選択できるようにするといいかも(今は2人) 
            server.init();
	    server.start();
            user++;
        //}	
    }
}
