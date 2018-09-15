import can from 'can/legacy';

export default can.Model({
  findOne: 'GET /api/v2/users/{user}/profile',
  destroy: 'POST /api/v2/admin/users/delete',
  update: 'POST /api/v2/admin/users/changegroup',
  findAll: 'GET /api/v2/admin/users'
}, {

  'checkPassword': function(password, confirm_password) {

    if (password === "" || password != confirm_password) {
      return "Please check your passwords.";
    }

    if (password.length < 6) {
      return "Password must contain at least six characters!";
    }

    var re = /[0-9]/;
    if (!re.test(password)) {
      return "Password must contain at least one number (0-9)!";
    }

    re = /[a-z]/;
    if (!re.test(password)) {
      return "Password must contain at least one lowercase letter (a-z)!";
    }

    re = /[A-Z]/;
    if (!re.test(password)) {
      return "Password must contain at least one uppercase letter (A-Z)!";
    }
  },

  'checkUsername': function(username) {
    if (username === "") {
      return "The username is required.";
    }

    if (username.length < 4) {
      return "The username must contain at least four characters.";
    }

    var pattern = new RegExp(/^[a-zA-Z0-9]+$/);
    if (!pattern.test(username)) {
      return "Your username is not valid. Only characters A-Z, a-z and digits 0-9 are acceptable.";
    }
  },

  'checkName': function(name) {
    if (name === "") {
      return "The full name is required.";
    }
  },

  'checkMail': function(mail) {

    if (mail === "") {
      return "E-Mail is required.";
    }

    var pattern2 = new RegExp(
      /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
    if (!pattern2.test(mail)) {
      return "Please enter a valid mail address.";
    }
  }

});
