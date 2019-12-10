package com.ichong.commonmodule.downloadmodule;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.Serializable;

/**
 * @Description 下载管理
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

public class Download {

	public static final String SERVICE_ACTION = "com.download2345.bookreader.service";
	public static final String RECEIVER_ACTION = "com.download2345.bookreader.receiver";
	public static final String RECEIVER_KEY_TASK_ENTITY = "task_entity";
	public static final String RECEIVER_KEY_TASK_ENTITYS = "task_entitys";
	public static final String URL = "url";
	public static final String TYPE = "type";
	public static final String SAVE_PATH = "save_path";
	public static final String EXT = "ext";
	public static final String RECEIVER_KEY_ERROR = "error";
	public static final int ADD = 0x0000001;
	public static final int REMOVE = 0x0000002;
	public static final int START = 0x0000003;
	public static final int ERROR = 0x0000004;
	public static final int CANCEL = 0x0000005;
	public static final int WAIT = 0x0000006;
	public static final int PROGRESS = 0x0000007;
	public static final int SUCCESS = 0x0000008;
	public static final int TASK_LIST = 0x0000009;

	private final Context mContext;
	private static Download instance;
	private DownloadReceiver mReceive;

	public static Download getInstanse(Context context) {
		if (instance == null) {
			instance = new Download(context);
		}
		return instance;
	}

	private Download(Context context) {
		this.mContext = context;
	}

	public void onResume(DownloadListener downloadListener) {
		if (this.mContext != null) {
			// 注册广播接收
			IntentFilter filter = new IntentFilter(Download.RECEIVER_ACTION);
			filter.setPriority(9999);
			this.mReceive = new DownloadReceiver(downloadListener);
			LocalBroadcastManager.getInstance(this.mContext).registerReceiver(this.mReceive, filter);
		}

	}

	public void onPause() {
		if (this.mContext != null) {
			LocalBroadcastManager.getInstance(this.mContext).unregisterReceiver(this.mReceive);
		}
	}

	/**
	 * 获取任务列表
	 * 
	 * @author zzy
	 * @date 2015年6月15日 下午2:26:36
	 */
	public void getTaskList() {
		Intent it = new Intent();
		it.setClass(this.mContext, DownloadService.class);
		it.setAction(SERVICE_ACTION);
		it.putExtra(TYPE, TASK_LIST);
		this.mContext.startService(it);
	}

	/**
	 * 添加一个任务
	 * 
	 * @param url
	 * @param savePath
	 * @author zzy
	 * @date 2015年6月10日 下午4:38:37
	 */
	public void addTask(String url, String savePath, Serializable ext) {
		Intent it = new Intent();
		it.setClass(this.mContext, DownloadService.class);
		it.setAction(SERVICE_ACTION);
		it.putExtra(URL, url);
		it.putExtra(SAVE_PATH, savePath);
		it.putExtra(EXT, ext);
		it.putExtra(TYPE, ADD);
		this.mContext.startService(it);
	}

	/**
	 * 删除一个任务
	 * 
	 * @param url
	 * @author zzy
	 * @date 2015年6月10日 下午4:38:46
	 */
	public void removeTask(String url) {
		Intent it = new Intent();
		it.setClass(this.mContext, DownloadService.class);
		it.setAction(SERVICE_ACTION);
		it.putExtra(URL, url);
		it.putExtra(TYPE, REMOVE);
		this.mContext.startService(it);
	}

	/**
	 * 开始执行任务
	 * 
	 * @author zzy
	 * @date 2015年6月10日 下午4:38:58
	 */
	public void start() {
		Intent it = new Intent();
		it.setClass(this.mContext, DownloadService.class);
		it.setAction(SERVICE_ACTION);
		it.putExtra(TYPE, START);
		this.mContext.startService(it);
	}

}
