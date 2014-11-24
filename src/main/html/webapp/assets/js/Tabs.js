
Tabs = can.Control({

	"init" : function(element, options) {
		this.page = null;
	},

	'run/:app route' : function(data) {
		
		console.log("ldsdsadsa");
		this.activate('run');	
		this.page = new SubmitJobPage("#content", {tool: data.app});
	},
	
	
	'pages/:page route' : function(data) {
		this.activate(data.page);
		this.show(data.page);
	},

	'jobs/:job/results route' : function(data) {
		this.activate('jobs');
		this.options.detailsPage = new JobDetailsPage("#content", {
			jobId : data.job,
			results : true
		});
		this.page = this.options.detailsPage;
	},
	
	'jobs/:job route' : function(data) {
		this.activate('jobs');
		this.options.detailsPage = new JobDetailsPage("#content", {
			jobId : data.job,
			results : false
		});
		this.page = this.options.detailsPage;
	},
	

	'recovery/:user/:key route' : function(data) {
		this.page = new ErrorPage("#content", {
			status : "27",
			message : "Please log out for password recovery."
		});
	},
	
	'activate/:user/:key route' : function(data) {
		this.page = new ErrorPage("#content", {
			status : "27",
			message : "Please log out for user activation."
		});
	},	

	activate : function(id) {

		// destroy active page
		if (this.page != null) {
			this.page.destroy();
		}

		// activated li
		this.element.find('li').each(function() {
			li = $(this);
			li.removeClass('active', '');
			$(this).find('a').each(function() {
				if ($(this).attr('id') == id) {
					li.addClass('active');
				}
			});
		});

	},

	show : function(id) {

		switch (id) {
		case "run":
			this.page = new SubmitJobPage("#content");
			break;

		case "jobs":
			this.options.jobs = new JobListPage("#content");
			this.page = this.options.jobs;
			break;

		case "home":
			this.page = new HomePage("#content", {login: false});
			break;

		case "help":
			this.page = new StaticPage("#content", {template: '/static/help.ejs'});
			break;

		case "contact":
			this.page = new StaticPage("#content", {template: '/static/contact.ejs'});
			break;

		case "profile":
			this.page = new UpdateProfilePage("#content");
			break;

		default:
			this.page = new Error404Page("#content");
			break;

		}

	}
});