package com.example.file_browser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import com.example.azure.wdmedia.R;
import com.example.file_browser.Utility.FileType;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FileBrowserActivity extends ListActivity {

	private ArrayList<HashMap<String, Object>> folderFiles;
	private File currentFolder;
	private HashMap<FileType, Integer> fileTypeIcon;
	private Comparator<HashMap<String, Object>> fileComparator;
	private FileBrowserListener fileBrowserListener;

	private SelectedItems selectedItems;

	private TextView txtHeader;

	public static final String ACTION_FILEBROWSER = "filebrowser";
	public static final String FOLDER = "folder";
	public static final int MUSIC_FOLDER = 0;
	public static final int DOWNLOAD_FOLDER = 1;
	public static final int MOVIE_FOLDER = 2;

	private static final String STR_REFRESH = "---(重新整理)---";
	private static final String STR_BACK = "---(回上一層)---";
	private static final String STR_ALL = "---(全選)---";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		init();

		if (getIntent().getAction().equals(ACTION_FILEBROWSER)) {
			int tmp = getIntent().getExtras().getInt(FOLDER);
			System.out.println("fucku====" + tmp);
			if (tmp == MUSIC_FOLDER) {
				browseTo(new File(Environment.getExternalStorageDirectory() + "/Music/"));
			}
			if (tmp == DOWNLOAD_FOLDER) {
				browseTo(new File(Environment.getExternalStorageDirectory() + "/Download/"));
			}
			if (tmp == MOVIE_FOLDER) {
				browseTo(new File(Environment.getExternalStorageDirectory() + "/Movies/"));
			}
		}
	}

	private void init() {
		fileTypeIcon = new HashMap<FileType, Integer>();
		fileTypeIcon.put(FileType.Image, Integer.valueOf(R.drawable.image));
		fileTypeIcon.put(FileType.Audio, Integer.valueOf(R.drawable.audio));
		fileTypeIcon.put(FileType.Package, Integer.valueOf(R.drawable.packed));
		fileTypeIcon.put(FileType.Webtext, Integer.valueOf(R.drawable.webtext));
		fileTypeIcon.put(FileType.Video, Integer.valueOf(R.drawable.video));
		fileTypeIcon.put(FileType.Text, Integer.valueOf(R.drawable.text));

		txtHeader = new TextView(this);
		txtHeader.setTextSize(18);
		txtHeader.setPadding(80, 14, 20, 14);
		txtHeader.setBackgroundResource(R.drawable.header_bg);
		txtHeader.setSingleLine();
		getListView().addHeaderView(txtHeader, null, false);

		folderFiles = new ArrayList<HashMap<String, Object>>();

		SimpleAdapter adapter = new SimpleAdapter(this, folderFiles,
				R.layout.file_browser_item, new String[] { "Icon", "Name" },
				new int[] { R.id.imgFileIcon, R.id.txtFileName });

		setListAdapter(adapter);
		fileComparator = createComparator();

		selectedItems = new SelectedItems();
		fileBrowserListener = new DefaultListener();
		setTitle("默認模式");

	}

	private void browseTo(File file) {
		txtHeader.setText(file.getAbsolutePath());
		if (file.isDirectory()) {

			System.out.println("fucku====" + "enter browseTo isDirectory");
			fileBrowserListener.onSwitchFolder();

			if (selectedItems != null)
				selectedItems.clear();

			currentFolder = file;
			System.out.println("fucku====" + "start addFiles");
			addFiles(file.listFiles());
		} else {
			fileBrowserListener.onChooseFile(file, false);
		}
	}

	private void browseBack() {
		if (currentFolder.getParent() != null)
			browseTo(currentFolder.getParentFile());
	}

	private void chooseAll(File file, boolean isAll)
	{
		File[] tmp ;
		txtHeader.setText(file.getAbsolutePath());
		if (file.isDirectory()) {
			tmp = file.listFiles();
			if(tmp != null)
			{
				System.out.println("fuck in ~~~~choose all");
				for (File files : tmp) {
					System.out.println("Fuck~~~~ " + files.getPath() + " ~~~~ " + files.getName());
					fileBrowserListener.onChooseFile(files, isAll);
				}
			}
		}
	}

	private void addFiles(File[] files) {
		folderFiles.clear();

		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put("Icon", R.drawable.refresh);
		item.put("Name", STR_REFRESH);
		folderFiles.add(item);

//		if (currentFolder.getParent() != null) {
//			item = new HashMap<String, Object>();
//			item.put("Icon", R.drawable.arrow);
//			item.put("Name", STR_BACK);
//			folderFiles.add(item);
//		}

		item = new HashMap<String, Object>();
		item.put("Icon", R.drawable.all);
		item.put("Name", STR_ALL);
		folderFiles.add(item);

		int iconId = 0;
		String fileName = "";

		if (files != null) {
			System.out.println("fucku====" + "files not null");
			for (File file : files) {
				fileName = file.getName();
				System.out.println("fucku==== " + "filename: " + fileName);
				if (file.isDirectory()) {
					iconId = R.drawable.folder;
				} else {
					FileType type = fileBrowserListener.onFilterFile(fileName);
					if (type == null)
						continue;
					else
						iconId = fileTypeIcon.get(type);
				}
				item = new HashMap<String, Object>();
				item.put("Icon", iconId);
				item.put("Name", fileName);
				folderFiles.add(item);
			}
		}

		Collections.sort(folderFiles, fileComparator);

		if (fileBrowserListener.hasOperation())
			folderFiles.add(fileBrowserListener.operationItem());

		((SimpleAdapter) getListAdapter()).notifyDataSetChanged();
	}

	private Comparator<HashMap<String, Object>> createComparator() {
		return new Comparator<HashMap<String, Object>>() {

			@Override
			public int compare(HashMap<String, Object> lhs,
					HashMap<String, Object> rhs) {
				// TODO Auto-generated method stub
				String lName = (String) lhs.get("Name"), rName = (String) rhs
						.get("Name");
				return lName.compareTo(rName);
			}
		};
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		// position-1 due to the header view
		int iconId = (Integer) folderFiles.get(position - 1).get("Icon");
		if (iconId == R.drawable.refresh) {

			browseTo(currentFolder);
		} else if (iconId == R.drawable.all) {
			//browseBack();
			chooseAll(currentFolder, true);
			if (selectedItems != null)
			{
				int i = l.getChildCount();
				for(int j = 3; j < i-1; j++)
				{
					System.out.println("Selected~~~~");
					View vv = l.getChildAt(j);
					selectedItems.chooseItem(vv, true);
				}
			}

		} else if (fileBrowserListener.isOperation(iconId)) {
			fileBrowserListener.handleOperation();
		} else {

			if (selectedItems != null)
				selectedItems.chooseItem(v, false);

			File clickedFile = null;
			clickedFile = new File(currentFolder.getAbsolutePath() + "/"
					+ folderFiles.get(position - 1).get("Name"));
			if (clickedFile != null)
				browseTo(clickedFile);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_browser_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.file_browser_filtered) {
			if (selectedItems != null)
				selectedItems.clear();

			selectedItems = new SelectedItems();
			fileBrowserListener = new DefaultListener();
			setTitle("默認模式");

			browseTo(currentFolder);
			return true;
		}
		if (id == R.id.file_browser_normal) {
			if (selectedItems != null)
				selectedItems.clear();

			selectedItems = null;
			fileBrowserListener = createNormalFileBrowserListener();
			setTitle("一般模式");

			browseTo(currentFolder);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private interface FileBrowserListener {
		/**
		 * 
		 * @param fileName
		 * @return the type of file, or null when file is not allowed to pass
		 */
		public FileType onFilterFile(String fileName);

		public void onChooseFile(File file, boolean isAll);

		public void onSwitchFolder();

		public boolean hasOperation();

		/**
		 * 
		 * @param iconId
		 * @return true if has operation and icon id is the same as operation
		 *         icon id
		 */
		public boolean isOperation(int iconId);

		/**
		 * use key "Icon" to set icon id, and use key "Name" to set file name
		 * 
		 * @return
		 */
		public HashMap<String, Object> operationItem();

		public void handleOperation();
	}

	private FileBrowserListener createNormalFileBrowserListener() {
		return new FileBrowserListener() {

			@Override
			public FileType onFilterFile(String fileName) {
				// TODO Auto-generated method stub
				return Utility.getFileType(fileName);
			}

			@Override
			public void onChooseFile(File file, boolean isAll) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				intent.setAction(Intent.ACTION_VIEW);
		
				String type = Utility.getMIMEType(file.getName());
	
				intent.setDataAndType(/* uri */Uri.fromFile(file), type);
		
				startActivity(intent);

			}

			@Override
			public boolean hasOperation() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public HashMap<String, Object> operationItem() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void handleOperation() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isOperation(int iconId) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onSwitchFolder() {
				// TODO Auto-generated method stub

			}

		};
	}

	private class DefaultListener implements FileBrowserListener {
		private HashSet<File> set;

		public DefaultListener() {
			// TODO Auto-generated constructor stub
			set = new HashSet<File>();
		}

		@Override
		public FileType onFilterFile(String fileName) {
			// TODO Auto-generated method stub
			FileType type = Utility.getFileType(fileName);
			if (type == FileType.Audio)
				return type;
			else if(type == FileType.Video)
				return type;

			return null;
		}

		@Override
		public void onChooseFile(File file, boolean isAll) {
			// TODO Auto-generated method stub
			if(isAll)
			{
				if (!set.contains(file)) {
					set.add(file);
				}
			}
			else {
				if (!set.contains(file)) {
					set.add(file);
				} else {
					set.remove(file);
				}
			}
		}

		@Override
		public void onSwitchFolder() {
			// TODO Auto-generated method stub

			set.clear();
		}

		@Override
		public boolean hasOperation() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isOperation(int iconId) {
			// TODO Auto-generated method stub
			return (iconId == R.drawable.forward);
		}

		@Override
		public HashMap<String, Object> operationItem() {
			// TODO Auto-generated method stub
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("Icon", R.drawable.forward);
			item.put("Name", "! ! ! 選取完成 ! ! !");
			return item;
		}

		@Override
		public void handleOperation() {
			// TODO Auto-generated method stub

			if (set.size() != 0) {
				Bundle bundle = new Bundle();
				bundle.putInt("size", set.size());

				ArrayList<String> path = new ArrayList<String>();
				ArrayList<String> name = new ArrayList<String>();
				String tmp;
				for (File f : set) {
					path.add(f.getAbsolutePath());
					tmp = f.getName();
					String newId = tmp.substring(0, tmp.lastIndexOf("."));
					name.add(newId);
				}

				bundle.putStringArrayList("path", path);
				bundle.putStringArrayList("name", name);

				Intent replyIntent = new Intent();
				replyIntent.putExtras(bundle);
				setResult(RESULT_OK, replyIntent);
			}

			FileBrowserActivity.this.finish();
		}
	}

	private class SelectedItems {
		private HashSet<View> set;

		public SelectedItems() {
			// TODO Auto-generated constructor stub
			set = new HashSet<View>();
		}

		public void chooseItem(View v, boolean isAll) {
			if(isAll)
			{
				if (!set.contains(v)) {
					v.setBackgroundResource(R.drawable.file_browser_item_bg_selected);
					set.add(v);
				}
			}
			else
			{
				if (!set.contains(v)) {
					v.setBackgroundResource(R.drawable.file_browser_item_bg_selected);
					set.add(v);
				} else {
					v.setBackgroundResource(R.drawable.file_browser_item_bg_selector);
					set.remove(v);
				}
			}
		}

		public void clear() {
			for (View v : set) {
				v.setBackgroundResource(R.drawable.file_browser_item_bg_selector);
			}

			set.clear();
		}
	}
}
