//model
User = can.Model({
	findOne : 'GET /users/details',
	destroy : 'POST /users/delete',
	findAll : 'GET /users'
}, {});



// controller
AdminUsersPage = can.Control({

	"init" : function(element, options) {
		var that = this;
		User.findAll({
			state : "failed"
		}, function(users) {
			that.element.html(can.view('/views/admin/users.ejs', users));
			$("#content").fadeIn();
		}, function(message) {
			new ErrorPage(that.element, {
				status : message.statusText,
				message : message.responseText
			});
		});

	},

	'.icon-trash click' : function(el, ev) {

		user = el.parent().parent().data('user');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to delete <b>"
				+ user.attr('username') + "</b>?", function(result) {
			if (result) {
				user.destroy();
			}
		});

	}

});