package cloudgene.mapred.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PageUtil {

	public static JSONObject createPageObject(int page, int pageSize, int count) {

		JSONObject object = new JSONObject();
		object.put("count", count);
		if (count != 0) {
			object.put("page", page);
			int pageCount = (count + pageSize - 1) / pageSize;
			if (page > 1) {
				object.put("prev", page - 1);
			}

			if (page < pageCount) {
				object.put("next", page + 1);
			}

			JSONArray pages = new JSONArray();
			for (int i = 3; i > 0; i--) {
				if (page - i > 0) {
					pages.add(page - i);
				}
			}
			pages.add(page);
			for (int i = 1; i <= 3; i++) {
				if (page + i <= pageCount) {
					pages.add(page + i);
				}
			}
			object.put("pages", pages);
		} else {
			object.put("page", 0);
			JSONArray pages = new JSONArray();
			object.put("pages", pages);
		}

		return object;
	}

}
