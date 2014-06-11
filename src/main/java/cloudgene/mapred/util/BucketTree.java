package cloudgene.mapred.util;

import java.util.List;
import java.util.Vector;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.multi.StorageServiceEventListener;
import org.jets3t.service.multi.ThreadedStorageService;
import org.jets3t.service.multi.event.CopyObjectsEvent;
import org.jets3t.service.multi.event.CreateBucketsEvent;
import org.jets3t.service.multi.event.CreateObjectsEvent;
import org.jets3t.service.multi.event.DeleteObjectsEvent;
import org.jets3t.service.multi.event.DownloadObjectsEvent;
import org.jets3t.service.multi.event.GetObjectHeadsEvent;
import org.jets3t.service.multi.event.GetObjectsEvent;
import org.jets3t.service.multi.event.ListObjectsEvent;
import org.jets3t.service.multi.event.LookupACLEvent;
import org.jets3t.service.multi.event.UpdateACLEvent;
import org.jets3t.service.security.AWSCredentials;

public class BucketTree {

	public static BucketItem[] getBucketTree(String key, String secret,
			String name) throws ServiceException {

		BucketItem[] results = null;

		AWSCredentials awsCredentials = null;

		if (key != null && !key.isEmpty() && secret != null
				&& !secret.isEmpty()) {

			awsCredentials = new AWSCredentials(key, secret);

		}

		try {

			ThreadedStorageService s3Service = new ThreadedStorageService(
					new RestS3Service(awsCredentials),
					new StorageServiceEventListener() {

						@Override
						public void event(DownloadObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(UpdateACLEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(LookupACLEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(GetObjectHeadsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(GetObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(DeleteObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(CreateBucketsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(CopyObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(CreateObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void event(ListObjectsEvent arg0) {
							// TODO Auto-generated method stub

						}
					});

			if (name.isEmpty()) {
				StorageBucket[] buckets = s3Service.getStorageService()
						.listAllBuckets();
				results = new BucketItem[buckets.length];
				int count = 0;
				for (StorageBucket bucket : buckets) {
					results[count] = new BucketItem();
					results[count].setId(bucket.getName());
					String temp = "";
					for (String key2 : bucket.getMetadataMap().keySet()){
						temp += " " + key2;
					}
					results[count].setText(temp);
					results[count].setPath("");
					results[count].setLeaf(false);
					count++;
				}
			} else {

				name = name.replaceAll("s3n://", "");

				String[] tiles = name.split("/", 2);

				String bucketName = tiles[0];
				String directory = "";
				if (tiles.length > 1) {
					directory = tiles[1];
				}
				StorageObject[] objects = null;
				String[] temp = null;

				if (!directory.isEmpty()) {

					if (!directory.endsWith("/")) {
						directory = directory + "/";
					}

					StorageObjectsChunk chunk = s3Service.getStorageService()
							.listObjectsChunked(bucketName, directory, "/",
									100, null, true);

					temp = chunk.getCommonPrefixes();

					objects = chunk.getObjects();

				} else {

					StorageObjectsChunk chunk = s3Service.getStorageService()
							.listObjectsChunked(bucketName, null, "/", 100,
									null, true);

					temp = chunk.getCommonPrefixes();

					objects = chunk.getObjects();

				}

				// merges both lists and removes duplicates

				List<String> keys = new Vector<String>();
				List<Boolean> leafs = new Vector<Boolean>();
				for (String object : temp) {
					if (!object.equals(directory)) {
						String[] splits = object.split("/");
						String newKey = splits[splits.length - 1];
						if (!keys.contains(newKey)) {
							keys.add(newKey);
							leafs.add(!object.endsWith("/"));
						}
					}
				}
				for (StorageObject object : objects) {
					if (!object.getKey().equals(directory)) {
						String[] splits = object.getKey().split("/");
						String newKey = splits[splits.length - 1];
						if (!keys.contains(newKey)) {
							keys.add(newKey);
							leafs.add(object.getContentLength() > 0);
						}
					}
				}

				results = new BucketItem[keys.size()];

				for (int i = 0; i < keys.size(); i++) {
					String key2 = keys.get(i);
					results[i] = new BucketItem();
					results[i].setId(bucketName + "/" + directory + key2);
					results[i].setText(key2);
					results[i].setPath("");
					results[i].setLeaf(leafs.get(i));
				}

			}

		} catch (S3ServiceException e) {
			// e.printStackTrace();
			return null;
		}

		return results;

	}
}
