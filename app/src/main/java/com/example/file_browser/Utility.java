package com.example.file_browser;

import java.util.Locale;

class Utility {
	private Utility() {

	}

	public enum FileType {
		Image, Audio, Package, Webtext, Video, Text;
	}

	public static FileType getFileType(String fileName) {
		for (String extension : IMAGE_EXTENSION) {
			if (fileName.endsWith(extension))
				return FileType.Image;
		}

		for (String extension : AUDIO_EXTENSION) {
			if (fileName.endsWith(extension))
				return FileType.Audio;
		}

		for (String extension : PACKAGE_EXTENSION) {
			if (fileName.endsWith(extension))
				return FileType.Package;
		}

		for (String extension : WEBTEXT_EXTENSION) {
			if (fileName.endsWith(extension))
				return FileType.Webtext;
		}

		for (String extension : VIDEO_EXTENSION) {
			if (fileName.endsWith(extension))
				return FileType.Video;
		}

		return FileType.Text;
	}

	public static String getMIMEType(String fileName) {

		String type = "*/*";
		// 獲取后缀名前的分隔符"."在fileName中的位置
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex < 0) {
			return type;
		}
		// 獲取文件的后缀名
		String end = fileName.substring(dotIndex, fileName.length())
				.toLowerCase(Locale.getDefault());
		if (end == "")
			return type;

		// 在MIME和文件类型的匹配表中找到对应的MIME类型
		for (int i = 0; i < MIME_MAP_TABLE.length; i++) {
			if (end.equals(MIME_MAP_TABLE[i][0])) {
				type = MIME_MAP_TABLE[i][1];
				break;
			}
		}
		return type;
	}

	private static final String[] IMAGE_EXTENSION = { ".png", ".gif", ".jpg",
			".jpeg", ".bmp" };

	private static final String[] AUDIO_EXTENSION = { ".mp3", ".wav", ".ogg",
			".midi", ".wma" };

	private static final String[] PACKAGE_EXTENSION = { ".jar", ".zip", ".rar",
			".gz" };

	private static final String[] WEBTEXT_EXTENSION = { ".htm", ".html", ".php" };

	private static final String[] VIDEO_EXTENSION = { ".mp4", ".rm", ".mpg",
			".avi", ".mpeg" };

	private static final String[][] MIME_MAP_TABLE = {
			// {后缀名，MIME类型}
			{ ".3gp", "video/3gpp" },
			{ ".apk", "application/vnd.android.package-archive" },
			{ ".asf", "video/x-ms-asf" },
			{ ".avi", "video/x-msvideo" },
			{ ".bin", "application/octet-stream" },
			{ ".bmp", "image/bmp" },
			{ ".c", "text/plain" },
			{ ".class", "application/octet-stream" },
			{ ".conf", "text/plain" },
			{ ".cpp", "text/plain" },
			{ ".doc", "application/msword" },
			{ ".docx",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			{ ".xls", "application/vnd.ms-excel" },
			{ ".xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			{ ".exe", "application/octet-stream" },
			{ ".gif", "image/gif" },
			{ ".gtar", "application/x-gtar" },
			{ ".gz", "application/x-gzip" },
			{ ".h", "text/plain" },
			{ ".htm", "text/html" },
			{ ".html", "text/html" },
			{ ".jar", "application/java-archive" },
			{ ".java", "text/plain" },
			{ ".jpeg", "image/jpeg" },
			{ ".jpg", "image/jpeg" },
			{ ".js", "application/x-javascript" },
			{ ".log", "text/plain" },
			{ ".m3u", "audio/x-mpegurl" },
			{ ".m4a", "audio/mp4a-latm" },
			{ ".m4b", "audio/mp4a-latm" },
			{ ".m4p", "audio/mp4a-latm" },
			{ ".m4u", "video/vnd.mpegurl" },
			{ ".m4v", "video/x-m4v" },
			{ ".mov", "video/quicktime" },
			{ ".mp2", "audio/x-mpeg" },
			{ ".mp3", "audio/x-mpeg" },
			{ ".mp4", "video/mp4" },
			{ ".mpc", "application/vnd.mpohun.certificate" },
			{ ".mpe", "video/mpeg" },
			{ ".mpeg", "video/mpeg" },
			{ ".mpg", "video/mpeg" },
			{ ".mpg4", "video/mp4" },
			{ ".mpga", "audio/mpeg" },
			{ ".msg", "application/vnd.ms-outlook" },
			{ ".ogg", "audio/ogg" },
			{ ".pdf", "application/pdf" },
			{ ".png", "image/png" },
			{ ".pps", "application/vnd.ms-powerpoint" },
			{ ".ppt", "application/vnd.ms-powerpoint" },
			{ ".pptx",
					"application/vnd.openxmlformats-officedocument.presentationml.presentation" },
			{ ".prop", "text/plain" }, { ".rc", "text/plain" },
			{ ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
			{ ".sh", "text/plain" }, { ".tar", "application/x-tar" },
			{ ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
			{ ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
			{ ".wmv", "audio/x-ms-wmv" },
			{ ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
			{ ".z", "application/x-compress" },
			{ ".zip", "application/x-zip-compressed" }, { "", "*/*" } };
}
