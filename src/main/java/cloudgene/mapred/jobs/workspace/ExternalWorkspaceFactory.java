package cloudgene.mapred.jobs.workspace;

import cloudgene.sdk.internal.IExternalWorkspace;

public class ExternalWorkspaceFactory {

	public static IExternalWorkspace get(String type, String location) {

		if (type == null) {
			return null;
		}

		if (type.equalsIgnoreCase("S3")) {
			return new S3Workspace(location);
		}

		return null;
	}

	public static IExternalWorkspace get(String url) {

		if (url == null) {
			return null;
		}

		if (url.startsWith("s3://")) {
			return new S3Workspace("");
		}

		return null;
	}

}
