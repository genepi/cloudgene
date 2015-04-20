//model
User = can.Model({
	findOne : 'GET /users/details',
	destroy : 'POST /users/delete',
	findAll : 'GET /users'
}, {});

// controller
AdminUsersPage = can
		.Control({

			"init" : function(element, options) {
				var that = this;
				User.findAll({
					state : "failed"
				}, function(users) {
					that.element
							.html(can.view('/views/admin/users.ejs', users));
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

			},

			'.icon-pencil click' : function(el, ev) {

				user = el.parent().parent().data('user');
				var oldText = user.attr('role');
				bootbox
						.confirm(
								'<h4> Change role of '
										+ user.attr('username')
										+ '</h4><form><input type="text"class="field span2" id="message" name="message" value="'
										+ oldText + '">'
										+ '</imput></form>', function(result) {
									if (result) {
										var text = $('#message').val();
										user.attr('role', text);
										user.save();
									}
								});

			}

		});