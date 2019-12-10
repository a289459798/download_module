package com.ichong.commonmodule.downloadmodule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @Description 类说明
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

@SuppressLint("NewApi")
class DownloadManage extends Thread implements DownloadTaskListener {

	private final Queue<DownloadTask> waitQueue;
	private Queue<DownloadTask> runningQueue;
	private final Settings mSetting;
	private boolean isRunning;
	private final Context mContext;

	public DownloadManage(Context context) {
		this.mContext = context;
		this.mSetting = new Settings();
		this.waitQueue = new LinkedList<DownloadTask>();
		this.runningQueue = new LinkedList<DownloadTask>();
	}

	/**
	 * 开始执行下载任务
	 * @author zzy
	 * @date 2015年6月9日 下午2:08:53
	 */
	public void startManage() {
		if (!this.isRunning) {
			this.isRunning = true;
			this.start();
		}
	}

	/**
	 * 关闭下载
	 * 
	 * @author zzy
	 * @date 2015年6月17日 下午4:24:37
	 */
	public synchronized void close() {
		this.isRunning = false;

		// 取消所有正在执行的task
		if (this.runningQueue != null) {
			while (this.runningQueue.peek() != null) {
				this.runningQueue.poll().onCancelled();
			}
		}

		if (this.waitQueue != null) {
			while (this.waitQueue.peek() != null) {
				this.waitQueue.poll().onCancelled();
			}
		}
	}

