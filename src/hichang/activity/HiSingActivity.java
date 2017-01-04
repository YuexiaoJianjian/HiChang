package hichang.activity;

import hichang.Song.CMediaPlayer;
import hichang.Song.LocalBitmap;
import hichang.Song.ReadText;
import hichang.Song.Sentence;
import hichang.Song.Singer;
import hichang.Song.Song;
import hichang.Song.User;
import hichang.audio.AudRec;
import hichang.ourView.CurveAndLrc;
import hichang.ourView.CurveAndLrc.ModeType;
import hichang.test.DswLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.android.flypigeon.service.MainService;
import com.android.flypigeon.util.Constant;
import com.android.flypigeon.util.Person;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class HiSingActivity extends Activity {

	private MyBroadcastRecv broadcastRecv = null;
	private IntentFilter bFilter = null;
	private int personId = -1;

	/**
	 * ������
	 */
	private SeekBar volumnSeekBar;
	/**
	 * �Ƿ���ڰ�������
	 */
	private boolean isComa;
	/**
	 * �Ƿ����ģʽ
	 */
	private boolean isKTV;
	/**
	 * ý������(��mediaVolumn_intת��)
	 */
	private int mediaVolume;
	private float mediaVolumn_float = 0.3f;
	/**
	 * ý������(��mediaVolumnת��)
	 */
	private int mediaVolumn_int;
	/**
	 * ��˷�����
	 */
	private int micVolumn;
	/**
	 * �ж�timer�Ƿ���
	 */
	private boolean isStart;
	/**
	 * ����ͼƬ
	 */
	Bitmap[] accompany;
	/**
	 * ����ͼƬ
	 */
	Bitmap[] original;
	/**
	 * ����ͼƬ
	 */
	Bitmap[] ktvMode;
	/**
	 * ����ͼƬ
	 */
	Bitmap[] professional;
	/**
	 * ����ͼƬ
	 */
	Bitmap volumnnote;
	/**
	 * ��Դ
	 */
	private Resources resources;
	/**
	 * ���ֲ�����
	 */
	private CMediaPlayer media;
	/**
	 * timer
	 */
	private Timer timer;
	/**
	 * handler
	 */
	private Handler handler;
	/**
	 * timerTask
	 */
	private TimerTask task1, task2, task3, task4;
	/**
	 * ���·�������ʾ����ImageView
	 */
	private ImageView funcRedImage, funcGreenImage, funcYellowImage, funcBlueImage;
	/**
	 * ԭ����ktv�Ƿ�����ʾ
	 */
	private ImageView funcFirstImage;
	/**
	 * ���ࡢרҵ�Ƿ�����ʾ
	 */
	private ImageView funcSecImage;
	/**
	 * ԭ���������ktvרҵ�в�б��
	 */
	private ImageView funcSprit;
	/**
	 * ����ͼ��
	 */
	private ImageView volumnIcon;
	/**
	 * ����ͼƬ
	 */
	private ImageView singerImage;
	/**
	 * ��������������߷�
	 */
	private TextView musicInfo;
	/**
	 * ���ݿ�Song����ʽӿ�
	 */
	Song song;
	/**
	 * ���ݿ�Singer����ʽӿ�
	 */
	Singer singer;
	/**
	 * ���ݿ�User����ʽӿ�
	 */
	User user;
	/**
	 * ��������Ϣ
	 */
	Song nowSong;
	/**
	 * �������Ժ����о��ӵ����
	 */
	ArrayList<Sentence> nowSongSentences;
	/**
	 * ��ʼ��Timer
	 */
	Timer startTimer;
	/**
	 * startTimer�Ƿ�ȡ����
	 */
	private boolean isStartTimerCancel;
	/**
	 * ����ʱ��Timer
	 */
	Timer disTimer;
	/**
	 * disTimer�Ƿ�ȡ����
	 */
	private boolean isDisTimerCancel;
	/**
	 * ������̵�Timer
	 */
	Timer songTimer, timeTimer;
	/**
	 * ģʽ�л�ʱ�ĵ���ʱ
	 */
	Timer startGreenTimer;
	/**
	 * startGreenTimer�Ƿ�ȡ����
	 */
	private boolean isStartGreenTimerCancel;
	/**
	 * �������׸�Ĳ��ŵ�ʱ��
	 */
	int nowSongTime;
	/**
	 * ���ڵĸ�ʣ���һ���ʣ��Լ���һ����
	 */
	Sentence nowSentence, nextSentence, lastSentence;
	/**
	 * ָʾ��һ���ʵ��α꣨��ArrayList�е�λ�ã�
	 */
	int sentenceFlag;
	/**
	 * ����ʱ���
	 */
	ImageView[] colorBalls = new ImageView[3];
	/**
	 * ����ʱʣ�µ�ʱ��
	 */
	double leftTimes;
	/**
	 * �������׸����ʱ��
	 */
	int nowSongLength;
	/**
	 * ʱ���
	 */
	final int TIMESTEP = 40;
	/**
	 * ��׼ʱ��
	 */
	int standTime;
	/**
	 * ʱ�����λ��
	 */
	int timeHandX;
	/**
	 * ���׸�ķ���
	 */
	int nowSongScore;
	/**
	 * ��ǰ��������߷�
	 */
	int highScore;
	/**
	 * ʱ����
	 */
	ImageView timeHand;
	/**
	 * �Ƿ�����ѡ��ı��
	 */
	boolean isNeedFinish = false;
	/**
	 * ����������Ŀ
	 */
	private int sentenceCount;
	/**
	 * �Ƿ�տ�ʼ����
	 */
	private boolean isStartNow;
	/**
	 * �Ƿ�տ�ʼ����֮ǰ����֮ǰ�Ͱ�����ɫ��
	 */
	private boolean isPressPre;

	private TextView textView;

	private CurveAndLrc curveLrc;

	private int curveW, curveH, curveX, curveY;
	private int lrcH, lrcW, lrcX, lrcY;
	private AudRec audRec;

	final static int MSG_TURN_COLORBARS = 100;
	final static int MSG_HIDE_YELLOWBALL = 101;
	final static int MSG_HIDE_REDBALL = 102;
	final static int MSG_HIDE_BLUEBALL = 103;
	final static int MSG_SHOW_SCORE = 104;
	final static int MSG_START_SONG = 200;
	final static int MSG_SHOW_TIME = 105;
	final static int MSG_START_SHOW_TIME = 106;
	final static int MSG_SHOW_END = 107;
	ImageView staffImage;
	String songTime;

	int[] pids = new int[1];
	ActivityManager am;
	MemoryInfo outInfo;
	Timer timer1;
	TextView processInfo;

	ImageView helpImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sing);

		int pid = Process.myPid();
		pids[0] = pid;
		am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		outInfo = new MemoryInfo();
		processInfo = (TextView) findViewById(R.id.sinprocessinfo);
		timer1 = new Timer();

		isStart = false;

		textView = (TextView) findViewById(R.id.sing_text_zcl);
		volumnSeekBar = (SeekBar) findViewById(R.id.sing_player_seekbar);
		musicInfo = (TextView) findViewById(R.id.showscore);
		singerImage = (ImageView) findViewById(R.id.sin_image);
		funcRedImage = (ImageView) findViewById(R.id.sing_function_red);
		funcGreenImage = (ImageView) findViewById(R.id.sing_function_green);
		funcBlueImage = (ImageView) findViewById(R.id.sing_function_blue);
		funcYellowImage = (ImageView) findViewById(R.id.sing_function_yellow);
		funcFirstImage = (ImageView) findViewById(R.id.sing_funcfirstimage);
		funcSecImage = (ImageView) findViewById(R.id.sing_funcsecimage);
		funcSprit = (ImageView) findViewById(R.id.sing_funcsprit);
		volumnIcon = (ImageView) findViewById(R.id.sing_icon);
		colorBalls[0] = (ImageView) findViewById(R.id.sing_count_blue);
		colorBalls[1] = (ImageView) findViewById(R.id.sing_count_red);
		colorBalls[2] = (ImageView) findViewById(R.id.sing_count_yellow);
		timeHand = (ImageView) findViewById(R.id.sing_time_hand);
		curveLrc = (CurveAndLrc) findViewById(R.id.sing_curveandlrc);
		staffImage = (ImageView) findViewById(R.id.sing_staff);
		helpImage = (ImageView) findViewById(R.id.sing_sentence);
		resources = this.getResources();
		isPressPre = false;

		isComa = false;
		isKTV = false;
		mediaVolume = 3;
		mediaVolumn_int = 30;
		micVolumn = 30;
		accompany = new Bitmap[2];
		original = new Bitmap[2];
		ktvMode = new Bitmap[2];
		professional = new Bitmap[2];

		// ��ȡ�����洫���ĸ���ID
		Intent intent = getIntent();
		int songid = intent.getIntExtra("songId", 0);
		personId = intent.getIntExtra("personid", -1);

		Log.d("onCreate", "songid:"+songid+",personId="+personId);
		
		
		song = new Song(getBaseContext());
		singer = new Singer(getBaseContext());
		user = new User(getBaseContext());

		nowSong = song.findSongById(songid);
		Log.d("onCreate", "nowSong:"+nowSong==null?"NULL":nowSong.getName());
		
		int singerid = singer.querySingerByName(nowSong.getSinger1()).getiD();
		Log.d("onCreate", "singerid:"+singerid);
		
		highScore = user.queryFirstScore(nowSong.getSongID());
		Log.d("onCreate", "highScore:"+highScore);
		
		String songName = nowSong.getName();
		Log.d("onCreate", "songName:"+songName);
		
		if (songName.charAt(0) > 0 && songName.charAt(0) < 128 && songName.length() > 12)
			songName = songName.substring(0, 12) + "...";
		else if ((songName.charAt(0) < 0 || songName.charAt(0) > 128) && songName.length() > 7)
			songName = songName.substring(0, 7) + "...";
		String picPath = MainActivity.SD_PATH+"Singer/" + singerid + "/" + singerid + "_p.png";
		singerImage.setImageBitmap(LocalBitmap.getLoacalBitmap(picPath));
		// musicInfo.setText(nowSong.getName()+"\n��߷�-"+highScore+"\n");

		initPic();

		handler = new Handler() {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 10: {
					am.getMemoryInfo(outInfo);
					List<RunningAppProcessInfo> runApps = am.getRunningAppProcesses();
					Debug.MemoryInfo[] dmf = am.getProcessMemoryInfo(pids);
					processInfo.setText("��ǰ�ڴ�ռ��:" + dmf[0].dalvikPrivateDirty + "KB\n �����ڴ�:"
							+ outInfo.availMem / 1024 + "KB\n");
					handler.sendEmptyMessage(500);
				}
				// �鳪��ԭ�����л����أ�ͼ������ֵ��л���
				case 1:
					funcFirstImage.setVisibility(ImageView.INVISIBLE);
					funcSecImage.setVisibility(ImageView.INVISIBLE);
					funcSprit.setVisibility(ImageView.INVISIBLE);
					break;
				// ģʽ֮����л����أ�ͼ������ֵ��л���
				case 2:
					funcFirstImage.setVisibility(ImageView.INVISIBLE);
					funcSecImage.setVisibility(ImageView.INVISIBLE);
					funcSprit.setVisibility(ImageView.INVISIBLE);
					break;
				// ����ͼ������
				case 3:
					volumnIcon.setVisibility(ImageView.INVISIBLE);
					volumnSeekBar.setVisibility(SeekBar.INVISIBLE);
					textView.setVisibility(TextView.INVISIBLE);
					break;
				// ����ʱС�����
				case MSG_TURN_COLORBARS:
					colorBalls[0].setVisibility(ImageView.VISIBLE);
					colorBalls[1].setVisibility(ImageView.VISIBLE);
					colorBalls[2].setVisibility(ImageView.VISIBLE);
					disTimer = new Timer();
					disTimer.schedule(new TimerTask() {
						int i = 0;

						@Override
						public void run() {
							if (i == 0) {
								handler.sendEmptyMessage(MSG_HIDE_YELLOWBALL);
							} else if (i == 1) {
								handler.sendEmptyMessage(MSG_HIDE_REDBALL);
							} else if (i == 2) {
								handler.sendEmptyMessage(MSG_HIDE_BLUEBALL);
							} else {
								this.cancel();
								disTimer.cancel();
							}
							i++;
						}
					}, 0, 1000);
					break;
				// ����������С������
				case MSG_HIDE_YELLOWBALL:
					colorBalls[2].setVisibility(ImageView.INVISIBLE);
					break;
				// �����ڶ���С������
				case MSG_HIDE_REDBALL:
					colorBalls[1].setVisibility(ImageView.INVISIBLE);
					break;
				// ������һ��С������
				case MSG_HIDE_BLUEBALL:
					colorBalls[0].setVisibility(ImageView.INVISIBLE);
					break;
				// ���׸�����������ʾ
				case MSG_SHOW_SCORE:
					startShowScore();
					break;
				case MSG_START_SHOW_TIME:
					timeTimer = new Timer();
					timeTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(MSG_SHOW_TIME);
						}
					}, 0, 500);

					break;
				case MSG_SHOW_TIME:
					nowSongTime = media.CGetCurrentPosition();
					if (!isStart && nowSongTime > nowSongSentences.get(0).StartTimeofThis - 3000) {
						helpImage.setVisibility(View.INVISIBLE);
						handler.sendEmptyMessage(MSG_TURN_COLORBARS);
						isStart = true;
					}
					int time = nowSongTime / 1000;
					int second = time % 60;
					int minute = (time - second) / 60;
					if (second < 10) {
						musicInfo.setText(nowSong.getName() + "\n��߷�-" + highScore + "\n" + minute
								+ ": " + "0" + second + "/" + songTime);
					} else {
						musicInfo.setText(nowSong.getName() + "\n��߷�-" + highScore + "\n" + minute
								+ ": " + second + "/" + songTime);
					}
					break;
				case MSG_START_SONG:
					media.CStart();
					songTimer = new Timer();
					songTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							nowSongTime = media.CGetCurrentPosition();
							if (sentenceFlag < sentenceCount) {
								if (nowSongTime >= nowSongSentences.get(sentenceFlag).StartTimeofThis
										+ nowSongSentences.get(sentenceFlag).LastTimeofThis) {
									sentenceFlag++;
								}
							}
							if (nowSongTime >= nowSongSentences.get(sentenceCount - 1).StartTimeofThis
									+ nowSongSentences.get(sentenceCount - 1).LastTimeofThis + 440) {
								curveLrc.clearTotal();
								handler.sendEmptyMessage(MSG_SHOW_END);
								this.cancel();
								songTimer.cancel();
							} else {
								curveLrc.drawCurveAndLrc(nowSongTime - 440,
										audRec.getNote(nowSongTime - 440));
							}
						}
					}, 40, 40);
					break;
				case MSG_SHOW_END:
					helpImage.setImageResource(R.drawable.thanks);
					helpImage.setVisibility(View.VISIBLE);
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(MSG_SHOW_SCORE);
						}
					}, 5000);
					break;
				}
			}
		};

		// timer1.schedule(new TimerTask() {
		// @Override
		// public void run() {
		// handler.sendEmptyMessage(10);
		// }
		// }, 500);

		sendNextActivityMsg(Constant.SING);
