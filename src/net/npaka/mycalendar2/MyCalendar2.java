package net.npaka.mycalendar2;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/*
 * 参考にしたコードhttp://sham-memo.blogspot.com/2010/08/android_16.html
 */
public class MyCalendar2 extends Activity implements OnClickListener{
    /** ブランク文字定数。 */
    private static final String BLANK = "";

    /** 画面幅。 */
    private int width;
    /** カレンダー部＋ボタン部のレイアウト変数 */
    private LinearLayout calendarFrame;
    /** カレンダー部のレイアウト変数 */
    private TableLayout calendarTable;
    /** カレンダーに表示する週の行数 */
    private final int nWeek = 6;
    /** 日付ビューの升 */
    private TextView[][] tvDay = new TextView[nWeek][7];
    /** 日付ビューの高さ。 */
    private static int CELL_HEIGHT = 40;
    /** 日付ビューのフォントサイズ。 */
    private static float CELL_FONT_SIZE = 18f;
    /** 日付ビューのデフォルトスタイル。 */
    private final int DEFAULT_DAY_COLOR = Color.BLACK;
    /** 日付ビューの選択時スタイル。 */
    private final int SELECTED_DAY_COLOR = Color.YELLOW;
    /** カレンダー日付情報（この年月のカレンダーを表示）。 */
    private Calendar drawDay;
    /** カレンダー選択時のイベント定義。 */
    private CalendarClickListener calendarClickListener = new CalendarClickListener();
    /** カレンダーフリック時のイベント定義。 */
    private CalendarFlickListenerImpl calendarFlickListener = new CalendarFlickListenerImpl();
    /** 翌月のカレンダーを表示するスライドアニメーション。 */
    private TranslateAnimation slideNextAnimation;
    /** 前月のカレンダーを表示するスライドアニメーション。 */
    private TranslateAnimation slidePreviousAnimation;
    /** スライドアニメーション速度。 */
    private static final int DURATION_SLIDE = 600;

    /** 運賃計算ボタン */
    private Button btnCalc;
    /** 料金設定用ボタン */
    private Button btnPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        // パラメータの初期化
        initParam();
        // イベントの初期化
        initEvents();
        // カレンダーヘッダ部の描画
        drawHeader();
        // カレンダー内部の描画
        drawBody();
    }

    /**
     * 再描画処理。
     */
    private void redraw() {
        // カレンダーヘッダ部の再描画
        drawHeader();
        // カレンダー内部の再描画
        drawBody();
    }

    /**
     * パラメータの初期化処理。
     */
    private void initParam() {
    	calendarFrame = (LinearLayout)findViewById(R.id.calendar_frame);
    	calendarTable = (TableLayout)findViewById(R.id.calendar_table);
    	btnCalc  = (Button)findViewById(R.id.btnCalc);
    	btnCalc.setOnClickListener(this);
    	btnPrice = (Button)findViewById(R.id.btnPrice);
    	btnPrice.setOnClickListener(this);
    	
    	//今日の日付を取得
    	drawDay = Calendar.getInstance();

        // 端末の解像度を取得
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();
        
        //calendarTableの枠を作る。５週×７日
        for(int i=0; i < nWeek; i++){
        	TableRow row = new TableRow(this);
            calendarTable.addView(row);
        	for(int j=0; j<7; j++){
	            tvDay[i][j] = new TextView(this);
	            row.addView(tvDay[i][j]);
	            tvDay[i][j].setText(BLANK);
	            tvDay[i][j].setTextSize(CELL_FONT_SIZE);
	            tvDay[i][j].setHeight(CELL_HEIGHT);
	            tvDay[i][j].setGravity(Gravity.CENTER);
	            tvDay[i][j].setBackgroundColor(DEFAULT_DAY_COLOR);
	            tvDay[i][j].setTag(DEFAULT_DAY_COLOR);
	            tvDay[i][j].setOnTouchListener(calendarFlickListener);
	            tvDay[i][j].setOnClickListener(calendarClickListener);
            }
        }
    }

    /**
     * イベントハンドラの初期設定。
     */
    private void initEvents() {
        // フリッカーイベントの設定
    	calendarTable.setOnTouchListener(calendarFlickListener);
        // アニメーション用インスタンスを生成。
        slideNextAnimation = new TranslateAnimation(width, 0, 0, 0);
        slideNextAnimation.setDuration(DURATION_SLIDE);
        slideNextAnimation.setAnimationListener(calendarFlickListener);
        slidePreviousAnimation = new TranslateAnimation(-width, 0, 0, 0);
        slidePreviousAnimation.setDuration(DURATION_SLIDE);
        slidePreviousAnimation.setAnimationListener(calendarFlickListener);
    }

    /**
     * カレンダー内部の描画処理。
     */
    private void drawBody() {
    	//その月の最初の曜日と最後の日を求める
        Calendar firstDay = (Calendar)this.drawDay.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int startDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int endDay         = firstDay.getActualMaximum(GregorianCalendar.DATE);

        // 日付と色の表示
        int d = 1 - startDayOfWeek;
        for(int i=0; i<nWeek; i++){
        	for(int j=0; j<7; j++){
        		d++;
        		if((0 < d) && (d <= endDay)){
        			tvDay[i][j].setText(String.valueOf(d));
        		}else{
        			tvDay[i][j].setText(BLANK);
        		}
        	}
        }
    }

    /**
     * カレンダーヘッダの描画処理。
     */
    private void drawHeader() {
        // 年月
        Integer year  = this.drawDay.get(Calendar.YEAR);
        Integer month = this.drawDay.get(Calendar.MONTH) + 1;
        TextView calendarDateText = (TextView)findViewById(R.id.calendar_date);
        calendarDateText.setText(year + "/" + month);
    }

    /**
     * カレンダー日付ビューをタッチした際のイベント定義クラス。
     */
    private class CalendarClickListener implements OnClickListener {

        @Override
        //クリックしたら使用日、不使用日の表示を切り替える
        public void onClick(View v) {
        	if(((TextView)v).getText() != BLANK){
	            if(v.getTag().equals(SELECTED_DAY_COLOR)){
	            	v.setBackgroundColor(DEFAULT_DAY_COLOR);
	            	v.setTag(DEFAULT_DAY_COLOR);
	            }else{
	            	v.setBackgroundColor(SELECTED_DAY_COLOR);
	            	v.setTag(SELECTED_DAY_COLOR);
	            }
        	}
        }
    }

    /**
     * カレンダーフリック時のイベント定義クラス。
     * スライドアニメーション開始・終了時のイベントも定義
     */
    private class CalendarFlickListenerImpl implements OnTouchListener, AnimationListener {
        /** カレンダータッチ時のx座標。 */
        private float touchDownX;
        /** スライド完了フラグ。 */
        private boolean isSlided;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
//          Log.d("AA",""+v+","+action+" x="+event.getX());
            switch(action) {
            case MotionEvent.ACTION_DOWN:
                // スライド完了フラグをリセットしてTouch Down時のx座標を保持
                isSlided = false;
                touchDownX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                // スライドが完了している場合はリセットされるまで処理しない
                if(!isSlided) {
                    // 現在のx座標を取得
                    float x = event.getX();
                    // 20px幅のスライド操作の場合のみ処理対象とする
                    if(touchDownX - x > 20) {
                        // 翌月のカレンダーを表示する
                    	drawDay.add(Calendar.MONTH, +1);
                        redraw();
                        calendarTable.startAnimation(slideNextAnimation);
                    } else if(x - touchDownX > 20) {
                        // 前月のカレンダーを表示する
                    	drawDay.add(Calendar.MONTH, -1);
                        redraw();
                        calendarTable.startAnimation(slidePreviousAnimation);
                    }
                }
                break;
            default:
            	break;
            }
            return false;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // スライドが始まったらカレンダーを非活性化する
            calendarFrame.setEnabled(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // スライドが完了したらカレンダーを活性化する
            isSlided = true;
            calendarFrame.setEnabled(true);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

    }

	@Override
	public void onClick(View v) {
		if(v == btnCalc) onClickCalc(v);
		if(v == btnPrice) onClickPrice(v);
	}
    
    private void onClickCalc(View v){
    	SCJUtil.alert(this, "onClickCalc "+v); 
    	calendarTable.setVisibility(View.VISIBLE);
    }
    
    private void onClickPrice(View v){
    	SCJUtil.alert(this, "onClickPrice "+v);  
//    	calendarTable.setVisibility(View.INVISIBLE);
        setContentView(R.layout.main);
    }
}
 

