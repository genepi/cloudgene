package cloudgene.mapred.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import cloudgene.mapred.util.FileUtil;
import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.Settings;

public class ImporterLocalFile extends AbstractTask {
	private String filename;

	private String path;

	private boolean delete = true;

	private long size;

	private long read = 0;

	private CountingOutputStream t;

	public ImporterLocalFile(String filename, String path) {
		this(filename, path, true);
		setName("import-local");
		size = getSize();
	}

	public ImporterLocalFile(String filename, String path, boolean delete) {
		setName("import-local");
		this.filename = filename;
		this.path = path;
		this.delete = delete;
		size = getSize();
	}

	private long getSize() {
		return countFiles(filename);
	}

	@Override
	public boolean execute() {

		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			// totalFiles = countFiles(filename);
			boolean result = importIntoHdfs(filename, fileSystem, path);

			// remove temp file
			if (delete) {
				File file = new File(filename);
				file.delete();
			}

			return result;
		} catch (IOException e) {

			// remove temp file
			if (delete) {
				File file = new File(filename);
				file.delete();
			}

			writeOutput(e.getLocalizedMessage());

			return false;
		}

	}

	public long countFiles(String filename) {

		File file = new File(filename);

		if (file.isFile()) {

			return file.length();

		} else {

			long sum = 0;
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					sum += countFiles(child.getAbsolutePath());
				}
			}

			return sum;

		}

	}

	public boolean importIntoHdfs(String filename, FileSystem fileSystem,
			String path) {

		File file = new File(filename);

		if (file.isFile()) {

			if (filename.toLowerCase().endsWith(".zip")) {
				importZipFile(filename, fileSystem, path);
			} else {
				importRegularFile(filename, fileSystem, path);
			}

		} else {

			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					importRegularFile(child.getAbsolutePath(), fileSystem, path);
				}
			}

		}

		return true;
	}

	public void importZipFile(String filename, FileSystem filesystem,
			String folder) {
		try {

			ZipInputStream zipinputstream = new ZipInputStream(
					new FileInputStream(filename));

			byte[] buf = new byte[1024];
			ZipEntry zipentry = zipinputstream.getNextEntry();

			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();

				if (!zipentry.isDirectory()) {
					Settings settings = Settings.getInstance();
					String hdfsWorkspace = settings.getHdfsWorkspace(job
							.getUser().getUsername());
					writeOutput("Extracting File " + entryName + "...");

					String target = HdfsUtil.path(hdfsWorkspace, folder,
							entryName);

					FSDataOutputStream out = filesystem
							.create(new Path(target));

					int n;
					while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
						out.write(buf, 0, n);
					out.close();

					zipinputstream.closeEntry();
				}

				zipentry = zipinputstream.getNextEntry();

			}// while

			zipinputstream.close();
			writeOutput("done extracting");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importRegularFile(String filename, FileSystem filesystem,
			String folder) {
		try {

			File file = new File(filename);

			writeOutput("Importing File " + filename + "...");

			if (file.isFile()) {

				FileInputStream in = new FileInputStream(filename);

				String entryName = FileUtil.getFilename(filename);
				Settings settings = Settings.getInstance();
				String workspace = settings.getHdfsWorkspace(job.getUser()
						.getUsername());
				String target = HdfsUtil.path(workspace, folder, entryName);

				FSDataOutputStream out = filesystem.create(new Path(target));

				t = new CountingOutputStream(out);

				IOUtils.copyBytes(in, t, filesystem.getConf());

				IOUtils.closeStream(in);
				IOUtils.closeStream(out);

				read += file.length();

				System.out.println("done importing");

			} else {

				File[] files = file.listFiles();
				if (files != null) {
					for (File child : files) {
						String target = HdfsUtil.path(path, file.getName());
						importRegularFile(child.getAbsolutePath(), filesystem,
								target);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getProgress() {

		if (t != null) {

			return (int) ((read + t.getByteCount()) / (double) size * 100d);

		} else {
			return 0;
		}
	}

	@Override
	public String[] getParameters() {
		return new String[] { "Folder-Name", "Size", "Type", "Source", };
	}

	@Override
	public String[] getValues() {
		return new String[] { path, FileUtils.byteCountToDisplaySize(size),
				"Local File", filename };
	}

}