//		sendStartSongMsg();
	}

	public void setKTV(boolean isKtv) {
		isKTV = isKtv;
		if (isKTV) {
			lrcH = (int) resources.getDimension(R.dimen.sing_ktv_lrc_height);
			lrcW = (int) resources.getDimension(R.dimen.sing_ktv_lrc_width);
			lrcX = (int) resources.getDimension(R.dimen.sing_ktv_lrc_x);
			lrcY = (int) resources.getDimension(R.dimen.sing_ktv_lrc_y);
			curveW = (int) resources.getDimension(R.dimen.sing_ktv_curve_width);
			curveH = (int) resources.getDimension(R.dimen.sing_ktv_curve_height);
			curveX = (int) resources.getDimension(R.dimen.sing_ktv_curve_x);
			curveY = (int) resources.getDimension(R.dimen.sing_ktv_curve_y);

			staffImage.setVisibility(View.VISIBLE);

		} else {
			lrcH = (int) resources.getDimension(R.dimen.sing_vocational_lrc_height);
			lrcW = (int) resources.getDimension(R.dimen.sing_vocational_lrc_width);
			lrcX = (int) resources.getDimension(R.dimen.sing_vocational_lrc_x);
			lrcY = (int) resources.getDimension(R.dimen.sing_vocational_lrc_y);
			curveW = (int) resources.getDimension(R.dimen.sing_vocational_curve_width);
			curveH = (int) resources.getDimension(R.dimen.sing_vocational_curve_height);
			curveX = (int) resources.getDimension(R.dimen.sing_vocational_curve_x);
			curveY = (int) resources.getDimension(R.dimen.sing_vocational_curve_y);
			staffImage.setVisibility(View.INVISIBLE);
		}
		curveLrc.setCurveXYWH(curveX, curveY, curveW, curveH);
		curveLrc.setLrcXYWH(lrcX, lrcY, lrcW, lrcH);
		curveLrc.setKTV(isKTV);
	}

	// ��ȡ��������Ϣ
	public void initSong() {
		ReadText nowText = new ReadText(nowSong.getSongLyricUrl());
		nowSongSentences = nowText.ReadData();

		lastSentence = nowSongSentences.get(nowSongSentences.size() - 1);
		sentenceCount = nowSongSentences.size();
		nowSongLength = media.CGetDuration();

		int sec = (nowSongLength / 1000) % 60;
		int min = (nowSongLength / 1000 - sec) / 60;
		if (sec < 10) {
			songTime = min + ":" + "0" + sec;
		} else {
			songTime = min + ":" + sec;
		}
		handler.sendEmptyMessage(MSG_START_SHOW_TIME);
		sentenceFlag = 0;

		curveLrc.init(nowSongSentences, nowText.max, nowText.min, ModeType.MODE_SING);
		setKTV(isKTV);

		audRec = new AudRec(nowSongSentences, handler, media);
		audRec.init();
	}

	// ��ʼ��һ��ý�岥����
	private void initMediaPlayer() {
//		media = new CMediaPlayer();
		media = new CMediaPlayer(getApplicationContext());
		media.CSetDataSource(nowSong.getMusicPath(), nowSong.getAccomanimentPath());
		media.CPrepare();
	}

	// ��ʼ�������õ���ͼƬ
	public void initPic() {
		volumnnote = BitmapFactory.decodeResource(resources, R.drawable.musicalnote);
		// ����ģ���ͼƬ
		for (int i = 0; i < 2; i++) {
			ktvMode[i] = BitmapFactory.decodeResource(resources, R.drawable.ktv_selected + i);
			professional[i] = BitmapFactory.decodeResource(resources,
					R.drawable.professional_selected + i);
			original[i] = BitmapFactory.decodeResource(resources, R.drawable.original_selected + i);
			accompany[i] = BitmapFactory.decodeResource(resources, R.drawable.accompany_selected
					+ i);
		}
	}

	// ��ʾ���׸�ķ���
	public void startShowScore() {
		nowSongScore = audRec.getTotalMark();
		// nowSongScore = 98;
		Intent intent = new Intent(HiSingActivity.this, HiScoreActivity.class);
		intent.putExtra("score", nowSongScore);
		intent.putExtra("songId", nowSong.getSongID());
		startActivityForResult(intent, 0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ����Ϊ���
		task1.cancel();
		task1 = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		};
		task2.cancel();
		task2 = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 2;
				handler.sendMessage(message);
			}
		};
		task3.cancel();
		task3 = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 3;
				handler.sendMessage(message);
			}
		};

		// ����Ϊ���ؼ�
		if (keyCode == 4) {
			Toast.makeText(this, "����", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.putExtra("type", 0);
			intent.setClass(HiSingActivity.this, RemoteMusicActivity.class);
			startActivity(intent);
			this.finish();
		}
		// ����Ϊ��ɫ�������ԭ�����л�
		if (keyCode == 183) {
			RedBtnClicked();
		}
		// ����Ϊ��ɫ��ģʽ���л�
		if (keyCode == 184) {
			GreenBtnClicked();
		}
		// ����Ϊ��ɫ����С����
		if (keyCode == 185) {
			YellowBtnClicked();
		}
		// ����Ϊ��ɫ����������
		if (keyCode == 186) {
			BlueBtnClicked();
		}
		return true;
	}
	
	public void ButtonClicked(View v){
		switch(v.getId()){
		case R.id.sing_function_red:
			RedBtnClicked();
			break;
		case R.id.sing_function_green:
			GreenBtnClicked();
			break;
		case R.id.sing_function_yellow:
			YellowBtnClicked();
			break;
		case R.id.sing_function_blue:
			BlueBtnClicked();
			break;
		}
	}
	// ����Ϊ��ɫ�������ԭ�����л�
	private void RedBtnClicked(){
		textView.setVisibility(TextView.INVISIBLE);
		volumnIcon.setVisibility(ImageView.INVISIBLE);
		volumnSeekBar.setVisibility(SeekBar.INVISIBLE);
		funcFirstImage.setVisibility(ImageView.VISIBLE);
		funcSecImage.setVisibility(ImageView.VISIBLE);
		funcSprit.setVisibility(ImageView.VISIBLE);
		if (isComa == false) {
			Toast.makeText(this, "����", Toast.LENGTH_SHORT).show();
			isComa = true;
			funcFirstImage.setImageBitmap(accompany[0]);
			funcSecImage.setImageBitmap(original[1]);
			media.CSetAccompany();
		} else {
			Toast.makeText(this, "ԭ��", Toast.LENGTH_SHORT).show();
			isComa = false;
			funcFirstImage.setImageBitmap(accompany[1]);
			funcSecImage.setImageBitmap(original[0]);
			media.CSetOriginal();
		}
		timer.schedule(task1, 3000);

	}
	// ����Ϊ��ɫ��ģʽ���л�
	private void GreenBtnClicked(){
		textView.setVisibility(TextView.INVISIBLE);
		volumnIcon.setVisibility(ImageView.INVISIBLE);
		volumnSeekBar.setVisibility(SeekBar.INVISIBLE);
		funcFirstImage.setVisibility(ImageView.VISIBLE);
		funcSecImage.setVisibility(ImageView.VISIBLE);
		funcSprit.setVisibility(ImageView.VISIBLE);
		if (isKTV == true) {
			colorBalls[0].setX(400);
			colorBalls[0].setY(770);
			colorBalls[1].setX(460);
			colorBalls[1].setY(770);
			colorBalls[2].setX(520);
			colorBalls[2].setY(770);
			Toast.makeText(this, "רҵģʽ", Toast.LENGTH_SHORT).show();
			funcFirstImage.setImageBitmap(ktvMode[1]);
			funcSecImage.setImageBitmap(professional[0]);
		} else {
			colorBalls[0].setX(120);
			colorBalls[0].setY(420);
			colorBalls[1].setX(170);
			colorBalls[1].setY(420);
			colorBalls[2].setX(220);
			colorBalls[2].setY(420);
			Toast.makeText(this, "ktvģʽ", Toast.LENGTH_SHORT).show();
			funcFirstImage.setImageBitmap(ktvMode[0]);
			funcSecImage.setImageBitmap(professional[1]);
		}
		setKTV(!isKTV);
		timer.schedule(task2, 3000);
	}
	// ����Ϊ��ɫ����С����
	private void YellowBtnClicked(){
		funcFirstImage.setVisibility(ImageView.INVISIBLE);
		funcSecImage.setVisibility(ImageView.INVISIBLE);
		funcSprit.setVisibility(ImageView.INVISIBLE);
		volumnIcon.setImageBitmap(volumnnote);
		Toast.makeText(this, "��С������������", Toast.LENGTH_SHORT).show();
		mediaVolume = mediaVolume - 1;
		mediaVolumn_float = mediaVolumn_float - 0.1f;
		if (mediaVolume < 0) {
			mediaVolume = 0;
			mediaVolumn_float = 0;
		}
		media.CSetVolume(mediaVolumn_float);
		mediaVolumn_int = (int) (mediaVolume * 10);
		volumnSeekBar.setProgress(mediaVolumn_int);
		textView.setText("" + mediaVolume);
		volumnIcon.setVisibility(ImageView.VISIBLE);
		volumnSeekBar.setVisibility(SeekBar.VISIBLE);
		textView.setVisibility(TextView.VISIBLE);
		timer.schedule(task3, 5000);
	}
	// ����Ϊ��ɫ����������
	private void BlueBtnClicked(){
		funcFirstImage.setVisibility(ImageView.INVISIBLE);
		funcSecImage.setVisibility(ImageView.INVISIBLE);
		funcSprit.setVisibility(ImageView.INVISIBLE);
		volumnIcon.setImageBitmap(volumnnote);
		mediaVolume = mediaVolume + 1;
		mediaVolumn_float = mediaVolumn_float + 0.1f;
		if (mediaVolume > 10) {
			mediaVolume = 10;
			mediaVolumn_float = 1;
		}
		media.CSetVolume(mediaVolumn_float);
		mediaVolumn_int = (int) (mediaVolume * 10);
		Toast.makeText(this, "����ý������", Toast.LENGTH_SHORT).show();
		volumnSeekBar.setProgress(mediaVolumn_int);
		textView.setText("" + mediaVolume);
		volumnIcon.setVisibility(ImageView.VISIBLE);
		volumnSeekBar.setVisibility(SeekBar.VISIBLE);
		textView.setVisibility(TextView.VISIBLE);
		timer.schedule(task3, 5000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 8:
			break;
		case 4:
			Intent intent = new Intent();
			intent.putExtra("type", 0);
			intent.setClass(HiSingActivity.this, RemoteMusicActivity.class);
			startActivity(intent);
			this.finish();
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		audRec.free();
		audRec = null;
		media.CStop();
		media.CRelease();
		Intent in = new Intent(HiSingActivity.this, MainService.class);
		in.setAction(Constant.stopSongAction);
		in.putExtra("personid", personId);
		startService(in);

		try {
			Thread.sleep(40);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sentenceFlag = 0;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		song = null;
		singer = null;
		user = null;
		nowSong = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		disTimer = new Timer();
		songTimer = new Timer();
		timer = new Timer();
		timeTimer = new Timer();
		task1 = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		};
		task2 = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 2;
				handler.sendMessage(message);
			}
		};
		task3 = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 3;
				handler.sendMessage(message);
				isStart = false;
			}
		};
		task4 = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 4;
				handler.sendMessage(message);
			}
		};

		regBroadcastRecv();
		initMediaPlayer();
		initSong();
		audRec.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		timeTimer.cancel();
		songTimer.cancel();
		disTimer.cancel();
		timer.cancel();
		timer1.cancel();
		unregisterReceiver(broadcastRecv);
	}

	// =========================�㲥������==========================================================
	private class MyBroadcastRecv extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.receivedTalkRequestAction)) {
				Intent mMainServiceIntent = new Intent(HiSingActivity.this, MainService.class);
				final Person psn = (Person) intent.getExtras().get("person");
				DswLog.v("HiSing", "Receive talk request from: " + psn.ipAddress);
				mMainServiceIntent.putExtra("person", psn);
				mMainServiceIntent.setAction(Constant.acceptTalkRequestAction);
				startService(mMainServiceIntent);
			} else if (intent.getAction().equals(Constant.receiveKeyPressedAction)) {
				Integer keyCode = (Integer) intent.getExtras().get("keycode");
				final Person psn = (Person) intent.getExtras().get("person");
				if (psn.personId != personId)
					return;
				int code = keyCode.intValue();
				int pressedKey = -1;
				switch (code) {
				case Constant.KEYBACK:
					pressedKey = KeyEvent.KEYCODE_BACK;
					break;
				case Constant.KEYMODE:
					pressedKey = 184;
					break;
				case Constant.KEYORIGINAL:
					pressedKey = 183;
					break;
				default:
					break;
				}
				if (pressedKey != -1) {
					onKeyDown(pressedKey, null);
				}
			} else if (intent.getAction().equals(Constant.getCurrentModeAction)) {
				final Person psn = (Person) intent.getExtras().get("person");

				Intent in = new Intent(HiSingActivity.this, MainService.class);
				in.putExtra("mode", Constant.SING);
				in.putExtra("person", psn);
				in.setAction(Constant.returnCurrentModeAction);
				startService(in);
			} else if (intent.getAction().equals(Constant.volumeChangedAction)) {
				final Person psn = (Person) intent.getExtras().get("person");
				if (psn == null)
					return;
				int volume = intent.getIntExtra("volume", -1);
				if (psn.personId == personId && volume != -1) {
					// �ı����� volumeΪ0~100����

				}
			}
		}
	}

	// =========================�㲥����������==========================================================

	// �㲥������ע��
	private void regBroadcastRecv() {
		broadcastRecv = new MyBroadcastRecv();
		bFilter = new IntentFilter();
		bFilter.addAction(Constant.volumeChangedAction);
		bFilter.addAction(Constant.getCurrentModeAction);
		bFilter.addAction(Constant.receivedTalkRequestAction);
		bFilter.addAction(Constant.receiveKeyPressedAction);
		registerReceiver(broadcastRecv, bFilter);
	}

	private void sendNextActivityMsg(int activityId) {
		Intent in = new Intent(HiSingActivity.this, MainService.class);
		in.putExtra("activityid", activityId);
		in.putExtra("personid", personId);
		in.setAction(Constant.nextActivityAction);
		startService(in);
	}

	private void sendStartSongMsg() {
		Intent in = new Intent(HiSingActivity.this, MainService.class);
		in.setAction(Constant.startSongAction);
		in.putExtra("personid", personId);
		startService(in);
	}
}
