package com.ichong.commonmodule.downloadmodule;


import java.io.Serializable;

/**
 * @Description 类说明
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

public class DownloadTaskEntity implements Serializable {

	private String url;
	private String localPath;
	private Object ext;
	private long downloadProgress;
	private long totalProgress;
	private DownloadStatus.TaskStatus status;

	/**
	 * @return the ext
	 */
	public Object getExt() {
		return this.ext;
	}

	/**
	 * @param ext
	 *            the ext to set
	 */
	public void setExt(Object ext) {
		this.ext = ext;
	}

	/**
	 * @return the status
	 */
	public DownloadStatus.TaskStatus getStatus() {
		return this.status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(DownloadStatus.TaskStatus status) {
		this.status = status;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the downloadProgress
	 */
	public long getDownloadProgress() {
		return this.downloadProgress;
	}

	/**
	 * @param downloadProgress
	 *            the downloadProgress to set
	 */
	public void setDownloadProgress(long downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	/**
	 * @return the totalProgress
	 */
	public long getTotalProgress() {
		return this.totalProgress;
	}

	/**
	 * @param totalProgress
	 *            the totalProgress to set
	 */
	public void setTotalProgress(long totalProgress) {
		this.totalProgress = totalProgress;
	}

	/**
	 * @return the localPath
	 */
	public String getLocalPath() {
		return this.localPath;
	}

	/**
	 * @param localPath
	 *            the localPath to set
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String toString() {
		return "TASK INFO \n status:" + this.status + " \n url:" + this.url + "\nlocalpath:" + this.localPath + "\ntotalProgress:" + this.totalProgress + "\ndownloadProgress:"
				+ this.downloadProgress;
	}

}
