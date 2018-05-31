package android.esptest.net.fifotestchart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.numerics.labelProviders.DateLabelProvider;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.HorizontalLineAnnotation;
import com.scichart.charting.visuals.annotations.LabelPlacement;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.AxisAlignment;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.data.model.DoubleRange;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    /*    //WebSocket Clientを使用　https://github.com/TooTallNate/Java-WebSocket
        //https://stackoverflow.com/questions/34131718/what-is-a-simple-way-to-implement-a-websocket-client-in-android-is-the-followin

        gradleディレクトリにあるbuild.gradleのrepositoriesにmavenCentral()を追加。
    allprojects {
        repositories {
            google()
            jcenter()
            mavenCentral()　←これ
        }
    }
        プロジェクトのbuild.gradleのdependencyに compile "org.java-websocket:Java-WebSocket:1.3.8" を追加。

    dependencies {
        implementation fileTree(dir: 'libs', include: ['*.jar'])
        implementation 'com.android.support:appcompat-v7:26.1.0'
        implementation 'com.android.support.constraint:constraint-layout:1.0.2'
        testImplementation 'junit:junit:4.12'
        androidTestImplementation 'com.android.support.test:runner:1.0.1'
        androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
        compile "org.java-websocket:Java-WebSocket:1.3.8"　←これ
    }
        追加した後、sync gradleするとwebsocketライブラリがプロジェクトにインストールされます。
         */
    private WebSocketClient mWebSocketClient;
    TextView tview;
    Button btnstart,btnstop;

    int fifoCapacity = 1500;
    SciChartBuilder sciChartBuilder;
    XyDataSeries lineData[];
    IAxis xAxis, yAxis;
    IRenderableSeries lineSeries[];



    boolean isRunning; //20180516
    private final DoubleRange yVisibleRange = new DoubleRange((double)0, (double)1200); //20180523

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tview = findViewById(R.id.text_status);
        btnstart = findViewById(R.id.btn_start);
        btnstop = findViewById(R.id.btn_stop);
        btnstop.setEnabled(false);

        //https://www.scichart.com/documentation/android/v2.x/Tutorial%2001%20-%20Adding%20SciChart%20libraries%20as%20dependencies.html
        //https://www.scichart.com/documentation/android/v2.x/Creating%20your%20First%20SciChart%20Android%20App%20-%20Part%201.html

        try {
            com.scichart.charting.visuals.SciChartSurface.setRuntimeLicenseKey(
                    "<LicenseContract>" +
                            "<Customer>FAIN-Biomedical</Customer>" +
                            "<OrderId>ABT180222-9261-52115</OrderId>" +
                            "<LicenseCount>1</LicenseCount>" +
                            "<IsTrialLicense>false</IsTrialLicense>" +
                            "<SupportExpires>02/22/2019 00:00:00</SupportExpires>" +
                            "<ProductCode>SC-IOS-ANDROID-2D-ENTERPRISE-SRC</ProductCode>" +
                            "<KeyCode>a801b98774bd4cb1aea9759cad3cbddf73e1cbecf1e19f63866f2a08911b2e468f310d1b0cff3ceb78bd551b2294ac3a5bd7fb725ea58cac267e49eab23822f7b6a70af7afaeb7e5275117cb6dcb75dbd4c20cb09fd93eea0a3001013a4c62dcae12714ebcddaf86a00ea71a14d9973d1c88f74b44fe93fa23862f3655450f973a907e57f8c06250b0a0008baba6fb46b2a8b83acaf49def725bb637ac4d969bd0a02abc3bf73f7357fc1579912eed85a754933cddec75e65a7331</KeyCode>" +
                            "</LicenseContract>"
            );
        }catch(Exception e){}

        final SciChartSurface chartSurface = new SciChartSurface(this);

        // Get a layout declared in "activity_main.xml" by id
        LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chart_layout);

        // Add the SciChartSurface to the layout
        chartLayout.addView(chartSurface);

        // Initialize the SciChartBuilder
        SciChartBuilder.init(this);

        // Obtain the SciChartBuilder instance
        sciChartBuilder = SciChartBuilder.instance();

        // Create a numeric X axis　X軸の設定。タイトル、ラベル、数字のタイプ等を設定する。
        xAxis = sciChartBuilder.newDateAxis() //.newNumericAxis() //newCategoryDateAxis() //newDateAxis()
                .withAxisTitle("X Axis Title")
                .withAutoRangeMode(AutoRange.Always)
                .withLabelProvider(new CustomLabelProvider()) //newCategoryDateAxisだと秒数が出ないので自分で描画する
                .build();

        // Create a numeric Y axis Y軸の設定。
        yAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle("Y Axis Title")
                .withVisibleRange(yVisibleRange) //20180523
                .withAutoRangeMode(AutoRange.Never)
                .build();


        // Create interactivity modifiers
        ModifierGroup chartModifiers = sciChartBuilder.newModifierGroup()
                .withPinchZoomModifier().withReceiveHandledEvents(true).build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .build();

        //https://www.scichart.com/documentation/android/v2.x/Tutorial%2006%20-%20Adding%20Realtime%20Updates.html
        //FIFOで250件のデータを保持する様設定する。
        lineData = new XyDataSeries[9];
        lineData[0] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[1] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[2] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[3] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[4] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[5] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[6] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[7] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        lineData[8] = sciChartBuilder.newXyDataSeries(Date.class, Double.class)
                .withFifoCapacity(fifoCapacity)
                .build();
        //適当にデータを先埋め。
        /*for (int i = 0; i < fifoCapacity; i++)
        {
            long xValue = new Date().getTime() - 5000 + i*10;
            lineData[0].append(new Date(xValue), Math.sin(i * 0.1));
            lineData[1].append(new Date(xValue), Math.cos(i * 0.1));
            lineData[2].append(new Date(xValue), Math.cos(i * 0.1));
        }*/

        //100ms毎にデータを追加する。FIFOなので常に250件分が表示される。
        /*TimerTask updateDataTask = new TimerTask() {
            private int x = 0;
            @Override
            public void run() {
                UpdateSuspender.using(chartSurface, new Runnable() {
                    @Override
                    public void run() {
                        long xValue = new Date().getTime();
                        lineData[0].append(new Date(xValue), Math.sin(x * 0.1));
                        lineData[1].append(new Date(xValue), Math.cos(x * 0.1));
                        // Zoom series to fit the viewport
                        //chartSurface.zoomExtents();
                        ++x;
                    }
                });
            }
        };

        Timer timer = new Timer();
        long delay = 0;
        long interval = 100;
        timer.schedule(updateDataTask, delay, interval);*/

        //ラインの種類、色、線の太さ等を設定
        lineSeries = new IRenderableSeries[9];
        lineSeries[0] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[0])
                .withStrokeStyle(ColorUtil.LightBlue, 2f, true)
                .build();
        lineSeries[1] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[1])
                .withStrokeStyle(ColorUtil.Green, 2f, true)
                .build();
        lineSeries[2] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[2])
                .withStrokeStyle(ColorUtil.White, 2f, true)
                .build();
        lineSeries[3] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[3])
                .withStrokeStyle(ColorUtil.Brown, 2f, true)
                .build();
        lineSeries[4] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[4])
                .withStrokeStyle(ColorUtil.Grey, 2f, true)
                .build();
        lineSeries[5] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[5])
                .withStrokeStyle(ColorUtil.Lime, 2f, true)
                .build();
        lineSeries[6] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[6])
                .withStrokeStyle(ColorUtil.Blue, 2f, true)
                .build();
        lineSeries[7] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[7])
                .withStrokeStyle(ColorUtil.Yellow, 2f, true)
                .build();
        lineSeries[8] = sciChartBuilder.newLineSeries()
                .withDataSeries(lineData[8])
                .withStrokeStyle(ColorUtil.Purple, 2f, true)
                .build();

        // Add a RenderableSeries onto the SciChartSurface
        //ラインをチャートに追加
        chartSurface.getRenderableSeries().add(lineSeries[0]);
        chartSurface.getRenderableSeries().add(lineSeries[1]);
        chartSurface.getRenderableSeries().add(lineSeries[2]);
        chartSurface.getRenderableSeries().add(lineSeries[3]);
        chartSurface.getRenderableSeries().add(lineSeries[4]);
        chartSurface.getRenderableSeries().add(lineSeries[5]);
        chartSurface.getRenderableSeries().add(lineSeries[6]);
        chartSurface.getRenderableSeries().add(lineSeries[7]);
        chartSurface.getRenderableSeries().add(lineSeries[8]);

        //https://www.scichart.com/questions/question/no-seconds-displayed-on-datetime-axis
        //https://www.scichart.com/documentation/v5.x/Axis%20Labels%20-%20LabelProvider%20API.html

        //横軸の線を追加。オレンジ色の線を0.5の所に表示してみている。
        //HorizontalLineAnnotation horizontalLineAnnotation = sciChartBuilder.newHorizontalLineAnnotation()
        //        .withYValue(512)
        //        .withStroke(2, ColorUtil.Orange)
        //        .withHorizontalGravity(Gravity.FILL_HORIZONTAL)
        //        .withAnnotationLabel(LabelPlacement.Axis)
        //        .build();

        // Add the Y axis to the YAxes collection of the surface
        Collections.addAll(chartSurface.getYAxes(), yAxis);

        // Add the X axis to the XAxes collection of the surface
        Collections.addAll(chartSurface.getXAxes(), xAxis);

        // Add the interactions to the ChartModifiers collection of the surface
        Collections.addAll(chartSurface.getChartModifiers(), chartModifiers);

       // Collections.addAll(chartSurface.getAnnotations(), horizontalLineAnnotation);

        //chartSurface.zoomExtents();

        //サンプルにあったテキストを追加する例
