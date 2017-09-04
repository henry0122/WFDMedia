package com.example.file_browser;

import java.io.File;

import android.net.Uri;

public class FileDetial {
	private File file;

	private String mime;

	// in byte
	private long length;

	public FileDetial(Uri uri) {
		// TODO Auto-generated constructor stub

		file = new File(uri.getPath());
		length = file.length();
		mime = Utility.getMIMEType(file.getName());
	}

	public File getFile() {
		return file;
	}

	public long getLength() {
		return length;
	}

	public String getMimeType() {
		return mime;
	}
}
