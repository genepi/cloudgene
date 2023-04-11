import Model from 'can-connect/can/model/model';

export default Model.extend({
    destroy: function(user) {

        return $.ajax({
            url: "/api/v2/users/" + user.username + "/profile",
            type: "DELETE",
            data: {
                "username": user.username,
                "password": user.password
            }

        })
    }
}, {

});
