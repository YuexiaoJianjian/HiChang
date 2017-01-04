package com.android.flypigeon.service;

import hichang.test.DswLog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.String;

import com.android.flypigeon.util.ByteAndInt;
import com.android.flypigeon.util.Constant;
import com.android.flypigeon.util.Message;
import com.android.flypigeon.util.Person;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class MainService extends Service {
	private ServiceBinder sBinder = new ServiceBinder();//�������
	private static ArrayList<Map<Integer,Person>> children = new ArrayList<Map<Integer,Person>>();//�����������е��û���ÿ��map���󱣴�һ�����ȫ���û�
	private static Map<Integer,Person> childrenMap = new HashMap<Integer,Person>();//��ǰ�����û�
	private static ArrayList<Integer> personKeys = new ArrayList<Integer>();//��ǰ�����û�id
	private static Map<Integer,List<Message>> msgContainer = new HashMap<Integer,List<Message>>();//�����û���Ϣ����
	private SharedPreferences pre = null;
	private SharedPreferences.Editor editor = null;
	private WifiManager wifiManager = null;
	private ServiceBroadcastReceiver receiver = null;
	public InetAddress localInetAddress = null;
	private String localIp = null;
	private byte[] localIpBytes = null; 
	private byte[] regBuffer = new byte[Constant.bufferSize];//��������ע�ύ��ָ��
	private byte[] msgSendBuffer = new byte[Constant.bufferSize];//��Ϣ���ͽ���
	private byte[] talkCmdBuffer = new byte[Constant.bufferSize];//ͨ��ָ��
	private byte[] songCmdBuffer = new byte[Constant.bufferSize];//ͨ��ָ��
	private static Person me = null;//������������������Ϣ
	private CommunicationBridge comBridge = null;//ͨѶ��Э�����ģ��		
	private boolean isServiceAlive = false;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return sBinder;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}
	@Override
	public void onRebind(Intent intent) {
		
	}
	@Override
	public void onCreate() {
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onStart(Intent intent, int startId) {	
		String actionString = intent.getAction();
		if(startId != 1) {
			if(actionString == null)
				return;
			else if(actionString.equals(Constant.acceptTalkRequestAction)) {	
				final Person psn = (Person)intent.getExtras().get("person");
				if(psn == null) return;
				new AcceptTalkThread(psn.personId).start();
			}
			else if(actionString.equals(Constant.stopSongAction)) {
				int personId = intent.getIntExtra("personid", -1);
				new StopTalkThread(personId).start();
			}
			else if(actionString.equals(Constant.returnCurrentModeAction)) {	
				final Person psn = (Person)intent.getExtras().get("person");
				int mode = intent.getIntExtra("mode", -1);
				if(psn == null) return;
				new SendCurrentModeThread(psn.personId,mode).start();
			}
			else if(actionString.equals(Constant.orderedSongAction)) {
				int personId = intent.getIntExtra("personid", -1);
				int songId = intent.getIntExtra("songid", -1);
				new SendOrderedSongThread(personId,songId).start();
			}
			else if(actionString.equals(Constant.orderedSongListAction)) {
				final Person psn = (Person)intent.getExtras().get("person");
				if(psn == null) return;
				ArrayList<Integer> songList = intent.getExtras().getIntegerArrayList("songlist");				
				new SendOrderedSongListThread(psn.personId,songList).start();
			}
			else if(actionString.equals(Constant.nextActivityAction)) {
				Integer activityID = (Integer)intent.getExtras().get("activityid");
				int personId = intent.getIntExtra("personid",-1);
				new NextActivityThread(personId,activityID.intValue()).start();
			}
			else if(actionString.equals(Constant.startSongAction)) {
				Integer personId = (Integer)intent.getExtras().get("personid");
				new StartSongThread(personId.intValue()).start();
			}
			else if(actionString.equals(Constant.refuseOrderSongAction)) {
				new RemoteSongThread(Constant.REFUSEORDERSONG).start();
			}
			else if(actionString.equals(Constant.acceptOrderSongAction)) {	
				new RemoteSongThread(Constant.ACCEPTORDERSONG).start();
			}
			else if(actionString.equals(Constant.remindOrderSongAction)) {	
				final Person psn = (Person)intent.getExtras().get("person");
				if(psn == null) return;
				new RemoteSongThread(Constant.REMINDORDERSONG,psn.personId).start();
			}
			else if(actionString.equals(Constant.currentSongFinishedAction)) {	
				final Person psn = (Person)intent.getExtras().get("person");
				if(psn == null) return;
				new RemoteSongThread(Constant.CURRENTSONGFINISH,psn.personId).start();
			}
			return;
		}
		
		isServiceAlive = true;
		
		initCmdBuffer();//��ʼ��ָ���
		wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		new CheckNetConnectivity().start();//�������״̬����ȡIP��ַ
		
		comBridge = new CommunicationBridge();//����socket����
		comBridge.start();
		
		pre = PreferenceManager.getDefaultSharedPreferences(this);
		editor = pre.edit();
		
		regBroadcastReceiver();//ע��㲥������
		getMyInfomation();//���������Ϣ
		new UpdateMe().start();//�����緢������������ע��
		new CheckUserOnline().start();//����û��б��Ƿ��г�ʱ�û�
		sendPersonHasChangedBroadcast();//֪ͨ�����û�������˳�
		System.out.println("Service started...");
	}
	
	private class SendOrderedSongThread extends Thread {
		int personId = -1;
		int songId = -1;
		
		public SendOrderedSongThread(int personId, int songId) {
			this.personId = personId;			
			this.songId = songId;
		}
		
		public void run() {
			SendOrderedSong(personId,songId);
		};
	}
	
	private class SendOrderedSongListThread extends Thread {
		int personId = -1;
		ArrayList<Integer> songList = new ArrayList<Integer>();
		
		public SendOrderedSongListThread(int personId, ArrayList<Integer> songList) {
			this.personId = personId;
			this.songList = songList;
		}
		
		public void run() {
			SendOrderedSongList(personId,songList);
		};
	}
	
	//���ͽ�����������
	private class AcceptTalkThread extends Thread {
		int personId = -1;
		
		public AcceptTalkThread(int personId) {
			this.personId = personId;			
		}
		
		public void run() {
			acceptTalk(personId);
		};
	};
	
	//
	private class StopTalkThread extends Thread {
		int personId = -1;
		
		public StopTalkThread(int personId) {
			this.personId = personId;
		}
		
		public void run() {
			stopTalk(personId);
		}
	}
	
	private class SendCurrentModeThread extends Thread {
		int personId = -1;
		int mode = -1;
		
		public SendCurrentModeThread(int personId, int mode) {
			this.personId = personId;			
			this.mode = mode;
		}
		
		public void run() {
			SendCurrentMode(personId, mode);
		};
	}
	
	private class StartSongThread extends Thread {
		int personId = -1;
		
		public StartSongThread(int personId) {
			this.personId = personId;			
		}
		
		public void run() {
			startSong(personId);
		};
	}
	
	private class NextActivityThread extends Thread {
		int activityID = -1;
		int personId = -1;
		
		public NextActivityThread(int personId,int activityID) {
			this.activityID = activityID;
			this.personId = personId;
		}
		
		public void run() {
			sendNextActivityID(personId,activityID);
		};
	}
	
	//���������߳�
	private class RemoteSongThread extends Thread {
		int personId = -1;
		int commandID = -1;
		
		public RemoteSongThread(int commandID) {	
			this.commandID = commandID;
		}
		
		public RemoteSongThread(int commandID, int personId) {
			this.commandID = commandID;
			this.personId = personId;			
		}
		
		public void run() {
			sendRemoteSongMsg(commandID,personId);
		};
	};
	
	//�����
	public class ServiceBinder extends Binder{
		public MainService getService(){
			return MainService.this;
		}
	}
	
    //������ѵ������Ϣ
    private void getMyInfomation(){
    	SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
//    	int iconId = pre.getInt("headIconId", R.drawable.black_bird);
    	String nickeName = pre.getString("nickeName", android.os.Build.MODEL);
    	//int myId = pre.getInt("myId", Constant.getMyId());
    	int myId = 1000001;
		editor.putInt("myId", myId);
		editor.commit();
		
    	if(null==me)me = new Person();
//    	me.personHeadIconId = iconId;
    	me.personNickeName = nickeName;
    	me.personId = myId;    	
    	//Toast.makeText(getApplicationContext(),localIp, Toast.LENGTH_SHORT).show();
    	me.ipAddress = localIp;
    	
    	//����ע�������û�����
    	System.arraycopy(ByteAndInt.int2ByteArray(myId), 0, regBuffer, 6, 4);
//    	System.arraycopy(ByteAndInt.int2ByteArray(iconId), 0, regBuffer, 10, 4);
    	for(int i=14;i<44;i++)regBuffer[i] = 0;//��ԭ�����ǳ��������
    	byte[] nickeNameBytes = nickeName.getBytes();
    	System.arraycopy(nickeNameBytes, 0, regBuffer, 14, nickeNameBytes.length);    	
    	
    	//����ͨ�������û�����
    	System.arraycopy(ByteAndInt.int2ByteArray(myId), 0, talkCmdBuffer, 6, 4);
//    	System.arraycopy(ByteAndInt.int2ByteArray(iconId), 0, talkCmdBuffer, 10, 4);
    	for(int i=14;i<44;i++)talkCmdBuffer[i] = 0;//��ԭ�����ǳ��������
    	System.arraycopy(nickeNameBytes, 0, talkCmdBuffer, 14, nickeNameBytes.length);
    	
    	//����ͨ�������û�����
    	System.arraycopy(ByteAndInt.int2ByteArray(myId), 0, songCmdBuffer, 6, 4);
//    	System.arraycopy(ByteAndInt.int2ByteArray(iconId), 0, talkCmdBuffer, 10, 4);
    	for(int i=14;i<44;i++)songCmdBuffer[i] = 0;//��ԭ�����ǳ��������
    	System.arraycopy(nickeNameBytes, 0, songCmdBuffer, 14, nickeNameBytes.length);
    }
	
	private String getCurrentTime(){
		Date date = new Date();
		return date.toLocaleString();
	}

    //�����������״̬,��ñ���IP��ַ
	private class CheckNetConnectivity extends Thread {
		public void run() {			
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}
			WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
			if (null != info) {
				int temp = info.getIpAddress();
				localIp = ( (temp) & 0xFF) +"."+((temp >> 8 ) & 0xFF)+"."+((temp >> 16 ) & 0xFF)+"."+(temp >> 24 & 0xFF );
				try {
					localInetAddress = InetAddress.getByName(localIp);
					localIpBytes = localInetAddress.getAddress();
					System.arraycopy(localIpBytes,0,regBuffer,44,4);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}			
			}
		};
	};
	
	//��ʼ��ָ���
	private void initCmdBuffer(){
		//��ʼ���û�ע��ָ���
		for(int i=0;i<Constant.bufferSize;i++)regBuffer[i]=0;
		System.arraycopy(Constant.pkgHead, 0, regBuffer, 0, 3);
		regBuffer[3] = Constant.CMD80;
		regBuffer[4] = Constant.CMD_TYPE1;
		regBuffer[5] = Constant.OPR_CMD1;
		
		//��ʼ����Ϣ����ָ���
		for(int i=0;i<Constant.bufferSize;i++)msgSendBuffer[i]=0;
		System.arraycopy(Constant.pkgHead, 0, msgSendBuffer, 0, 3);
		msgSendBuffer[3] = Constant.CMD81;
		msgSendBuffer[4] = Constant.CMD_TYPE1;
		msgSendBuffer[5] = Constant.OPR_CMD1;
				
		//��ʼ��ͨ��ָ��
		for(int i=0;i<Constant.bufferSize;i++)talkCmdBuffer[i]=0;
		System.arraycopy(Constant.pkgHead, 0, talkCmdBuffer, 0, 3);
		talkCmdBuffer[3] = Constant.CMD83;
		talkCmdBuffer[4] = Constant.CMD_TYPE1;
		talkCmdBuffer[5] = Constant.OPR_CMD1;
		
		//��ʼ��ͨ��ָ��
		for (int i = 0; i < Constant.bufferSize; i++)
			songCmdBuffer[i] = 0;
		System.arraycopy(Constant.pkgHead, 0, songCmdBuffer, 0, 3);
		songCmdBuffer[3] = Constant.KEYPRESSED;
		songCmdBuffer[4] = Constant.KEYUP;
		songCmdBuffer[5] = Constant.OPR_CMD1;
	}
	//��������û�����
	public ArrayList<Map<Integer,Person>> getChildren(){
		return children;
	}
	//��������û�id
	public ArrayList<Integer> getPersonKeys(){
		return personKeys;
	}
	//�����û�id��ø��û�����Ϣ
	public List<Message> getMessagesById(int personId){
		return msgContainer.get(personId);
	}
	//�����û�id��ø��û�����Ϣ����
	public int getMessagesCountById(int personId){
		List<Message> msgs = msgContainer.get(personId);
		if(null!=msgs){
			return msgs.size();
		}else {
			return 0;
		}
	}
	
	//ÿ��10�뷢��һ��������
	boolean isStopUpdateMe = false;
	private class UpdateMe extends Thread{
		@Override
		public void run() {
			while(!isStopUpdateMe){
				try{
					comBridge.joinOrganization();
					sleep(10000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	//����û��Ƿ����ߣ��������15��˵���û������ߣ�����б���������û�
	private class CheckUserOnline extends Thread{
		@Override
		public void run() {
			super.run();
			boolean hasChanged = false;
			while(!isStopUpdateMe){
				if(childrenMap.size()>0){
					Set<Integer> keys = childrenMap.keySet();
					for (Integer key : keys) {
						if(System.currentTimeMillis()-childrenMap.get(key).timeStamp>15000){
							childrenMap.remove(key);
							personKeys.remove(Integer.valueOf(key));
							hasChanged = true;
						}
					}
				}
				if(hasChanged)sendPersonHasChangedBroadcast();
				try {sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
	}
	
	//�����û����¹㲥
	private void sendPersonHasChangedBroadcast(){
		Intent intent = new Intent();
		intent.setAction(Constant.personHasChangedAction);
		sendBroadcast(intent);
	}
	
	//ע��㲥������
	private void regBroadcastReceiver(){
		receiver = new ServiceBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.WIFIACTION);
		filter.addAction(Constant.ETHACTION);
		filter.addAction(Constant.updateMyInformationAction);
		filter.addAction(Constant.imAliveNow);
		registerReceiver(receiver, filter);
	}
	
	//�㲥������������
	private class ServiceBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Constant.WIFIACTION) || intent.getAction().equals(Constant.ETHACTION)){
				new CheckNetConnectivity().start();
			}else if(intent.getAction().equals(Constant.updateMyInformationAction)){
				getMyInfomation();
				comBridge.joinOrganization();
			}else if(intent.getAction().equals(Constant.imAliveNow)){
				
			}
		}
	}
	
	//��ʼ��������
	public void startTalk(int personId){
		comBridge.startTalk(personId);
	}
	//������������
	public void stopTalk(int personId){
		comBridge.stopTalk(personId);
	}
	
	public void SendOrderedSong(int personId, int songId) {
		comBridge.SendOrderedSong(personId, songId);
	}
	
	public void SendOrderedSongList(int personId,ArrayList<Integer> songList) {
		comBridge.SendOrderedSongList(personId, songList);
	}
	
	//����Զ����������
	public void acceptTalk(int personId){
		comBridge.acceptTalk(personId);
	}
	//��ʼ����
	public void startSong(int personId){
		comBridge.startSong(personId);
	}
	//���͵�������Ϣ
	public void sendRemoteSongMsg(int commandID, int personId){
		comBridge.sendRemoteSongMsg(commandID,personId);
	}
	
	public void SendCurrentMode(int personId, int mode) {
		comBridge.sendCurrentMode(personId,mode);
	}
	
	public void sendNextActivityID(int personId,int activityID)
	{
		comBridge.sendNextActivityID(personId,activityID);
	}
	
	@Override
	public void onDestroy() {
		comBridge.release();
		unregisterReceiver(receiver);
		isStopUpdateMe = true;
		children.clear();
		System.out.println("Service on destory...");
	}
	
	//========================Э�������ͨѶģ��=======================================================
	private class CommunicationBridge extends Thread{
		private MulticastSocket multicastSocket = null;
		private byte[] recvBuffer = new byte[Constant.bufferSize];
		private boolean isStopTalk = false;//ͨ��������־
		
		private AudioHandler audioHandler = null;//��Ƶ����ģ�飬�����շ���Ƶ����
		
		public CommunicationBridge(){			
			audioHandler = new AudioHandler();
		}

		//���鲥�˿ڣ�׼���鲥ͨѶ
		@Override
		public void run() {
			super.run();
			try {
				multicastSocket = new MulticastSocket(Constant.PORT);
				multicastSocket.joinGroup(InetAddress.getByName(Constant.MULTICAST_IP));
				System.out.println("Socket started...");
				while (!multicastSocket.isClosed() && null!=multicastSocket) {
					for (int i=0;i<Constant.bufferSize;i++){recvBuffer[i]=0;}
		        	DatagramPacket rdp = new DatagramPacket(recvBuffer, recvBuffer.length);
		        	multicastSocket.receive(rdp);
		        	parsePackage(recvBuffer);
		        }
			} catch (Exception e) {
				try {
					if(null!=multicastSocket && !multicastSocket.isClosed()){
						multicastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
						multicastSocket.close();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} 
		}

		//�������յ������ݰ�
		private void parsePackage(byte[] pkg) {
			int CMD = pkg[3];//������
			int cmdType = pkg[4];//��������
			int oprCmd = pkg[5];//��������

			//����û�ID��
			byte[] uId = new byte[4];
			System.arraycopy(pkg, 6, uId, 0, 4);
			int userId = ByteAndInt.byteArray2Int(uId);
			if(userId == 1000001) return;
			
			switch (CMD) {
			case Constant.CMD80:
				switch (cmdType) {
				case Constant.CMD_TYPE1:
					//�������Ϣ�����Լ���������Է����ͻ�Ӧ��,���ѶԷ������û��б�
					if(userId != me.personId){
						updatePerson(userId,pkg);
						//����Ӧ���
						byte[] ipBytes = new byte[4];//������󷽵�ip��ַ
						System.arraycopy(pkg, 44, ipBytes, 0, 4);
						try {
							InetAddress targetIp = InetAddress.getByAddress(ipBytes);
							regBuffer[4] = Constant.CMD_TYPE2;//���Լ���ע����Ϣ�޸ĳ�Ӧ����Ϣ��־�����Լ�����Ϣ���͸�����
							DatagramPacket dp = new DatagramPacket(regBuffer,Constant.bufferSize,targetIp,Constant.PORT);
							multicastSocket.send(dp);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case Constant.CMD_TYPE2:
					if(userId != me.personId) {
						updatePerson(userId,pkg);
					}
					break;
				case Constant.CMD_TYPE3:
					childrenMap.remove(userId);
					personKeys.remove(Integer.valueOf(userId));
					sendPersonHasChangedBroadcast();
					break;
				}
				break;
			case Constant.CMD81:// �յ���Ϣ
				switch (cmdType) {
				case Constant.CMD_TYPE1:
					List<Message> messages = null;
					if(msgContainer.containsKey(userId)){
						messages = msgContainer.get(userId);
					}else{
						messages = new ArrayList<Message>();
					}
					byte[] msgBytes = new byte[Constant.msgLength];
					System.arraycopy(pkg, 10, msgBytes, 0, Constant.msgLength);
					String msgStr = new String(msgBytes).trim();
					Message msg = new Message();
					msg.msg = msgStr;
					msg.receivedTime = getCurrentTime();
					messages.add(msg);
					msgContainer.put(userId, messages);
					
					Intent intent = new Intent();
					intent.setAction(Constant.hasMsgUpdatedAction);
					intent.putExtra("userId", userId);
					intent.putExtra("msgCount", messages.size());
					sendBroadcast(intent);
					break;
				case Constant.CMD_TYPE2:
					break;
				}
				break;
			case Constant.CMD83://83�������ͨѶ���
				switch(cmdType){
				case Constant.CMD_TYPE1:
					switch(oprCmd){
					case Constant.OPR_CMD1://���յ�Զ������ͨ������
						System.out.println("Received a talk request ... ");
						isStopTalk = false;
						Person person = childrenMap.get(Integer.valueOf(userId));
						Intent intent = new Intent();
						intent.putExtra("person", person);
						intent.setAction(Constant.receivedTalkRequestAction);
						sendBroadcast(intent);
						break;
					case Constant.OPR_CMD2:
						//�յ��ر�ָ��ر�����ͨ��
						System.out.println("Received remote user stop talk cmd ... ");
						isStopTalk = true;
						Intent i = new Intent();
						i.setAction(Constant.remoteUserClosedTalkAction);
						sendBroadcast(i);
						audioHandler.stop();
						break;
					case Constant.OPR_CMD3:
						//����Ӧ�𣬿�ʼ����ͨ��
						if(!isStopTalk){
							System.out.println("Begin to talk with remote user ... ");
							final Person p = childrenMap.get(Integer.valueOf(userId));
							audioHandler.audioSend(p);
						}
						break;
					}
					break;
				}
				break;
			case Constant.KEYPRESSED://84����, ������Ϣ
				if(userId != me.personId){
					Person person = childrenMap.get(Integer.valueOf(userId));
					if(cmdType >= Constant.KEYUP && cmdType <= Constant.KEYORIGINAL) {					
						Intent i = new Intent();
						i.setAction(Constant.receiveKeyPressedAction);
						i.putExtra("keycode", cmdType);
						i.putExtra("person", person);
						sendBroadcast(i);
					}
				}				
				break;
			case Constant.ORDERSONG://�������
				if(userId != me.personId){
					switch (cmdType) {
					case Constant.ORDEREDSONG:
						//��ø���ID��
						byte[] songId = new byte[4];
						System.arraycopy(pkg, 10, songId, 0, 4);
						int intSongID = ByteAndInt.byteArray2Int(songId);
						
						Person person = childrenMap.get(Integer.valueOf(userId));
						Intent i = new Intent();
						i.setAction(Constant.receiveRemotedSongAction);
						i.putExtra("songid", intSongID);
						i.putExtra("person", person);
						sendBroadcast(i);
						break;
					case Constant.REQUESTORDEREDSONGLIST:
						Person psn = childrenMap.get(Integer.valueOf(userId));
						Intent i1 = new Intent();
						i1.setAction(Constant.requestOrderedSongAction);
						i1.putExtra("person", psn);
						sendBroadcast(i1);
						break;
					case Constant.GETMODE:
						Person psn1 = childrenMap.get(Integer.valueOf(userId));
						Intent i2 = new Intent();
						i2.setAction(Constant.getCurrentModeAction);
						i2.putExtra("person", psn1);
						sendBroadcast(i2);
						break;
					default:
						break;
					}
				}
				break;
			case Constant.VOLUME:
				if(userId != me.personId){
					Person person = childrenMap.get(Integer.valueOf(userId));
					if(cmdType >= 0 && cmdType <= 100) {					
						Intent i = new Intent();
						i.setAction(Constant.volumeChangedAction);
						i.putExtra("volume", cmdType);
						i.putExtra("person", person);
						sendBroadcast(i);
					}
				}		
				break;
			}
		}
		
		//���»���û���Ϣ���û��б���
		private void updatePerson(int userId,byte[] pkg){
			Person person = new Person();
			getPerson(pkg,person);
			childrenMap.put(userId, person);
			if(!personKeys.contains(Integer.valueOf(userId))) {
				personKeys.add(Integer.valueOf(userId));				
			}
			if(!children.contains(childrenMap)){
				children.add(childrenMap);
			}
			sendPersonHasChangedBroadcast();
		}
		
		//�ر�Socket����
		private void release(){
			try {
				regBuffer[4] = Constant.CMD_TYPE3;//�����������޸ĳ�ע����־�����㲥���ͣ��������û����˳�
				DatagramPacket dp = new DatagramPacket(regBuffer,Constant.bufferSize,InetAddress.getByName(Constant.MULTICAST_IP),Constant.PORT);
				multicastSocket.send(dp);
				System.out.println("Send logout cmd ...");
				
				multicastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
				multicastSocket.close();
				
				System.out.println("Socket has closed ...");
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				audioHandler.release();
			}
		}
		
		//�������ݰ�����ȡһ���û���Ϣ
		private void getPerson(byte[] pkg,Person person){
			
			byte[] personIdBytes = new byte[4];
			byte[] iconIdBytes = new byte[4];
			byte[] nickeNameBytes = new byte[30];
			byte[] personIpBytes = new byte[4];
			
			System.arraycopy(pkg, 6, personIdBytes, 0, 4);
			System.arraycopy(pkg, 10, iconIdBytes, 0, 4);
			System.arraycopy(pkg, 14, nickeNameBytes, 0, 30);
			System.arraycopy(pkg, 44, personIpBytes, 0, 4);
			
			person.personId = ByteAndInt.byteArray2Int(personIdBytes);
			person.personHeadIconId = ByteAndInt.byteArray2Int(iconIdBytes);
			person.personNickeName = (new String(nickeNameBytes)).trim();
			person.ipAddress = Constant.intToIp(ByteAndInt.byteArray2Int(personIpBytes));
			person.timeStamp = System.currentTimeMillis();
		}
		
		//ע���Լ���������
		public void joinOrganization(){
			try {
				if(null!=multicastSocket && !multicastSocket.isClosed()){
					regBuffer[4] = Constant.CMD_TYPE1;//�ָ���ע�������־����������ע���Լ�
					DatagramPacket dp = new DatagramPacket(regBuffer,Constant.bufferSize,InetAddress.getByName(Constant.MULTICAST_IP),Constant.PORT);
					multicastSocket.send(dp);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	    //��ʼ�������У���Զ������������������
	    public void startTalk(int personId){
			try {
				isStopTalk = false;
				talkCmdBuffer[3] = Constant.CMD83;
				talkCmdBuffer[4] = Constant.CMD_TYPE1;
		    	talkCmdBuffer[5] = Constant.OPR_CMD1;
				System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;
				DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
				multicastSocket.send(dp);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    //������������
	    public void stopTalk(int personId){
	    	isStopTalk = true;
	    	talkCmdBuffer[3] = Constant.CMD83;
	    	talkCmdBuffer[4] = Constant.CMD_TYPE1;
	    	talkCmdBuffer[5] = Constant.OPR_CMD2;
	    	Person person = childrenMap.get(Integer.valueOf(personId));
	    	if(person == null) return;
	    	try {
	    		System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);
	    		DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
				multicastSocket.send(dp);
				audioHandler.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	    //����Զ�������������󣬲���Զ�̷�����������
	    public void acceptTalk(int personId){
	    	isStopTalk = false;
			talkCmdBuffer[3] = Constant.CMD83;
			talkCmdBuffer[4] = Constant.CMD_TYPE1;
			talkCmdBuffer[5] = Constant.OPR_CMD3;
			Person person = childrenMap.get(Integer.valueOf(personId));
			if(person == null) return;
			try {
				//���ͽ�������ָ��
				System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
				DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
				multicastSocket.send(dp);
				audioHandler.audioPlay(person);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    //��ʼ����
	    public void startSong(int personId){	    	
	    	songCmdBuffer[3] = Constant.ORDERSONG;
	    	songCmdBuffer[4] = Constant.STARTSONG;
	    	
			if(personId != -1) {
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;
				DswLog.v("Service", "Start song: " + person.ipAddress);
				try {
					//���͵���������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, songCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(songCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	    //
	    public void sendNextActivityID(int personId, int activityID){
	    	talkCmdBuffer[3] = Constant.ORDERSONG;
			talkCmdBuffer[4] = Constant.NEXTACTIVITY;
			talkCmdBuffer[5] = (byte) activityID;
			DswLog.v("Service", "activityID: " + activityID);
			if(personId == -2) {
				try {
					//���Ͷಥ������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(Constant.MULTICAST_IP),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(personId != -1) {
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;
				
				try {
					//���͵���������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	    public void sendCurrentMode(int personId, int mode) {
	    	talkCmdBuffer[3] = Constant.ORDERSONG;
			talkCmdBuffer[4] = Constant.MODE;
			talkCmdBuffer[5] = (byte) mode;
			
			if(personId != -1) {
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;				
				try {
					//���͵���������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	    public void SendOrderedSong(int personId, int songId) {
	    	talkCmdBuffer[3] = Constant.ORDERSONG;
			talkCmdBuffer[4] = Constant.ORDEREDSONG;
			DswLog.v("Service", "Ordered Song: " + songId + " " + personId);
			System.arraycopy(ByteAndInt.int2ByteArray(songId), 0, talkCmdBuffer, 10, 4);
			if(personId != -1) {
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;				
				try {
					//���͵���������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	    public void SendOrderedSongList(int personId,ArrayList<Integer> songList) {
	    	talkCmdBuffer[3] = Constant.ORDERSONG;
			talkCmdBuffer[4] = Constant.ORDEREDSONGLIST;			
						
			if(personId != -1) {
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;
				try {
					//���͵���������Ϣ
					Iterator<Integer> it1 = songList.iterator();
					int songCount = 0;
					while(it1.hasNext()){
						Integer tempSongId = it1.next();				
						System.arraycopy(ByteAndInt.int2ByteArray(tempSongId.intValue()), 0, talkCmdBuffer, songCount*4+48, 4);
						songCount++;
					}
					System.arraycopy(ByteAndInt.int2ByteArray(songCount), 0, talkCmdBuffer, 10, 4);
					
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	    //���͵�������Ϣ
	    public void sendRemoteSongMsg(int commandID, int personId) {
	    	talkCmdBuffer[3] = Constant.ORDERSONG;
			talkCmdBuffer[4] = (byte) commandID;
			if(personId != -1) {
				Person person = childrenMap.get(Integer.valueOf(personId));
				if(person == null) return;
				try {
					//���͵���������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(person.ipAddress),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					//���Ͷಥ������Ϣ
					System.arraycopy(InetAddress.getByName(me.ipAddress).getAddress(), 0, talkCmdBuffer, 44, 4);				
					DatagramPacket dp = new DatagramPacket(talkCmdBuffer,Constant.bufferSize,InetAddress.getByName(Constant.MULTICAST_IP),Constant.PORT);
					multicastSocket.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    }
	    
	    //=========================RTP��������ģ��==================================================================    
		//����rtp��������ģ��
		private class AudioHandler{
			private AudioSend     audioSend = null;
			private AudioPlay     audioPlay = null;
			private SoundSender   sender    = null;
			private SoundReceiver receiver  = null;
			
			//����������Ƶ�������߳�
			public void audioSend(Person person){
				if(audioSend != null) stop();
				audioSend = new AudioSend(person);
				audioSend.start();
			}
			
			public void audioPlay(Person person){
				if(audioPlay != null) stop();
				audioPlay = new AudioPlay(person);
				audioPlay.start();
			}
			
			//��Ƶ���߳�
			public class AudioPlay extends Thread{
				Person person = null;
				public AudioPlay(Person person){
					this.person = person;	
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
				}
				
				@Override
				public void run() {
					receiver = new SoundReceiver(person.ipAddress);
				}
			}
			
			//��Ƶ�����߳�
			public class AudioSend extends Thread{
				Person person = null;				
				
				public AudioSend(Person person){
					this.person = person;			
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
				}
				@Override
				public void run() {
					super.run();	
					
					sender = new SoundSender(person.ipAddress);
					sender.run();					
				}
			}
			
			public void stop() {
				if(sender != null) {
					sender.stop();
					sender = null;
				}
				if(receiver != null) {
					receiver.stop();
					receiver = null;
				}
			}
			
			public void release() {
				if(sender != null) {
					sender.stop();
					sender = null;
				}
				if(receiver != null) {
					receiver.stop();
					receiver = null;
				}
			}
		}
		//=========================TCP��������ģ�����================================================================== 
	}
	//========================Э�������ͨѶģ�����=======================================================
}