class SCJUtil {

    private final static String STR_BLANK = "";
    private static Toast toast = null;

    /**
     * 文字列のブランク判定。
     * @param str 対象文字列
     * @return ブランクの場合true
     */
    public static boolean isEmpty(String str) {
        return str == null || STR_BLANK.equals(str);
    }

    /**
     * 当日か判定します。
     *
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 判定結果（当日の場合true）
     */
    public static boolean isToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH) + 1;
        int todayDay = today.get(Calendar.DATE);
        return todayYear == year && todayMonth == month && todayDay == day;
    }

    /**
     * ダイアログ表示します。
     *
     * @param activity アクティビティ
     * @param msg メッセージ
     */
    public static void alert(Activity activity, String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        toast.show();
        SCJUtil.toast = toast;
    }

    /**
     * 「処理中です...」メッセージをダイアログ表示します。
     * @param activity
     */
    public static void alertTransition(Activity activity) {
        alert(activity, activity.getString(R.string.now_proc));
    }

    /**
     * メッセージを強制的に非表示にします。
     */
    public static void closeMsg() {
        if(SCJUtil.toast != null) {
            SCJUtil.toast.cancel();
            SCJUtil.toast = null;
        }
    }

    /**
     * 確認ダイアログを作成します。
     * @param activity アクティビティ
     * @param title タイトル
     * @param msg メッセージ
     * @param okClickListenerImpl 「OK」ボタン押下時のイベントハンドラ
     * @param cancelClickListenerImpl 「キャンセル」ボタン押下時のイベントハンドラ
     * @return 確認ダイアログ
     */
    public static AlertDialog createConfirmDialog(Activity activity, String title, String msg
    		, android.content.DialogInterface.OnClickListener okClickListenerImpl
            , android.content.DialogInterface.OnClickListener cancelClickListenerImpl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(activity.getString(R.string.ok),  okClickListenerImpl);
        builder.setNegativeButton(activity.getString(R.string.cancel), cancelClickListenerImpl);
        return builder.create();
    }

}
