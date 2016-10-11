TabsPublic = can.Control({

	"init" : function(element, options) {
		this.page = null;
	},

	'activate/:user/:key route' : function(data) {
		this.activate('register');

		$.ajax({
			url : "users/activate/" + data.user + "/" + data.key,
			type : "GET",
			data : $(this).serialize(),
			dataType : 'json',
			success : function(data) {
				$("#content").hide().html(can.view('views/activate.ejs', {
					data : data
				})).fadeIn();
			},
			error : function(message) {
				alert('failure: ' + message);
			}

		});
	},

	'run/:app route' : function(data) {
		
		this.activate('run');	
		this.page = new SubmitJobPage("#content", {tool: data.app});
	},
	
	
	'pages/:page route' : function(data) {
		this.activate(data.page);
		this.show(data.page);
	},

	'recovery/:user/:key route' : function(data) {
		this.activate('register');
		this.show('recovery', data);
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
			admin: false,
			results : false
		});
		this.page = this.options.detailsPage;
	},


	activate : function(id) {

		// destroy active page
		if (this.page != null) {
			this.page.destroy();
		}

		this.activePage = id;

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

	show : function(id, data) {

		switch (id) {
		case "home":
			this.page = new HomePage("#content", {
				login : true
			});
			break;

		case "activate":

			this.page = new StaticPage("#content", {
				template : 'views/activate.ejs'
			});
			break;

		case "register":
			this.page = new RegisterUserPage("#content");
			break;
			
		case "login":
			this.page = new UserLoginForm("#content");
			break;			

		case "reset-password":
			this.page = new ResetPasswordPage("#content");
			break;

		case "recovery":
			this.page = new NewPasswordPage("#content", {data:data});
			break;

		case "help":
			this.page = new StaticPage("#content", {
				template : 'static/help.ejs'
			});
			break;

		case "contact":
			this.page = new StaticPage("#content", {
				template : 'static/contact.ejs'
			});
			break;

		case "run":
		case "jobs":
			this.page = new ErrorPage("#content", {
				status : "401",
				message : "The request requires user authentication."
			});
			break;

		default:
			this.page = new StaticPage("#content", {template: 'static/'+ id + '.ejs'});
			break;
		}

	}
});
