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
 * �Q�l�ɂ����R�[�hhttp://sham-memo.blogspot.com/2010/08/android_16.html
 */
public class MyCalendar2 extends Activity implements OnClickListener{
    /** �u�����N�����萔�B */
    private static final String BLANK = "";

    /** ��ʕ��B */
    private int width;
    /** �J�����_�[���{�{�^�����̃��C�A�E�g�ϐ� */
    private LinearLayout calendarFrame;
    /** �J�����_�[���̃��C�A�E�g�ϐ� */
    private TableLayout calendarTable;
    /** �J�����_�[�ɕ\������T�̍s�� */
    private final int nWeek = 6;
    /** ���t�r���[�̏� */
    private TextView[][] tvDay = new TextView[nWeek][7];
    /** ���t�r���[�̍����B */
    private static int CELL_HEIGHT = 40;
    /** ���t�r���[�̃t�H���g�T�C�Y�B */
    private static float CELL_FONT_SIZE = 18f;
    /** ���t�r���[�̃f�t�H���g�X�^�C���B */
    private final int DEFAULT_DAY_COLOR = Color.BLACK;
    /** ���t�r���[�̑I�����X�^�C���B */
    private final int SELECTED_DAY_COLOR = Color.YELLOW;
    /** �J�����_�[���t���i���̔N���̃J�����_�[��\���j�B */
    private Calendar drawDay;
    /** �J�����_�[�I�����̃C�x���g��`�B */
    private CalendarClickListener calendarClickListener = new CalendarClickListener();
    /** �J�����_�[�t���b�N���̃C�x���g��`�B */
    private CalendarFlickListenerImpl calendarFlickListener = new CalendarFlickListenerImpl();
    /** �����̃J�����_�[��\������X���C�h�A�j���[�V�����B */
    private TranslateAnimation slideNextAnimation;
    /** �O���̃J�����_�[��\������X���C�h�A�j���[�V�����B */
    private TranslateAnimation slidePreviousAnimation;
    /** �X���C�h�A�j���[�V�������x�B */
    private static final int DURATION_SLIDE = 600;

    /** �^���v�Z�{�^�� */
    private Button btnCalc;
    /** �����ݒ�p�{�^�� */
    private Button btnPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        // �p�����[�^�̏�����
        initParam();
        // �C�x���g�̏�����
        initEvents();
        // �J�����_�[�w�b�_���̕`��
        drawHeader();
        // �J�����_�[�����̕`��
        drawBody();
    }

    /**
     * �ĕ`�揈���B
     */
    private void redraw() {
        // �J�����_�[�w�b�_���̍ĕ`��
        drawHeader();
        // �J�����_�[�����̍ĕ`��
        drawBody();
    }

    /**
     * �p�����[�^�̏����������B
     */
    private void initParam() {
    	calendarFrame = (LinearLayout)findViewById(R.id.calendar_frame);
    	calendarTable = (TableLayout)findViewById(R.id.calendar_table);
    	btnCalc  = (Button)findViewById(R.id.btnCalc);
    	btnCalc.setOnClickListener(this);
    	btnPrice = (Button)findViewById(R.id.btnPrice);
    	btnPrice.setOnClickListener(this);
    	
    	//�����̓��t���擾
    	drawDay = Calendar.getInstance();

        // �[���̉𑜓x���擾
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();
        
        //calendarTable�̘g�����B�T�T�~�V��
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
     * �C�x���g�n���h���̏����ݒ�B
     */
    private void initEvents() {
        // �t���b�J�[�C�x���g�̐ݒ�
    	calendarTable.setOnTouchListener(calendarFlickListener);
        // �A�j���[�V�����p�C���X�^���X�𐶐��B
        slideNextAnimation = new TranslateAnimation(width, 0, 0, 0);
        slideNextAnimation.setDuration(DURATION_SLIDE);
        slideNextAnimation.setAnimationListener(calendarFlickListener);
        slidePreviousAnimation = new TranslateAnimation(-width, 0, 0, 0);
        slidePreviousAnimation.setDuration(DURATION_SLIDE);
        slidePreviousAnimation.setAnimationListener(calendarFlickListener);
    }

    /**
     * �J�����_�[�����̕`�揈���B
     */
    private void drawBody() {
    	//���̌��̍ŏ��̗j���ƍŌ�̓������߂�
        Calendar firstDay = (Calendar)this.drawDay.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int startDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int endDay         = firstDay.getActualMaximum(GregorianCalendar.DATE);

        // ���t�ƐF�̕\��
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
     * �J�����_�[�w�b�_�̕`�揈���B
     */
    private void drawHeader() {
        // �N��
        Integer year  = this.drawDay.get(Calendar.YEAR);
        Integer month = this.drawDay.get(Calendar.MONTH) + 1;
        TextView calendarDateText = (TextView)findViewById(R.id.calendar_date);
        calendarDateText.setText(year + "/" + month);
    }

    /**
     * �J�����_�[���t�r���[���^�b�`�����ۂ̃C�x���g��`�N���X�B
     */
    private class CalendarClickListener implements OnClickListener {

        @Override
        //�N���b�N������g�p���A�s�g�p���̕\����؂�ւ���
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
     * �J�����_�[�t���b�N���̃C�x���g��`�N���X�B
     * �X���C�h�A�j���[�V�����J�n�E�I�����̃C�x���g����`
     */
    private class CalendarFlickListenerImpl implements OnTouchListener, AnimationListener {
        /** �J�����_�[�^�b�`����x���W�B */
        private float touchDownX;
        /** �X���C�h�����t���O�B */
        private boolean isSlided;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
//          Log.d("AA",""+v+","+action+" x="+event.getX());
            switch(action) {
            case MotionEvent.ACTION_DOWN:
                // �X���C�h�����t���O�����Z�b�g����Touch Down����x���W��ێ�
                isSlided = false;
                touchDownX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                // �X���C�h���������Ă���ꍇ�̓��Z�b�g�����܂ŏ������Ȃ�
                if(!isSlided) {
                    // ���݂�x���W���擾
                    float x = event.getX();
                    // 20px���̃X���C�h����̏ꍇ�̂ݏ����ΏۂƂ���
                    if(touchDownX - x > 20) {
                        // �����̃J�����_�[��\������
                    	drawDay.add(Calendar.MONTH, +1);
                        redraw();
                        calendarTable.startAnimation(slideNextAnimation);
                    } else if(x - touchDownX > 20) {
                        // �O���̃J�����_�[��\������
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
            // �X���C�h���n�܂�����J�����_�[��񊈐�������
            calendarFrame.setEnabled(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            // �X���C�h������������J�����_�[������������
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
     * ������̃u�����N����B
     * @param str �Ώە�����
     * @return �u�����N�̏ꍇtrue
     */
    public static boolean isEmpty(String str) {
        return str == null || STR_BLANK.equals(str);
    }

    /**
     * ���������肵�܂��B
     *
     * @param year �N
     * @param month ��
     * @param day ��
     * @return ���茋�ʁi�����̏ꍇtrue�j
     */
    public static boolean isToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH) + 1;
        int todayDay = today.get(Calendar.DATE);
        return todayYear == year && todayMonth == month && todayDay == day;
    }

    /**
     * �_�C�A���O�\�����܂��B
     *
     * @param activity �A�N�e�B�r�e�B
     * @param msg ���b�Z�[�W
     */
    public static void alert(Activity activity, String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        toast.show();
        SCJUtil.toast = toast;
    }

    /**
     * �u�������ł�...�v���b�Z�[�W���_�C�A���O�\�����܂��B
     * @param activity
     */
    public static void alertTransition(Activity activity) {
        alert(activity, activity.getString(R.string.now_proc));
    }

    /**
     * ���b�Z�[�W�������I�ɔ�\���ɂ��܂��B
     */
    public static void closeMsg() {
        if(SCJUtil.toast != null) {
            SCJUtil.toast.cancel();
            SCJUtil.toast = null;
        }
    }

    /**
     * �m�F�_�C�A���O���쐬���܂��B
     * @param activity �A�N�e�B�r�e�B
     * @param title �^�C�g��
     * @param msg ���b�Z�[�W
     * @param okClickListenerImpl �uOK�v�{�^���������̃C�x���g�n���h��
     * @param cancelClickListenerImpl �u�L�����Z���v�{�^���������̃C�x���g�n���h��
     * @return �m�F�_�C�A���O
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
