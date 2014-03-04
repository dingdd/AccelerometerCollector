package beacon.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

public class Storage {
	public static File getStorageDir(String pathName) {
		// Get the directory for the user's public pictures directory.
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
				pathName);
		if (!file.mkdirs()) {
			Log.i("accmag", "Directory not created");
		}
		return file;
	}

	public static OutputStream initStorageFiles(File dir, String filename,
			long timestamp) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(dir.getAbsolutePath() + "/" + filename
					+ "-" + timestamp + ".txt");
		} catch (FileNotFoundException e) {
			Log.i("accmag", "open error!!");
			e.printStackTrace();
		}
		return os;
	}
}
