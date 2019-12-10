package com.ichong.commonmodule.downloadmodule;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @Description 类说明
 * @author zzy
 * @date 2014年6月3日 下午6:26:17
 * @version V1.0.0
 */

class DownloadTask extends AsyncTask<Void, Integer, DownloadStatus.TaskStatus> {

	public static final int TIME_OUT = 30000;
	private static final int BUFFER_SIZE = 1024 * 4;
	private static final String TMP_SUFFER = ".download";

	private DownloadTaskListener mDownloadTaskListener;
	private final DownloadTaskEntity mDownloadTaskEntity;
	private volatile boolean isCancel;

	private HttpURLConnection connection;
	private long totalSize;
	private long downloadSize;

	private final File saveFile;
	private final File tmpSaveFile;
	private final URL downloadUrl;

	private String error;
	private final Context mContext;

	public DownloadTask(Context context, DownloadTaskEntity downloadTaskEntity) throws MalformedURLException {
		this.mDownloadTaskEntity = downloadTaskEntity;
		if (this.mDownloadTaskEntity == null || TextUtils.isEmpty(this.mDownloadTaskEntity.getUrl()) || TextUtils.isEmpty(this.mDownloadTaskEntity.getLocalPath())) {

			if (this.mDownloadTaskListener != null) {
				this.mDownloadTaskListener.onError(this, "参数格式不对");
			}

		}
		this.mContext = context;
		this.downloadUrl = new URL(this.mDownloadTaskEntity.getUrl());
		this.saveFile = new File(this.mDownloadTaskEntity.getLocalPath());
		this.tmpSaveFile = new File(this.mDownloadTaskEntity.getLocalPath() + TMP_SUFFER);
	}

	public void setDownloadTaskListener(DownloadTaskListener downloadTaskListener) {
		this.mDownloadTaskListener = downloadTaskListener;
	}

	public DownloadTaskEntity getDownloadTaskEntity() {
		return this.mDownloadTaskEntity;
	}

	@Override
	protected DownloadStatus.TaskStatus doInBackground(Void... params) {
		DownloadStatus.TaskStatus status = DownloadStatus.TaskStatus.Error;

		try {
			int redult = this.download();

			if (this.isCancel) {
				status = DownloadStatus.TaskStatus.Cancel;
			} else if (redult != -1) {
				status = DownloadStatus.TaskStatus.Success;
			}
		} catch (NetworkErrorException e) {
			e.printStackTrace();
			this.error = e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			this.error = e.getMessage();
		} finally {
			if (this.connection != null) {
				this.connection.disconnect();
			}
		}
		return status;
	}

	/**
	 * 下载
	 *
	 * @return
	 * @throws IOException
	 * @author zzy
	 * @throws NetworkErrorException
	 * @date 2015年6月10日 下午4:24:50
	 */
	private int download() throws IOException, NetworkErrorException {

		if (!DownloadUtils.isNetworkAvailable(this.mContext)) {
			throw new NetworkErrorException("网络不可用");
		}

		this.connection = (HttpURLConnection) this.downloadUrl.openConnection();
		this.connection.setConnectTimeout(TIME_OUT);
		this.connection.setRequestMethod("GET");
		this.connection.connect();
		this.totalSize = this.connection.getContentLength();

		Log.d("zzy", "totalSize:" + this.totalSize);

		this.mDownloadTaskEntity.setTotalProgress(this.totalSize);

		this.connection.disconnect();

		// 检测文件夹是否存在
		File parentDir = new File(this.saveFile.getParent());
		if (!parentDir.isDirectory()) {
			parentDir.mkdirs();
		}
		this.connection = (HttpURLConnection) this.downloadUrl.openConnection();
		this.connection.setRequestMethod("GET");
		this.connection.setReadTimeout(0);
		this.connection.setRequestProperty("Connection", "Keep-Alive");

		if (this.saveFile.exists() && this.totalSize == this.saveFile.length()) {
			// 下载文件已经存在
			this.saveFile.delete();
			// throw new IOException("下载文件已经存在");
		} else if (this.tmpSaveFile.exists()) {
			this.tmpSaveFile.delete();
			// connection.setRequestProperty("Range", "bytes=" +
			// tmpSaveFile.length() + "-");

		}
		/*
		 * check memory
		 */
		long storage = DownloadUtils.getAvailableStorage();
		if (this.totalSize > storage) {
			throw new IOException("存储空间不足");
		}

		/*
		 * start download
		 */
		ProgressReportingRandomAccessFile outputStream = new ProgressReportingRandomAccessFile(this.tmpSaveFile, "rw");
		Log.d("zzy", 4 + "");
		this.publishProgress(0, (int) this.totalSize);

		InputStream input = this.connection.getInputStream();
		int bytesCopied = this.copy(input, outputStream);

		if (this.tmpSaveFile.length() != this.totalSize && this.totalSize != -1 && !this.isCancel) {
			throw new IOException("Download incomplete: " + bytesCopied + " != " + this.totalSize);
		}

		return bytesCopied;
	}