/*        // Create a TextAnnotation and specify the inscription and position for it
        TextAnnotation textAnnotation = sciChartBuilder.newTextAnnotation()
                .withX1(5.0)
                .withY1(55.0)
                .withText("Hello World!")
                .withHorizontalAnchorPoint(HorizontalAnchorPoint.Center)
                .withVerticalAnchorPoint(VerticalAnchorPoint.Center)
                .withFontStyle(20, ColorUtil.White)
                .build();

        // Add the annotation to the Annotations collection of the surface
        Collections.addAll(surface.getAnnotations(), textAnnotation);*/
    }

//https://www.scichart.com/documentation/v5.x/Axis%20Labels%20-%20LabelProvider%20API.html
//https://www.scichart.com/documentation/android/v2.x/Axis%20Labels%20-%20LabelProvider%20API.html

    // To create a LabelProvider for a NumericAxis or Log Axis, inherit NumericLabelProvider
// ..  for a DateTimeAxis, inherit DateTimeLabelProvider
// ..  for a TimeSpanAxis, inherit TimeSpanLabelProvider
// ..  for a CategoryDateTimeAxis, inherit TradeChartAxisLabelProvider
    public class CustomLabelProvider extends DateLabelProvider
    {
        public CustomLabelProvider() {
            super();
        }

        @Override
        public String formatLabel(Comparable dataValue) {
            Date labeldate = new Date((long)(double)dataValue);
            String str = new SimpleDateFormat("hh:mm:ss").format(labeldate);
            return str;
        }

        @Override
        public String formatCursorLabel(Comparable dataValue) {
            // return a formatting string for modifiers' axis labels
            return formatLabel(dataValue);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

        @Override
    protected void onPause() {
        super.onPause();
        stopwebsocket();
    }

    public void onClickStart(View view){
        startwebsocket();
    }

    public void onClickStop(View view){
        stopwebsocket();
        yVisibleRange.setMinMax((double)0,(double)100); //20180523
    }

    void startwebsocket(){
        switchbuttons(false);
        isRunning = true; //20180516
        if (mWebSocketClient == null) {
            tview.setText("Connecting...");
            connectWebSocket();
        }else if (mWebSocketClient.isClosed()) {
            mWebSocketClient.reconnect();
        }
    }

    void stopwebsocket(){
        isRunning = false; //20180516
        if (mWebSocketClient != null) {
            if (!mWebSocketClient.isClosed()) {
                mWebSocketClient.close();
            }
        }
    }

    public void switchbuttons(boolean enable){
        btnstart.setEnabled(enable);
        btnstop.setEnabled(!enable);
    }

    public void sendTime(){
        long currenttime = System.currentTimeMillis() / 1000; //msを秒に変更
        ws_command = "T" + currenttime;
        if (mWebSocketClient.isOpen())
            mWebSocketClient.send(ws_command);
    }

    String ws_command;
    public void onClickGetminmax(View view){
        ws_command = "L";
        if (mWebSocketClient.isOpen())
            mWebSocketClient.send(ws_command);
    }

    public void onClickResetminmax(View view){
        ws_command = "R";
        if (mWebSocketClient.isOpen())
            mWebSocketClient.send(ws_command);
    }

    public void onClickSettime(View view){
        long currenttime = System.currentTimeMillis() / 1000; //msを秒に変更
        ws_command = "T" + currenttime;
        if (mWebSocketClient.isOpen())
            mWebSocketClient.send(ws_command);
    }

    //Aのリクエストを遅延させる場合これを使う。
    final Handler handler = new Handler();
    final Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                mWebSocketClient.send("A");
            }catch (WebsocketNotConnectedException e){}
        }
    };

    //20180516 切断された場合リトライする
    final Handler retryhandler = new Handler();
    final Runnable runretry = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                tview.setText("Reconnect...");
                startwebsocket();
            }
        }
    };

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.1.1:81/"); //IPアドレスとポート設定。ポートはArduino側のポートと合わせる。
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) { //Websocket通信で接続、切断、受信イベント時関数が呼ばれる。
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tview.setText("Connected");
                    }});
                sendTime(); //時計情報を送信
                //mWebSocketClient.send("A"); //接続したので「A」を送って最初のデータをリクエストする。
                handler.postDelayed(r,10); //少し遅らせてAを送る
            }

            @Override
            public void onMessage(String s) { //メッセージ受信
                final String message = s;
                if (s.startsWith("A")) {

                    //A1524753170327,0,1024,1,512,2,512,
                    //データは A[UNIX TIME], ID, 値, ID, 値, ID, 値・・・の形式で渡されてくる
                    //ID, 値は必ずしも連番とは限らない（1,234,3,456,5,432 の様に抜ける事もある。子ESP8266からデータが来ていない、通信不安定などの場合）


                    //カンマで分割してanalogdataに配列として入れる。
                    String[] analogdata = message.split(",", 0); //改行コードで文字列を分割する。配列0がA0の値、1が経過時間


                    //配列が3つ以上ある場合（時間データと一つ以上のID,値のデータ）グラフに反映。
                    if (analogdata != null && analogdata.length >= 3) {

                        //時間データを文字列から数値に変換
                        long timedata = Long.parseLong(analogdata[0].substring(1));

                        //データを調べてグラフに入れていく
                        int espid;
                        boolean filled[] = new boolean[9];
                        for (int i=3;i<=analogdata.length;i+=2){
                            espid = Integer.parseInt(analogdata[i-2]); //IDを数字に変換
                            if (espid >= 0 && espid < 9){ //espidの範囲確認。
                                //グラフデータに追加
                                lineData[espid].append(new Date(timedata), (double)Integer.parseInt(analogdata[i-1]));
                                filled[espid] = true;
                            }
                        }
                        //グラフデータが無かったものは適当なデータで埋めておく
                        for (int i=0;i<9;i++){
                            if (!filled[i])
                                lineData[i].append(new Date(timedata), (double)0); //0でも良いし、最後に取得したデータでも良い
                        }

                    }
                    //mWebSocketClient.send("A"); //次の「A」を送る。途切れた場合の為にタイマー処理で一定時間応答がない場合Aを再送した方がいいかも。
                    handler.postDelayed(r,10); //少し遅らせてAを送る

                }

                else if (s.startsWith("L")) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //最小最大データ受信
                        }});
                }
            }


            @Override
            public void onClose(int i, String s, boolean b) { //通信切断
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tview.setText("Disconnected");
                        switchbuttons(true);
                        retryhandler.postDelayed(runretry,2000); //20180516 2秒後に再接続する
                    }});
            }

            @Override
            public void onError(Exception e) { //エラー発生。再接続するなら一定時間経過後に再接続の処理を入れる。
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tview.setText("Connection Error");
                        switchbuttons(true);
                    }});
            }
        };
        mWebSocketClient.connect();
    }

}
