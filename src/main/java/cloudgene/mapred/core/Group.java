package cloudgene.mapred.core;

import java.util.List;
import java.util.Vector;

public  class Group {

		private String name;

		private List<String> apps = new Vector<String>();

		public Group(String name) {
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setApps(List<String> apps) {
			this.apps = apps;
		}

		public List<String> getApps() {
			return apps;
		}
		
		public void addApp(String app){
			apps.add(app);
		}

		@Override
		public boolean equals(Object obj) {
			Group g = (Group) obj;
			return g.getName().toLowerCase().equals(getName().toLowerCase());
		}
	}