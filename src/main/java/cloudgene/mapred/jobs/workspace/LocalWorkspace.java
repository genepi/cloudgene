package cloudgene.mapred.jobs.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.HashUtil;
import genepi.io.FileUtil;

public class LocalWorkspace implements IWorkspace {

	private static final String OUTPUT_DIRECTORY = "outputs";

	private static final String INPUT_DIRECTORY = "input";

	private static final String TEMP_DIRECTORY = "temp";

	private static final String LOGS_DIRECTORY = "logs";

	private static final Logger log = LoggerFactory.getLogger(LocalWorkspace.class);

	private String location;

	private String workspace;

	public LocalWorkspace(String location) {
		this.location = absolute(location);
	}

	@Override
	public String getName() {
		return "Local Workspace";
	}

	@Override
	public void setup(String job) throws IOException {
		workspace = FileUtil.path(location, job);
		log.info("Init workspace " + workspace);
		FileUtil.createDirectory(workspace);
	}

	@Override
	public String upload(String id, File file) throws IOException {
		String folder = FileUtil.path(workspace, id);
		FileUtil.createDirectory(folder);
		String target = FileUtil.path(folder, file.getName());
		log.info("Copy file " + file.getAbsolutePath() + " to " + target);
		FileUtil.copy(file.getAbsolutePath(), target);
		return target;
	}

	@Override
	public String uploadInput(String id, File file) throws IOException {
		return upload(FileUtil.path(INPUT_DIRECTORY, id), file);
	}

	@Override
	public InputStream download(String path) throws IOException {
		String absolutePath = path;
		if (!absolutePath.startsWith("/")) {
			absolutePath = FileUtil.path(location, path);
		}
		File file = new File(absolutePath);
		if (file.exists()) {
			return new FileInputStream(file);
		} else {
			throw new IOException("File '" + path + "' not found in workspace.");
		}
	}

	@Override
	public boolean exists(String path) throws IOException {
		String absolutePath = path;
		if (!absolutePath.startsWith("/")) {
			absolutePath = FileUtil.path(location, path);
		}
		File file = new File(absolutePath);
		return file.exists();
	}

	@Override
	public void delete(String job) throws IOException {

		try {
			log.debug("Deleting " + job + " on local workspace...");
			String workspace = FileUtil.path(location, job);
			FileUtil.deleteDirectory(workspace);

			log.debug("Deleted all files on local workspace for job " + job + ".");

		} catch (Exception e) {
			log.error("Deleting " + job + " failed.", e);
			throw new IOException("Deleting " + job + " failed.", e);
		}

	}

	@Override
	public void cleanup(String job) throws IOException {

		// TODO: add flag to disable cleanup (e.g. debugging)

		try {
			log.debug("Cleanup " + job + " on local workspace...");
			String temp = FileUtil.path(location, job, TEMP_DIRECTORY);
			FileUtil.deleteDirectory(temp);

			String inputs = FileUtil.path(location, job, INPUT_DIRECTORY);
			FileUtil.deleteDirectory(inputs);

			log.debug("Deleted all files on local workspace for job " + job + ".");

		} catch (Exception e) {
			log.error("Deleting " + job + " failed.", e);
			throw new IOException("Deleting " + job + " failed.", e);
		}

	}

	@Override
	public String createPublicLink(String url) {
		return null;
	}

	@Override
	public String getParent(String url) {
		return new File(url).getParent();
	}

	@Override
	public String createFolder(String id) {
		String folder = FileUtil.path(workspace, id);
		FileUtil.createDirectory(folder);
		return folder;
	}

	@Override
	public String createFile(String parent, String id) {
		String folder = FileUtil.path(workspace, parent);
		FileUtil.createDirectory(folder);
		return FileUtil.path(folder, id);
	}

	@Override
	public String createLogFile(String id) {
		String folder = FileUtil.path(workspace, LOGS_DIRECTORY);
		FileUtil.createDirectory(folder);
		return FileUtil.path(folder, id);
	}

	@Override
	public String createTempFolder(String id) {
		String folder = FileUtil.path(workspace, TEMP_DIRECTORY, id);
		FileUtil.createDirectory(folder);
		return folder;
	}

	@Override
	public List<Download> getDownloads(String url) {
		File folder = new File(url);
		List<Download> downloads = new Vector<Download>();
		exportFolder("", folder, downloads);
		return downloads;
	}

	private void exportFolder(String prefix, File folder, List<Download> downloads) {

		if (!folder.exists()) {
			return;
		}

		if (folder.isFile()) {
			Download download = createDownload(prefix, folder);
			downloads.add(download);
			return;
		}

		File[] files = folder.listFiles();

		for (File file : files) {
			if (folder.isFile()) {
				Download download = createDownload(prefix, file);
				downloads.add(download);
			} else {
				exportFolder(prefix.equals("") ? file.getName() : prefix + "/" + file.getName(), file, downloads);
			}
		}

	}

	protected Download createDownload(String prefix, File file) {
		String filename = prefix.equals("") ? file.getName() : prefix + "/" + file.getName();
		String size = FileUtils.byteCountToDisplaySize(file.length());
		String hash = HashUtil.getSha256(filename + size + (Math.random() * 100000));
		Download download = new Download();
		download.setName(filename);
		download.setPath(relative(file.getAbsolutePath()));
		download.setSize(size);
		download.setHash(hash);
		return download;
	}

	protected String relative(String absolute) {
		Path pathAbsolute = Paths.get(absolute);
		Path pathRoot = Paths.get(location);
		Path pathRelative = pathRoot.relativize(pathAbsolute);
		return pathRelative.toString();
	}

	protected String absolute(String path) {
		return new File(path).getAbsolutePath();
	}

	@Override
	public List<Download> getLogs() {
		String location = FileUtil.path(workspace, LOGS_DIRECTORY);
		return getDownloads(location);
	}

}
