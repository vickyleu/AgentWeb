/*
 * Copyright (C)  Justson(https://github.com/Justson/Downloader)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.download.library;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cenxiaozhong
 * @date 2019/2/9
 * @since 1.0.0
 */
public class SyncDownloader extends Downloader implements Callable<File> {

	private static final Handler HANDLER = new Handler(Looper.getMainLooper());
	private volatile boolean mEnqueue;
	private ReentrantLock mLock = new ReentrantLock();
	private Condition mCondition = mLock.newCondition();

	SyncDownloader(DownloadTask downloadTask) {
		super();
		mDownloadTask = downloadTask;
	}

	@Override
	protected void onPreExecute() {
		try {
			super.onPreExecute();
		} catch (Throwable throwable) {
			this.mThrowable = throwable;
			throw throwable;
		}
	}

	@Override
	protected void onPostExecute(Integer integer) {
		try {
			super.onPostExecute(integer);
		} finally {
			mLock.lock();
			try {
				mCondition.signal();
			} finally {
				mLock.unlock();
			}
		}
	}

	@Override
	protected void destroyTask() {
	}

	@Override
	public DownloadTask cancelDownload() {
		super.cancelDownload();
		return null;
	}

	@Override
	public File call() throws Exception {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new UnsupportedOperationException("Sync download must call it in the non main-Thread  ");
		}
		mLock.lock();
		try {
			final CountDownLatch syncLatch = new CountDownLatch(1);
			HANDLER.post(new Runnable() {
				@Override
				public void run() {
					mEnqueue = download(mDownloadTask);
					syncLatch.countDown();
				}
			});
			syncLatch.await();
			if (!mEnqueue) {
				throw new RuntimeException("download task already exist!");
			}
			mCondition.await();
		} finally {
			mLock.unlock();
		}
		if (null != mThrowable) {
			throw (RuntimeException) mThrowable;
		}
		return mDownloadTask.mFile;
	}
}
