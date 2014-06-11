package cloudgene.mapred.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipUtil {

	public static boolean extract(String filename, String folder) {

		try {

			ZipInputStream zipinputstream = new ZipInputStream(
					new FileInputStream(filename));

			byte[] buf = new byte[1024];
			ZipEntry zipentry = zipinputstream.getNextEntry();

			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();

				if (!zipentry.isDirectory()) {

					String target = FileUtil.path(folder, entryName);

					File file = new File(target);
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}

					FileOutputStream out = new FileOutputStream(file);

					int n;
					while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
						out.write(buf, 0, n);
					out.close();

					zipinputstream.closeEntry();
				}

				zipentry = zipinputstream.getNextEntry();

			}// while

			zipinputstream.close();
			System.out.println("done extracting");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
