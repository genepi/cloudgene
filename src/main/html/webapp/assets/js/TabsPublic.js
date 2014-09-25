TabsPublic = can.Control({

	"init" : function(element, options) {
		this.page = null;
	},

	'activate/:user/:key route' : function(data) {
		this.activate('register');

		$.ajax({
			url : "/users/activate/" + data.user + "/" + data.key,
			type : "GET",
			data : $(this).serialize(),
			dataType : 'json',
			success : function(data) {
				$("#content").hide().html(can.view('/views/activate.ejs', {
					data : data
				})).fadeIn();
			},
			error : function(message) {
				alert('failure: ' + message);
			}

		});
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

	'jobs/:job route' : function(data) {
		this.page = new ErrorPage("#content", {
			status : "401",
			message : "The request requires user authentication."
		});
	},

	'jobs/:job/results route' : function(data) {
		this.page = new ErrorPage("#content", {
			status : "401",
			message : "The request requires user authentication."
		});
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
			li.attr('class', '');
			$(this).find('a').each(function() {
				if ($(this).attr('id') == id) {
					li.attr('class', 'active');
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
				template : '/views/activate.ejs'
			});
			break;

		case "register":
			this.page = new RegisterUserPage("#content");
			break;

		case "reset-password":
			this.page = new ResetPasswordPage("#content");
			break;

		case "recovery":
			this.page = new NewPasswordPage("#content");
			break;

		case "help":
			this.page = new StaticPage("#content", {
				template : '/static/help.ejs'
			});
			break;

		case "contact":
			this.page = new StaticPage("#content", {
				template : '/static/contact.ejs'
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
			this.page = new Error404Page("#content");
			break;
		}

	}
});