	/**
	 * 判断该任务在队列中是否存在
	 * 
	 * @param url
	 * @return
	 * @author zzy
	 * @date 2015年6月9日 下午2:19:26
	 */
	private synchronized boolean hasTask(String url) {
		if (url == null) {
			return true;
		}
		// 先判断是否在下载中
		if (this.runningQueue != null) {
			for (DownloadTask task : this.runningQueue) {
				if (task != null && url.equals(task.getDownloadTaskEntity().getUrl())) {
					return true;
				}
			}
		}

		if (this.waitQueue != null) {
			for (DownloadTask task : this.waitQueue) {
				if (task != null && url.equals(task.getDownloadTaskEntity().getUrl())) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized void getTaskList() {
		ArrayList<DownloadTaskEntity> tasks = new ArrayList<DownloadTaskEntity>();
		DownloadTaskEntity entity;
		// 先判断是否在下载中
		if (this.runningQueue != null) {
			for (DownloadTask task : this.runningQueue) {
				entity = task.getDownloadTaskEntity();
				if (entity != null) {
					entity.setStatus(DownloadStatus.TaskStatus.Running);
					tasks.add(entity);
				}
			}
		}

		if (this.waitQueue != null) {
			for (DownloadTask task : this.waitQueue) {
				entity = task.getDownloadTaskEntity();
				if (entity != null) {
					entity.setStatus(DownloadStatus.TaskStatus.Wait);
					tasks.add(entity);
				}
			}
		}

		Intent it = new Intent(Download.RECEIVER_ACTION);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITYS, tasks);
		it.putExtra(Download.TYPE, Download.TASK_LIST);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	/**
	 * 添加一个任务
	 *
	 * @param url
	 * @param savePath
	 * @author zzy
	 * @date 2015年6月9日 上午10:18:25
	 */
	public void addTask(String url, String savePath, Object ext) {
		DownloadTaskEntity downloadTaskEntity = new DownloadTaskEntity();
		downloadTaskEntity.setUrl(url);
		downloadTaskEntity.setLocalPath(savePath);
		downloadTaskEntity.setExt(ext);
		Log.i("gw", "!hasTask(url): " + !this.hasTask(url));

		if (!this.hasTask(url)) {

			DownloadTask downloadTask;
			try {
				downloadTask = new DownloadTask(this.mContext, downloadTaskEntity);
				downloadTask.setDownloadTaskListener(this);
				// if (runningQueue.size() >= mSetting.getMaxThreadNum()) {
				boolean flag = this.waitQueue.offer(downloadTask);
				if (!flag) {
					this.sendError(downloadTaskEntity, "任务列表已满");
				} else {
					this.sendWait(downloadTaskEntity);
				}
				// } else {
				// runningQueue.offer(downloadTask);
				// }
			} catch (MalformedURLException e) {
				e.printStackTrace();
				this.sendError(downloadTaskEntity, e.getMessage());
			}

		} else {
			this.sendError(downloadTaskEntity, "该任务已经存在");
		}
	}

	/**
	 * 删除一个任务
	 *
	 * @param url
	 * @author zzy
	 * @date 2015年6月9日 上午10:58:23
	 */
	public void removeTask(String url) {
		if (url == null) {
			return;
		}

		Log.d("zzy", "remove url:" + url);

		Log.d("zzy", "runningQueue:" + this.runningQueue);
		Log.d("zzy", "waitQueue:" + this.waitQueue);
		// 先判断是否在下载中
		if (this.runningQueue != null) {
			for (DownloadTask task : this.runningQueue) {
				Log.d("zzy", "task:" + task.getDownloadTaskEntity());
				if (task != null && url.equals(task.getDownloadTaskEntity().getUrl())) {
					task.onCancelled();
					return;
				}
			}
		}
		if (this.waitQueue != null) {
			for (DownloadTask task : this.waitQueue) {
				if (task != null && url.equals(task.getDownloadTaskEntity().getUrl())) {
					task.onCancelled();
					return;
				}
			}
		}

	}
	@Override
	public void run() {
		super.run();
		while (this.isRunning) {
			if (this.runningQueue == null) {
				this.runningQueue = new LinkedList<DownloadTask>();
			}
			if (this.runningQueue.size() < this.mSetting.getMaxThreadNum()) {
				DownloadTask task;
				if (this.waitQueue != null && (task = this.waitQueue.poll()) != null) {
					this.runningQueue.add(task);

					if (Build.VERSION.SDK_INT > 10) {
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
					} else {
						task.execute();
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private void sendStart(DownloadTaskEntity taskEntity) {
		Intent it = new Intent(Download.RECEIVER_ACTION);
		taskEntity.setStatus(DownloadStatus.TaskStatus.Start);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITY, taskEntity);
		it.putExtra(Download.TYPE, Download.START);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	private void sendWait(DownloadTaskEntity taskEntity) {
		Intent it = new Intent(Download.RECEIVER_ACTION);
		taskEntity.setStatus(DownloadStatus.TaskStatus.Wait);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITY, taskEntity);
		it.putExtra(Download.TYPE, Download.WAIT);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	private void sendError(DownloadTaskEntity taskEntity, String error) {
		Intent it = new Intent(Download.RECEIVER_ACTION);
		taskEntity.setStatus(DownloadStatus.TaskStatus.Error);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITY, taskEntity);
		it.putExtra(Download.RECEIVER_KEY_ERROR, error);
		it.putExtra(Download.TYPE, Download.ERROR);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	private void sendSuccess(DownloadTaskEntity taskEntity) {
		Intent it = new Intent(Download.RECEIVER_ACTION);
		taskEntity.setStatus(DownloadStatus.TaskStatus.Success);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITY, taskEntity);
		it.putExtra(Download.TYPE, Download.SUCCESS);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	private void sendCancel(DownloadTaskEntity taskEntity) {
		Intent it = new Intent(Download.RECEIVER_ACTION);
		taskEntity.setStatus(DownloadStatus.TaskStatus.Cancel);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITY, taskEntity);
		it.putExtra(Download.TYPE, Download.CANCEL);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	@Override
	public void setContextClassLoader(ClassLoader cl) {
		super.setContextClassLoader(cl);
	}

	private void sendProgress(DownloadTaskEntity taskEntity) {

		Intent it = new Intent(Download.RECEIVER_ACTION);
		taskEntity.setStatus(DownloadStatus.TaskStatus.Running);
		it.putExtra(Download.RECEIVER_KEY_TASK_ENTITY, taskEntity);
		it.putExtra(Download.TYPE, Download.PROGRESS);
		LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(it);
	}

	class Settings {
		private int maxThreadNum = 1;

		public int getMaxThreadNum() {
			return this.maxThreadNum;
		}

		public void setMaxThreadNum(int maxThreadNum) {
			this.maxThreadNum = maxThreadNum;
		}

	}

	@Override
	public void onSuccess(DownloadTask task) {
		this.sendSuccess(task.getDownloadTaskEntity());
		if (this.runningQueue != null) {
			this.runningQueue.remove(task);
		}

	}

	@Override
	public void onError(DownloadTask task, String error) {
		this.sendError(task.getDownloadTaskEntity(), error);
		if (this.runningQueue != null) {
			this.runningQueue.remove(task);
		}
	}

	@Override
	public void onCancel(DownloadTask task) {
		this.sendCancel(task.getDownloadTaskEntity());
		if (this.waitQueue != null) {
			this.waitQueue.remove(task);
		}
		if (this.runningQueue != null) {
			this.runningQueue.remove(task);
		}

	}

	@Override
	public void onProgress(DownloadTask task) {
		this.sendProgress(task.getDownloadTaskEntity());
	}

	@Override
	public void onStart(DownloadTask task) {
		this.sendStart(task.getDownloadTaskEntity());

	}

}
