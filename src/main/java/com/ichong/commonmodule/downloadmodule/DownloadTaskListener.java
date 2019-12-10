package com.ichong.commonmodule.downloadmodule;

/**
 * @Description 任务回调接口
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

interface DownloadTaskListener {
	/**
	 * 任务完成
	 * 
	 * @param task
	 * @author zzy
	 * @date 2015年6月17日 下午4:22:42
	 */
	void onSuccess(DownloadTask task);

	/**
	 * 任务失败
	 * 
	 * @param task
	 * @param error
	 * @author zzy
	 * @date 2015年6月17日 下午4:22:53
	 */
	void onError(DownloadTask task, String error);

	/**
	 * 任务取消
	 * 
	 * @param task
	 * @author zzy
	 * @date 2015年6月17日 下午4:22:59
	 */
	void onCancel(DownloadTask task);

	/**
	 * 任务进度
	 * 
	 * @param task
	 * @author zzy
	 * @date 2015年6月17日 下午4:23:08
	 */
	void onProgress(DownloadTask task);

	/**
	 * 开始任务
	 * 
	 * @param task
	 * @author zzy
	 * @date 2015年6月17日 下午4:23:16
	 */
	void onStart(DownloadTask task);
}
