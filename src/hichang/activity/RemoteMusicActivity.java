package hichang.activity;

import hichang.Song.Singer;
import hichang.Song.Song;
import hichang.Song.LocalBitmap;
import hichang.ourView.MusicItemView;
import hichang.ourView.RankMusicItemView;
import hichang.ourView.SingerItemView;
import hichang.test.DswLog;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.flypigeon.service.MainService;
import com.android.flypigeon.util.Constant;
import com.android.flypigeon.util.Person;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteMusicActivity extends Activity {

	private MyBroadcastRecv broadcastRecv = null;
	private IntentFilter bFilter = null;
	private int personId;

	// private ImageView[] heatimage = new ImageView[10]; // �ȶ�ͼ�겼��
	// private ImageView[] number = new ImageView[10]; // ���
	// private ImageView[] singerimage = new ImageView[9];// ����ͷ��
	// private ImageView[] singerbox = new ImageView[9]; // ���Ǳ�����
	private ImageView[] arrow = new ImageView[4]; // �����ͷ
	private TextView[] songname = new TextView[10]; // �������б�
	private RankMusicItemView[] songItems = new RankMusicItemView[10]; // ���а���-������
	private MusicItemView[] musicItems = new MusicItemView[10]; // �������-������
	private SingerItemView[] singerItems = new SingerItemView[10]; // ���ǵ��-������
	private TextView[] musicstar = new TextView[9]; // �����б�
	private ImageView[] keyboardImageViews = new ImageView[12];
	private TextView leftText; // ��ָʾ���ı�
	private TextView rightText; // ��ָʾ���ı�
	private TextView titleText; // �����ı�
	private TextView pageText; // ҳ��
	private TextView search; // ����
	private LinearLayout searchframe; // �����򱳾�����
	// private ImageView keyBoard; //����
	private ImageView midSmallBack; // �м�С����
	private ImageView searchBack; // ���̱���
	private ImageView smallarrow; // С��ͷ
	private ImageView oKkey; // OK��

	private AbsoluteLayout layout0;
	private AbsoluteLayout layout;

	private Animation showImage; // ��ʾ���ݶ���
	private Handler handler;
	private Timer timer;

	private int[] idViewSong;
	private int[] idViewMusic;
	private int[] idViewSinger;
	private int[] idPicHeat;
	private int[] idPicNum;
	private int[] idPicPage;
	private int[] idPicArrow;
	private int[] idViewKeyboard = new int[12];
	private int[] idPicKeyboard = new int[12];
	private int[] idPicKeyboard_Yellow = new int[12];

	/*
	 * ��ʶ��ǰҳ��״̬ flag=1:���ǵ�� flag=2:���а��� flag=3:�������
	 */
	private int flag = 2;
	// ҳ��
	private int page = 1;
	// ���ҳ��
	private int maxPage;
	// ���һҳ����/������
	private int lastPageCount;
	// ��ǰҳ������������
	private int nowCount = 10;
	// �Ӹ��ǵ�������ת������������ʱ���ǽ����ҳ��
	private int prePage = 1;
	// �Ӹ��ǵ�������ת������������ʱ������������ı�����
	private String preText;
	// �Ƿ��һ�ν����ҳ��
	private boolean isFirstEnter = true;
	// ��ǰ��ҳ����ת�����Ĳ���
	private int afferentParam = 0;
	// �����Ĳ���
	private String outflowParam;
	// ����
	private int keyCode;
	/*
	 * OK���Ƿ��ѵ�� flag=1:isOKDown=falseʱ���ּ���������������������
	 * isOKDown=trueʱ��ʾ�������������ѡ�����״̬
	 * flag=2:isOKDown���⣬����ʱ����Ϊfalse���Ա�������ѡ���������ʱ������������
	 * flag=3:isOKDown=falseʱ���ּ��������������������� isOKDown=trueʱ��ʾ�������������ѡ�����״̬
	 */
	private boolean isOKDown = false;
	/*
	 * �Ƿ��ǴӸ��ǵ�������ת������������ ���ǣ��򷵻ؼ�����ת�ظ��ǵ����棬���Ҹ�����������ʾ�������а���ĳ���ֵĸ���
	 * ���򣬷��ؼ��������أ��Ҹ�����������ʾ�������и��������У�10��
	 */
	private boolean isFromSinger = false;
	// �Ƿ���������������
	private boolean isFromSong = false;
	private int songVisible = View.VISIBLE;
	private int musicVisible = View.INVISIBLE;
	private int singerVisible = View.INVISIBLE;
	private int keyBoardVisible=View.INVISIBLE;
	// ���ǵ������ȡ�ĸ�����
	private String cSingerName = "";
	// ������ʽʵ��
	private Typeface typeFace;

	private Song song;
	private Singer singer;
	// ���ݿ��и������и�������
	private int songcount;
	// ���ݿ��и��ֱ��и�������
	private int singercount;
	// �����嵥
	private List<Song> songList;
	// �����嵥,ֻ��Ҫ��������Ϣ
	private List<Singer> singerList;

	final static int MSG_KEY_ZERO = 201;
	final static int MSG_KEY_ONE = 202;
	final static int MSG_KEY_TWO = 203;
	final static int MSG_KEY_THREE = 204;
	final static int MSG_KEY_FOUR = 205;
	final static int MSG_KEY_FIVE = 206;
	final static int MSG_KEY_SIX = 207;
	final static int MSG_KEY_SEVEN = 208;
	final static int MSG_KEY_EIGHT = 209;
	final static int MSG_KEY_NINE = 210;
	final static int MSG_KEY_ZERO_YELLOW = 211;
	final static int MSG_KEY_ONE_YELLOW = 212;
	final static int MSG_KEY_TWO_YELLOW = 213;
	final static int MSG_KEY_THREE_YELLOW = 214;
	final static int MSG_KEY_FOUR_YELLOW = 215;
	final static int MSG_KEY_FIVE_YELLOW = 216;
	final static int MSG_KEY_SIX_YELLOW = 217;
	final static int MSG_KEY_SEVEN_YELLOW = 218;
	final static int MSG_KEY_EIGHT_YELLOW = 219;
	final static int MSG_KEY_NINE_YELLOW = 220;

	/** Called when the activity is first created. */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		regBroadcastRecv();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ȥ��Activity�����״̬��
		setContentView(R.layout.remotemusic);

		// ʹ��assets/fonts/Ŀ¼������
		typeFace = Typeface.createFromAsset(getAssets(), "fonts/fzzy.ttf");
		idViewSong = new int[] { R.id.song1, R.id.song2, R.id.song3, R.id.song4, R.id.song5,
				R.id.song6, R.id.song7, R.id.song8, R.id.song9, R.id.song0 };
		idViewMusic = new int[] { R.id.music1, R.id.music2, R.id.music3, R.id.music4, R.id.music5,
				R.id.music6, R.id.music7, R.id.music8, R.id.music9, R.id.music0 };
		idViewSinger = new int[] { R.id.singer1, R.id.singer2, R.id.singer3, R.id.singer4,
				R.id.singer5, R.id.singer6, R.id.singer7, R.id.singer8, R.id.singer9 };// ������
		idPicHeat = new int[] { R.drawable.heat3, R.drawable.heat2, R.drawable.heat1, R.drawable.heat0 };// �ȶȼ���
		idViewKeyboard = new int[] { R.id.remote_keyboard_num_0, R.id.remote_keyboard_num_1,
				R.id.remote_keyboard_num_2, R.id.remote_keyboard_num_3, R.id.remote_keyboard_num_4,
				R.id.remote_keyboard_num_5, R.id.remote_keyboard_num_6, R.id.remote_keyboard_num_7,
				R.id.remote_keyboard_num_8, R.id.remote_keyboard_num_9,
				R.id.remote_keyboard_num_pin, R.id.remote_keyboard_num_jiao };
		idPicKeyboard = new int[] { R.drawable.remote_zero1, R.drawable.remote_one1,
				R.drawable.remote_two1, R.drawable.remote_three1, R.drawable.remote_four1,
				R.drawable.remote_five1, R.drawable.remote_six1, R.drawable.remote_seven1,
				R.drawable.remote_eight1, R.drawable.remote_nine1, R.drawable.remote_pingxian1,
				R.drawable.remote_jiaoti1 };
		idPicNum = new int[] { R.drawable.button1, R.drawable.button2, R.drawable.button3,
				R.drawable.button4, R.drawable.button5, R.drawable.button6, R.drawable.button7,
				R.drawable.button8, R.drawable.button9, R.drawable.button0 };
		idPicKeyboard_Yellow = new int[] { R.drawable.remote_zero2, R.drawable.remote_one2,
				R.drawable.remote_two2, R.drawable.remote_three2, R.drawable.remote_four2,
				R.drawable.remote_five2, R.drawable.remote_six2, R.drawable.remote_seven2,
				R.drawable.remote_eight2, R.drawable.remote_nine2, R.drawable.remote_pingxian2,
				R.drawable.remote_jiaoti2 };
		idPicPage = new int[] { R.drawable.zero, R.drawable.one, R.drawable.two, R.drawable.three,
				R.drawable.four, R.drawable.five, R.drawable.six, R.drawable.seven,
				R.drawable.eight, R.drawable.nine };
		idPicArrow = new int[] { R.drawable.arrow_up, R.drawable.arrow_down, R.drawable.arrow_left,
				R.drawable.arrow_right, R.drawable.upwhite, R.drawable.downwhite,
				R.drawable.leftwhite, R.drawable.rightwhite };
		Log.d("onCreate", "1");
		layout0 = (AbsoluteLayout) findViewById(R.id.widget0);
		layout = (AbsoluteLayout) findViewById(R.id.layout);
		for (int i = 0; i < 10; i++) {
			songItems[i] = (RankMusicItemView) findViewById(idViewSong[i]);
			musicItems[i] = (MusicItemView) findViewById(idViewMusic[i]);
			if (i < 9) {
				singerItems[i] = (SingerItemView) findViewById(idViewSinger[i]);
			}
		}
		for (int i = 0; i < 12; i++) {
			keyboardImageViews[i] = (ImageView) findViewById(idViewKeyboard[i]);
			keyboardImageViews[i].setImageResource(idPicKeyboard[i]);
		}
		search = (TextView) findViewById(R.id.search);
		searchframe = (LinearLayout) findViewById(R.id.searchframe);
		midSmallBack = (ImageView) findViewById(R.id.midsmallframe);
		searchBack = (ImageView) findViewById(R.id.searchback);
		oKkey = (ImageView) findViewById(R.id.keyboard_ok);
		smallarrow = (ImageView) findViewById(R.id.smallarrow);
		arrow[0] = (ImageView) findViewById(R.id.arrow_up);
		arrow[1] = (ImageView) findViewById(R.id.arrow_down);
		arrow[2] = (ImageView) findViewById(R.id.arrow_left);
		arrow[3] = (ImageView) findViewById(R.id.arrow_right);
		leftText = (TextView) findViewById(R.id.lefttext);
		rightText = (TextView) findViewById(R.id.righttext);
		titleText = (TextView) findViewById(R.id.tilte);
		pageText = (TextView) findViewById(R.id.page);
		Log.d("onCreate", "2");
		// ��������
		leftText.setTypeface(typeFace);
		rightText.setTypeface(typeFace);
		titleText.setTypeface(typeFace);

		Intent intent = getIntent();
		afferentParam = intent.getIntExtra("type", -1);

		// ʵ����Song��Singer
		song = new Song(this.getBaseContext());
		singer = new Singer(this.getBaseContext());

		showImage = AnimationUtils.loadAnimation(this, R.anim.showimage);
		showImage.setFillAfter(true);
		Log.d("onCreate", "3");
		// ��ʼ������Ϣ
		setTitleText();
		setVisible();
		setListView(flag, 1);
		Log.d("onCreate", "4");
		timer = new Timer();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				final int msgwhat = msg.what;
				switch (msgwhat) {
				case 0:case 1:case 2:case 3:
					arrow[msgwhat].setBackgroundResource(idPicArrow[msgwhat]);
					break;
				case 4:case 5:case 6:case 7:
					arrow[msgwhat - 4].setBackgroundResource(idPicArrow[msgwhat]);
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(msgwhat - 4);
						}
					}, 150);
					break;
				case 8:
					keyboardImageViews[11].setImageResource(idPicKeyboard[11]);
					break;
				case MSG_KEY_ZERO:
				case MSG_KEY_ZERO + 1:
				case MSG_KEY_ZERO + 2:
				case MSG_KEY_ZERO + 3:
				case MSG_KEY_ZERO + 4:
				case MSG_KEY_ZERO + 5:
				case MSG_KEY_ZERO + 6:
				case MSG_KEY_ZERO + 7:
				case MSG_KEY_ZERO + 8:
				case MSG_KEY_ZERO + 9:
					keyboardImageViews[msgwhat - 201].setImageResource(idPicKeyboard[msgwhat - 201]);
					break;
				case MSG_KEY_ZERO_YELLOW:
				case MSG_KEY_ZERO_YELLOW + 1:
				case MSG_KEY_ZERO_YELLOW + 2:
				case MSG_KEY_ZERO_YELLOW + 3:
				case MSG_KEY_ZERO_YELLOW + 4:
				case MSG_KEY_ZERO_YELLOW + 5:
				case MSG_KEY_ZERO_YELLOW + 6:
				case MSG_KEY_ZERO_YELLOW + 7:
				case MSG_KEY_ZERO_YELLOW + 8:
				case MSG_KEY_ZERO_YELLOW + 9:
					keyboardImageViews[msgwhat - 211]
							.setImageResource(idPicKeyboard_Yellow[msgwhat - 211]);
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(msgwhat - 10);
						}
					}, 300);
					break;
				}
			}
		};
		Log.d("onCreate", "5");
		sendNextActivityMsg(Constant.REMOTESONG);

		arrow[0].setOnClickListener(onArrowClickListener);
		arrow[1].setOnClickListener(onArrowClickListener);
		arrow[2].setOnClickListener(onArrowClickListener);
		arrow[3].setOnClickListener(onArrowClickListener);
	}

	private OnClickListener onArrowClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.arrow_left:
				arrowLeftClick();
				break;
			case R.id.arrow_right:
				arrowRightClick();
				break;
			case R.id.arrow_up:
				arrowUpClick();
				break;
			case R.id.arrow_down:
				arrowDownClick();
				break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:arrowUpClick();break;
		case KeyEvent.KEYCODE_DPAD_DOWN:arrowDownClick();break;
		case KeyEvent.KEYCODE_DPAD_LEFT:arrowLeftClick();break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:arrowRightClick();break; 	
		case KeyEvent.KEYCODE_0:dealKeyDown(0);break;
		case KeyEvent.KEYCODE_1:dealKeyDown(1);break;
		case KeyEvent.KEYCODE_2:dealKeyDown(2);break;
		case KeyEvent.KEYCODE_3:dealKeyDown(3);break;
		case KeyEvent.KEYCODE_4:dealKeyDown(4);break;
		case KeyEvent.KEYCODE_5:dealKeyDown(5);break;
		case KeyEvent.KEYCODE_6:dealKeyDown(6);break;
		case KeyEvent.KEYCODE_7:dealKeyDown(7);break;
		case KeyEvent.KEYCODE_8:dealKeyDown(8);break;
		case KeyEvent.KEYCODE_9:dealKeyDown(9);break;
		case KeyEvent.KEYCODE_ENTER:  //OK��
			okClicked();
			break;
		case KeyEvent.KEYCODE_BACK:
			BackClicked();
			break;
		case 219: { //���水ť
			jiaotiClicked();
		}
		default:
			return false;
		}
		return true;
	}
	public void KeyNumClicked(View v){
		int id=v.getId();
		for(int i=0;i<10;i++){
			if(id==idViewKeyboard[i]){
				dealKeyDown(i);
			}
		}
	}
	public void ButtonClicked(View v) {
		switch (v.getId()) {
		case R.id.keyboard_ok:
			okClicked();
		case R.id.remote_keyboard_num_pin:
			pingxianClicked();
			break;
		case R.id.remote_keyboard_num_jiao:
			jiaotiClicked();
			break;
		}
	}
	//���а����������¼�����
	public void SongClicked(View v) {
		int id=v.getId();
		Log.d("SongClicked", "id="+id);
		for(int i=0;i<10;i++){
			if(id==idViewSong[i]){
				
				remoteByList(i);
			}
		}
	}
	//��������������¼�����
	public void MusicClicked(View v) {
		int id=v.getId();
		Log.d("MusicClicked", "id="+id);
		for(int i=0;i<10;i++){
			if(id==idViewMusic[i]){
				if (isOKDown) {
					remoteByList(i);
				}
			}
		}
	}
	//���ǵ����ǵ���¼�����
	public void SingerClicked(View v) {
		int id=v.getId();
		Log.d("SingerClicked", "id="+id);
		for(int i=0;i<9;i++){
			if(id==idViewSinger[i]){
				if (isOKDown) {
					remoteMusicBySinger(i);
				}
			}
		}
		
	}
	// �����ͷ����¼�����
	private void arrowLeftClick() {
		handler.sendEmptyMessage(6);
		isOKDown = false;
		oKkey.setImageResource(R.drawable.keyborad_okbutton2);
		smallarrow.setImageResource(R.drawable.right);
		searchBack.setImageResource(R.drawable.keyframe_bright);
		midSmallBack.setImageResource(R.drawable.midframe_dark);
		if (flag == 1) {
			flag = 3;
			search.setText("");
			search.setEnabled(true);
			isOKDown = false;
		} else if (flag == 3) {
			flag--;
			isFromSinger = false;
		} else {
			flag--;
			search.setText("");
			search.setEnabled(true);
			isOKDown = false;
		}
		page = 1;
		layout.startAnimation(showImage);
		setTitleText();
		setVisible();
		setListView(flag, page);
	}
	// ���Ҽ�ͷ����¼�����
	private void arrowRightClick() {
		handler.sendEmptyMessage(7);
		isOKDown = false;
		oKkey.setImageResource(R.drawable.keyborad_okbutton2);
		smallarrow.setImageResource(R.drawable.right);
		searchBack.setImageResource(R.drawable.keyframe_bright);
		midSmallBack.setImageResource(R.drawable.midframe_dark);
		if (flag == 3) {
			flag = 1;
			search.setText("");
			search.setEnabled(true);
			isFromSinger = false;
		} else if (flag == 2) {
			flag++;
			isOKDown = false;
			search.setText("");
			search.setEnabled(true);
		} else {
			flag++;
		}
		page = 1;
		layout.startAnimation(showImage);
		setTitleText();
		setVisible();
		setListView(flag, page);
	}
	// ���ϼ�ͷ����¼�����          
	private void arrowUpClick() {
		handler.sendEmptyMessage(4);
		if (page == 1)
			return;
		page--;
//		setTitleText();
		setListView(flag, page);
	}
	// ���¼�ͷ����¼�����
	private void arrowDownClick() {
		handler.sendEmptyMessage(5);
		page++;
//		setTitleText();
		setListView(flag, page);
	}
	// Ok������¼�����
	private void okClicked() {
		if (flag != 2) {
			if (!isOKDown) {
				search.setEnabled(false);
				// oKkey.setImageResource(R.drawable.keyboard_okbutton1);
				smallarrow.setImageResource(R.drawable.left);
				searchBack.setImageResource(R.drawable.keyframe_dark);
				midSmallBack.setImageResource(R.drawable.midframe_bright);
				isOKDown = true;
			} else if (isOKDown) {
				search.setEnabled(true);
				isOKDown = false;
				oKkey.setImageResource(R.drawable.keyborad_okbutton2);
				smallarrow.setImageResource(R.drawable.right);
				searchBack.setImageResource(R.drawable.keyframe_bright);
				midSmallBack.setImageResource(R.drawable.midframe_dark);
			}
		}
	}
	// ���Ե���¼�����
	private void pingxianClicked(){
		
	}
	// ���水ť����¼����� �����˸�
	private void jiaotiClicked(){
		if (!isOKDown) {
			String text = "";
			int length = search.getText().length();
			if (length > 0) {
				text = search.getText().toString().substring(0, length - 1);
			}
			search.setText(text);
			onSeTextChange();
			keyboardImageViews[11].setImageResource(idPicKeyboard_Yellow[11]);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(8);
				}
			}, 300);
		}
	}
	//���ذ�ť����¼�����
	private void BackClicked(){
		if (flag == 3 && isFromSinger) {
			flag = 1;
			search.setText(preText);
			setTitleText();
			setListView(flag, prePage);
			page = prePage;
			isFromSinger = false;
		} else if (!isFromSinger) {
			showImage.cancel();
//			Intent intent = new Intent();
//			intent.putExtra("pid", 2);
//			intent.setClass(RemoteMusicActivity.this, progressBarActivity.class);
//			startActivity(intent);
			finish();
		}
	}

	// ����������������ͼƬ
	private void setTitleText() {
		// flag=1:���ǵ�� flag=2:���а��� flag=3:�������
		if (flag == 1) {// ���ǵ��
			titleText.setText("��ҳ>���ǵ��");
			leftText.setText("�������");
			rightText.setText("���а���");
		} else if (flag == 2) {// ���а���
			titleText.setText("��ҳ>���а���");
			leftText.setText("���ǵ��");
			rightText.setText("�������");

		} else {// �������
			if (isFromSinger)
				titleText.setText("��ҳ>���ǵ��>�������");
			else
				titleText.setText("��ҳ>�������");
			leftText.setText("���а���");
			rightText.setText("���ǵ��");
		}
	}
	//  ������ͼ��ʾ/Ӱ��
	private void setVisible(){
		// flag=1:���ǵ�� flag=2:���а��� flag=3:�������
		singerVisible = flag == 1 ? View.VISIBLE : View.INVISIBLE;
		songVisible = flag == 2 ? View.VISIBLE : View.INVISIBLE;
		musicVisible = flag == 3 ? View.VISIBLE : View.INVISIBLE;
		keyBoardVisible = flag == 2 ? View.INVISIBLE : View.VISIBLE;
		searchBack.setVisibility(keyBoardVisible);
		searchframe.setVisibility(keyBoardVisible);
		oKkey.setVisibility(keyBoardVisible);
		smallarrow.setVisibility(keyBoardVisible);
		midSmallBack.setVisibility(keyBoardVisible);
		for (int i = 0; i < 12; i++) {
			keyboardImageViews[i].setVisibility(keyBoardVisible);
		}
		for (int i = 0; i < 10; i++) {
			songItems[i].setVisibility(songVisible);
			musicItems[i].setVisibility(musicVisible);
			if (i < 9) {
				singerItems[i].setVisibility(singerVisible);
			}
		}
	}
	// ������ʾ
	public void setListView(int currentflag, int currentpage) {
		nowCount = 10;
		// �������ҳ���������һҳ�ĸ�������Ŀ
		if (currentflag == 1) {
			String searchtext = search.getText().toString();
			if (searchtext == "")
				singercount = singer.getSingerCount();
			else
				singercount = singer.getSingerCountBySimpleName(searchtext);
			if (singercount % 9 == 0) {
				maxPage = singercount / 9;
				lastPageCount = 9;
			} else {
				maxPage = singercount / 9 + 1;
				lastPageCount = singercount % 9;
			}
			if (singercount == 0) {
				maxPage = 0;
				nowCount = lastPageCount = 0;
			}
		} else if (currentflag == 2) {
			songcount = song.getSongCount();
			if (songcount % 10 == 0) {
				maxPage = songcount / 10;
				lastPageCount = 10;
			} else {
				maxPage = songcount / 10 + 1;
				lastPageCount = songcount % 10;
			}
			if (songcount == 0) {
				maxPage = 0;
				nowCount = lastPageCount = 0;
			}
		} else {
			String searchtext = search.getText().toString();
			if (isFromSinger && searchtext == "")
				songcount = song.getSongCountBySigner(cSingerName);
			else if (isFromSinger && searchtext != "")
				songcount = song.getSongCountBySgAndSn(cSingerName, searchtext);
			else if (!isFromSinger && searchtext != "")
				songcount = song.getSongCountBySimpleName(searchtext);
			else
				songcount = song.getSongCount();
			if (songcount % 10 == 0) {
				maxPage = songcount / 10;
				lastPageCount = 10;
			} else {
				maxPage = songcount / 10 + 1;
				lastPageCount = songcount % 10;
			}
			if (songcount == 0) {
				maxPage = 0;
				nowCount = lastPageCount = 0;
			}
		}
		// �����ǰҳ��������ҳ���С��1ֱ�ӷ��ز����κθ���
		if (currentpage > maxPage && maxPage != 0) {
			page--;
			return;
		}
		if (currentpage < 1) {
			page++;
			return;
		}
		if (currentpage > maxPage && maxPage == 0) {
			currentpage = page = 1;
		}
		// ��ʾ���ֻ��߸���
		if (currentpage == maxPage) {
			nowCount = lastPageCount;
		}
		// �����ݿ��ó���Ϣ��������ʾ
		switch (currentflag) {// flag=1:���ǵ�� flag=2:���а��� flag=3:�������
		case 1: {
			singerList = null;
			String searchtext = search.getText().toString();
			if (searchtext != "")
				singerList = singer.queryNineSingerBySN(searchtext, currentpage);
			else
				singerList = singer.queryNineSinger(currentpage);
			if (nowCount == 10)
				nowCount = 9;
			String str = "";
			int i = 0;
			if (nowCount != 0)
				nowCount = singerList.size();
			for (; i < nowCount; i++) {
				singerItems[i].setSinger(singerList.get(i));
				singerItems[i].setVisibility(View.VISIBLE);
				singerItems[i].setImageBm(singerList.get(i).getImage(getBaseContext()));
				singerItems[i].invalidate();
			}
			for (; i < 9; i++) {
				// ����ǰҳ��Ϊ���һҳ���Ҹ��������������Ϊ9��������ʱ��9���б�ռ��������ݵĿؼ�����
				singerItems[i].setSinger(null);
				singerItems[i].setVisibility(View.INVISIBLE);
			}
		}
			break;
		case 2: {
			songList = null;
			songList = song.queryTenSongByTwoClicks(currentpage);
			if (nowCount != 0)
				nowCount = songList.size();
			String str = "";
			int i = 0;
			Log.d("setListString", "case 2");
			for (; i < nowCount; i++) {
				songItems[i].setNumBm(BitmapFactory.decodeResource(getResources(), idPicNum[i]));
				songItems[i].setHeatBm(BitmapFactory.decodeResource(getResources(), idPicHeat[3]));
				songItems[i].setSong(songList.get(i));
				songItems[i].setVisibility(View.VISIBLE);
				songItems[i].invalidate();
			}
			for (; i < 10; i++) {
				songItems[i].setSong(null);
				songItems[i].setVisibility(View.INVISIBLE);
			}
			if (currentpage == 1) {
				for (i = 0; i < 3; i++) {
					songItems[i].setHeatBm(BitmapFactory.decodeResource(getResources(), idPicHeat[i]));
					songItems[i].invalidate();
				}
			}
		}
			break;
		case 3: {
			songList = null;
			String searchtext = search.getText().toString();
			if (isFromSinger && searchtext == "")
				songList = song.findTenSongBySinger(cSingerName, currentpage);
			else if (isFromSinger && searchtext != "")
				songList = song.findTenSongBySgAndSn(cSingerName, searchtext, currentpage);
			else if (!isFromSinger && searchtext != "")
				songList = song.findTenSongBySimpleName(searchtext, currentpage);
			else
				songList = song.findTenSong(currentpage);
			if (nowCount != 0)
				nowCount = songList.size();
			String str = "";
			int i = 0;
			for (i = 0; i < nowCount; i++) {
				musicItems[i].setSong(songList.get(i));
				musicItems[i].setNumBm(BitmapFactory.decodeResource(getResources(), idPicNum[i]));
				musicItems[i].setVisibility(View.VISIBLE);
				musicItems[i].invalidate();
				if (isFromSinger) {
					str = cSingerName;
					musicItems[i].setNowSinger(cSingerName);
				}
			}
			for (; i < 10; i++) {
				musicItems[i].setSong(null);
				musicItems[i].setVisibility(View.INVISIBLE);
			}
		}
			break;
		}
		String strPage = currentpage + "/" + maxPage;
		Paint paint=pageText.getPaint();
		int width=(int)paint.measureText(strPage);
		pageText.setX(1750 - width);
		pageText.setText(strPage);
	}
	// �񵥵�衢�������
	public void remoteByList(int keycode) {
		Song outputSong = new Song();
		if (keycode >= 0 && keycode < nowCount) {
			outputSong = songList.get(keycode);
			if (outputSong.getIsAvailable() == 0) {
				Toast.makeText(getApplicationContext(), "��Ǹ���ø��������ڣ�", Toast.LENGTH_LONG).show();
				return;
			}
			Intent intent = new Intent();
			intent.putExtra("personid", personId);
			intent.putExtra("songId", outputSong.getSongID());
			intent.putExtra("isReturn", 0);

			sendOrderedSongMsg(outputSong.getSongID());
//			Class nextclass;
//			if (afferentParam == 0) {
//				nextclass = HiSingActivity.class;
//				intent.putExtra("activityId", 0);
//			} else if (afferentParam == 1) {
//				nextclass = PracticeActivity.class;
//				intent.putExtra("activityId", 1);
//			} else {
//				nextclass = PartyActivity.class;
//				intent.putExtra("activityId", 2);
//			}
//			intent.setClass(RemoteMusicActivity.this, progressBarActivity.class);
			Class nextclass;
			if (afferentParam == 0) {
				nextclass = HiSingActivity.class;
			} else if (afferentParam == 1) {
				nextclass = PracticeActivity.class;
			} else {
				nextclass = PartyActivity.class;
			}
			intent.setClass(RemoteMusicActivity.this, nextclass);
			RemoteMusicActivity.this.startActivity(intent);
			// System.exit(0);
			finish();
		} else
			return;
	}
	// ���ǵ��������������
	public void remoteMusicBySinger(int keycode) {
		if (keycode < 0 || keycode + 1 > nowCount) {
			return;
		}
		prePage = page;
		preText = search.getText().toString();
		search.setText("");
		page = 1;
		flag = 3;
		isFromSinger = true;
		isOKDown = false;
		oKkey.setImageResource(R.drawable.keyborad_okbutton2);
		smallarrow.setImageResource(R.drawable.right);
		searchBack.setImageResource(R.drawable.keyframe_bright);
		midSmallBack.setImageResource(R.drawable.midframe_dark);
		cSingerName = singerList.get(keycode).getName();
		setTitleText();
		setVisible();
		setListView(3, 1);
	}
	// �������ְ����¼�
	public void dealKeyDown(int keycode) {
		if (flag == 1) {
			if (!isOKDown) {
				handler.sendEmptyMessage(MSG_KEY_ZERO_YELLOW + keycode);
				String str = search.getText().toString();
				search.setText(str + keycode + "");
				onSeTextChange();
			}
		} else if (flag == 3) {
			if (!isOKDown) {
				handler.sendEmptyMessage(MSG_KEY_ZERO_YELLOW + keycode);
				String str = search.getText().toString();
				search.setText(str + keycode + "");
				onSeTextChange();
			}
		}
	}
	//�����ı����ݸı�
	private void onSeTextChange() {
		page = 1;
		setTitleText();
		setVisible();
		setListView(flag, 1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		DswLog.v("select", "resume");
		int myPid = Process.myPid();
		int myPids[] = new int[1];
		myPids[0] = myPid;
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		Debug.MemoryInfo[] dmf = am.getProcessMemoryInfo(myPids);
		// processInfo.setText("ռ���ڴ�"+dmf[0].dalvikPrivateDirty+"KB");
		DswLog.v("�ڴ�", dmf[0].dalvikPrivateDirty + "KB");
	}

	@Override
	protected void onDestroy() {
		// heatimage = null;singerimage = null;singerbox = null;number = null;
		arrow = null;
		songname = null;
		musicstar = null;
		leftText = null;
		rightText = null;
		pageText = null;
		search = null;
		searchframe = null;
		// keyBoard=null;
		searchBack = null;
		oKkey = null;
		layout0 = null;
		layout = null;
		showImage = null;
		handler = null;
		timer = null;
		idPicPage = null;
		idPicArrow = null;
		preText = null;
		cSingerName = null;
		typeFace = null;
		song = null;
		singer = null;
		songList = null;
		singerList = null;
		unregisterReceiver(broadcastRecv);
		super.onDestroy();
	}

	// =========================�㲥������==========================================================
	private class MyBroadcastRecv extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.receiveKeyPressedAction)) {
				Integer keyCode = (Integer) intent.getExtras().get("keycode");
				final Person psn = (Person) intent.getExtras().get("person");
				int code = keyCode.intValue();
				int pressedKey = -1;
				switch (code) {
				case Constant.KEY0:
					pressedKey = KeyEvent.KEYCODE_0;
					break;
				case Constant.KEY1:
					pressedKey = KeyEvent.KEYCODE_1;
					break;
				case Constant.KEY2:
					pressedKey = KeyEvent.KEYCODE_2;
					break;
				case Constant.KEY3:
					pressedKey = KeyEvent.KEYCODE_3;
					break;
				case Constant.KEY4:
					pressedKey = KeyEvent.KEYCODE_4;
					break;
				case Constant.KEY5:
					pressedKey = KeyEvent.KEYCODE_5;
					break;
				case Constant.KEY6:
					pressedKey = KeyEvent.KEYCODE_6;
					break;
				case Constant.KEY7:
					pressedKey = KeyEvent.KEYCODE_7;
					break;
				case Constant.KEY8:
					pressedKey = KeyEvent.KEYCODE_8;
					break;
				case Constant.KEY9:
					pressedKey = KeyEvent.KEYCODE_9;
					break;
				case Constant.KEYUP:
					pressedKey = KeyEvent.KEYCODE_DPAD_UP;
					break;
				case Constant.KEYDOWN:
					pressedKey = KeyEvent.KEYCODE_DPAD_DOWN;
					break;
				case Constant.KEYLEFT:
					pressedKey = KeyEvent.KEYCODE_DPAD_LEFT;
					break;
				case Constant.KEYRIGHT:
					pressedKey = KeyEvent.KEYCODE_DPAD_RIGHT;
					break;
				case Constant.KEYOK:
					pressedKey = KeyEvent.KEYCODE_ENTER;
					break;
				case Constant.KEYTURN:
					pressedKey = 219;
					break;
				case Constant.KEYSCREENDISPLAY:
					pressedKey = KeyEvent.KEYCODE_DPAD_LEFT;
					break;
				case Constant.KEYBACK:
					pressedKey = KeyEvent.KEYCODE_BACK;
					break;
				default:
					break;
				}
				if (pressedKey != -1) {
					personId = psn.personId;
					onKeyDown(pressedKey, null);
				}
			} else if (intent.getAction().equals(Constant.receiveRemotedSongAction)) {
				int songId = intent.getIntExtra("songid", -1);
				final Person psn = (Person) intent.getExtras().get("person");
				DswLog.v("Remote", "songid: " + songId);
				if (songId == -1)
					return;
				personId = psn.personId;
				throwCardSong(songId);
			} else if (intent.getAction().equals(Constant.getCurrentModeAction)) {
				final Person psn = (Person) intent.getExtras().get("person");

				Intent in = new Intent(RemoteMusicActivity.this, MainService.class);
				in.putExtra("mode", Constant.REMOTESONG);
				in.putExtra("person", psn);
				in.setAction(Constant.returnCurrentModeAction);
				startService(in);
			}
		}
	}

	// =========================�㲥����������==========================================================

	private void throwCardSong(int songId) {
		Song outputSong = new Song();
		outputSong = song.findSongById(songId);
		if (outputSong.getIsAvailable() == 0) {
			Toast.makeText(getApplicationContext(), "��Ǹ���ø��������ڣ�", Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("personid", personId);
		intent.putExtra("songId", outputSong.getSongID());
		intent.putExtra("isReturn", 0);

		sendOrderedSongMsg(outputSong.getSongID());
		Class nextclass;
		if (afferentParam == 0) {
			nextclass = HiSingActivity.class;
			intent.putExtra("activityId", 0);
		} else if (afferentParam == 1) {
			nextclass = PracticeActivity.class;
			intent.putExtra("activityId", 1);
		} else {
			nextclass = PartyActivity.class;
			intent.putExtra("activityId", 2);
		}
		intent.setClass(RemoteMusicActivity.this, progressBarActivity.class);
		RemoteMusicActivity.this.startActivity(intent);
		// System.exit(0);
		finish();
	}

	// �㲥������ע��
	private void regBroadcastRecv() {
		broadcastRecv = new MyBroadcastRecv();
		bFilter = new IntentFilter();
		bFilter.addAction(Constant.getCurrentModeAction);
		bFilter.addAction(Constant.receiveRemotedSongAction);
		bFilter.addAction(Constant.receiveKeyPressedAction);
		registerReceiver(broadcastRecv, bFilter);
	}

	private void sendNextActivityMsg(int activityId) {
		Intent in = new Intent(RemoteMusicActivity.this, MainService.class);
		in.putExtra("activityid", activityId);
		in.putExtra("personid", -2);
		in.setAction(Constant.nextActivityAction);
		startService(in);
	}

	private void sendOrderedSongMsg(int songId) {
		Intent in = new Intent(RemoteMusicActivity.this, MainService.class);
		in.putExtra("personid", personId);
		in.putExtra("songid", songId);
		in.setAction(Constant.orderedSongAction);
		startService(in);
	}
}