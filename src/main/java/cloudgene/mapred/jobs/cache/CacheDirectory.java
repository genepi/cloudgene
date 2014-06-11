package cloudgene.mapred.jobs.cache;

import java.util.HashMap;
import java.util.Map;

import cloudgene.mapred.database.CacheDao;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.engine.graph.GraphNode;
import cloudgene.mapred.util.HashUtil;

public class CacheDirectory {

	private CacheDao dao;

	public CacheDirectory(CacheDao dao) {
		this.dao = dao;
	}

	public boolean isCached(GraphNode node, CloudgeneContext context) {

		String signature = calculateSignature(node, context);
		CacheEntry entry = dao.findBySignature(signature);
		return entry != null;

	}

	public void restore(GraphNode node, CloudgeneContext context) {

		String signature = calculateSignature(node, context);
		CacheEntry entry = dao.findBySignature(signature);

		if (entry != null) {

			// update all outputs with stored paths
			Map<String, String> oldPaths = restorOutputString(entry.getOutput());
			for (String output : node.getOutputs()) {
				String oldValue = context.get(output);
				String value = oldPaths.get(output);
				context.println("    " + output + ": " + oldValue + " -> "
						+ value);
				context.setOutput(output, value);
			}

			// update chache entry
			int used = entry.getUsed();
			used++;
			entry.setUsed(used);
			entry.setLastUsedOn(System.currentTimeMillis());
			dao.update(entry);

		}

	}

	public void addToCache(GraphNode node, CloudgeneContext context) {

		String signature = calculateSignature(node, context);
		long size = calculateSize(node, context);
		String output = createOutputString(node, context);

		CacheEntry entry = new CacheEntry();
		entry.setCreatedOn(System.currentTimeMillis());
		entry.setExecutionTime(node.getExecutionTime());
		entry.setLastUsedOn(0);
		entry.setOutput(output);

		entry.setSignature(signature);
		entry.setSize(size);
		entry.setUsed(0);
		entry.setUser(null);

		dao.insert(entry);

	}

	protected String calculateSignature(GraphNode node, CloudgeneContext context) {
		String signature = "";

		for (String input : node.getInputs()) {

			String value = context.get(input);
			/*
			 * CloudgeneParameter parameter = context.getParameter(input); if
			 * (parameter.getType().equals(WdlParameter.HDFS_FILE) ||
			 * parameter.getType().equals(WdlParameter.HDFS_FOLDER)) {
			 * 
			 * Configuration config = new Configuration(); try { FileSystem fs =
			 * FileSystem.get(config); FileChecksum sum = fs.getFileChecksum(new
			 * Path(value)); System.out.println(sum.toString());
			 * 
			 * } catch (Exception e) {
			 * 
			 * }
			 * 
			 * signature += value;
			 * 
			 * } else if (parameter.getType().equals(WdlParameter.LOCAL_FILE) ||
			 * parameter.getType().equals(WdlParameter.LOCAL_FOLDER)) {
			 * 
			 * } else {
			 */

			signature += value;

			// }
		}

		return HashUtil.getMD5(signature);
	}

	protected long calculateSize(GraphNode node, CloudgeneContext context) {

		// calculate size

		int size = 0;

		for (String output : node.getOutputs()) {
			size += 0;
		}

		return size;

	}

	protected String createOutputString(GraphNode node, CloudgeneContext context) {

		String outputString = "";
		for (String output : node.getOutputs()) {
			outputString += output + "=" + context.get(output) + " ";
		}
		return outputString;

	}

	protected Map<String, String> restorOutputString(String outputString) {

		Map<String, String> params = new HashMap<String, String>();

		String[] tiles = outputString.split(" ");

		for (String tile : tiles) {
			if (!tile.trim().isEmpty()) {
				String[] tiles2 = tile.split("=");
				params.put(tiles2[0], tiles2[1]);
			}
		}

		return params;

	}

}