	/**
	 * 写入流
	 *
	 * @param input
	 * @param out
	 * @return
	 * @throws IOException
	 * @throws NetworkErrorException
	 * @author zzy
	 * @date 2015年6月10日 下午4:24:59
	 */
	private int copy(InputStream input, RandomAccessFile out) throws IOException, NetworkErrorException {

		if (input == null || out == null) {
			return -1;
		}

		byte[] buffer = new byte[BUFFER_SIZE];

		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);

		int count = 0, n = 0;

		try {

			out.seek(out.length());

			while (!this.isCancel) {
				n = in.read(buffer, 0, BUFFER_SIZE);
				if (n == -1) {
					break;
				}
				out.write(buffer, 0, n);
				count += n;

				/*
				 * check network
				 */
				if (!DownloadUtils.isNetworkAvailable(this.mContext)) {
					throw new NetworkErrorException("网络不可用");
				}

			}
		} finally {
			this.connection.disconnect(); // must close client first
			out.close();
			in.close();
			input.close();
		}
		return count;

	}

	@Override
	protected void onPostExecute(DownloadStatus.TaskStatus result) {
		super.onPostExecute(result);

		if (result == DownloadStatus.TaskStatus.Cancel) {
			if (this.mDownloadTaskListener != null) {
				this.mDownloadTaskListener.onCancel(this);
			}
			return;
		} else if (result == DownloadStatus.TaskStatus.Error) {
			this.tmpSaveFile.delete();
			if (this.mDownloadTaskListener != null) {
				this.mDownloadTaskListener.onError(this, this.error);
			}
			return;
		}
		// finish download
		this.tmpSaveFile.renameTo(this.saveFile);
		if (this.mDownloadTaskListener != null) {
			this.mDownloadTaskListener.onSuccess(this);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (this.mDownloadTaskListener != null) {
			this.mDownloadTaskListener.onStart(this);
		}
	}

	/**
	 * 获取任务进度
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);

		if (progress.length > 1) {
			this.totalSize = progress[1];
			if (this.totalSize == -1) {
				this.tmpSaveFile.delete();
				if (this.mDownloadTaskListener != null) {
					this.mDownloadTaskListener.onError(this, "下载失败");
				}
			}
		} else {
			this.downloadSize = progress[0];
			this.mDownloadTaskEntity.setDownloadProgress(this.downloadSize);
			if (this.mDownloadTaskListener != null)
				this.mDownloadTaskListener.onProgress(this);
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		this.isCancel = true;
		try {
			if (this.connection != null) {
				this.connection.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
		private int progress;

		public ProgressReportingRandomAccessFile(File file, String mode) throws FileNotFoundException {

			super(file, mode);
		}

		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException {

			super.write(buffer, offset, count);

			if ((float) (DownloadTask.this.tmpSaveFile.length() - this.progress) / DownloadTask.this.totalSize * 100 > 1) {

				this.progress = (int) DownloadTask.this.tmpSaveFile.length();
				DownloadTask.this.publishProgress(this.progress);
			}
		}
	}

}
