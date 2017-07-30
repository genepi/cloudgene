TabsAdmin = can.Control({

    "init": function(element, options) {
        this.page = null;
    },

    'pages/:page route': function(data) {
        this.activate(data.page);
        this.show(data.page);
    },

    'jobs/:job/results route': function(data) {
        this.activate('jobs');
        this.options.detailsPage = new JobDetailsPage("#content", {
            jobId: data.job,
            results: true
        });
        this.page = this.options.detailsPage;
    },

    'jobs/:job route': function(data) {
        this.activate('jobs');
        this.options.detailsPage = new JobDetailsPage("#content", {
            jobId: data.job,
            admin: true,
            results: false
        });
        this.page = this.options.detailsPage;
    },

    activate: function(id) {

        // stop and destroy page
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

    show: function(id) {

        switch (id) {

            case "admin-jobs":
                this.page = new AdminJobsPage("#content");
                break;

            case "admin-users":
                this.page = new AdminUsersPage("#content");
                break;

            case "admin-home":
                this.page = new AdminHomePage("#content");
                break;

            case "admin-piggene":
                $("#content").hide();
                $("#content").html(can.view('views/admin/piggene.ejs'));
                $("#content").fadeIn();

                break;

            case "admin-logs":
                $("#content").hide();
                $("#content").html(can.view('views/admin/logs.ejs'));
                $("#content").fadeIn();
                $("#log-cloudgene").load("/api/v2/admin/server/logs/cloudgene.log");
                $("#log-access").load("/api/v2/admin/server/logs/access.log");
                $("#log-jobs").load("/api/v2/admin/server/logs/jobs.log",
                    function(response, status, xhr) {

                        if (status == "error") {
                            var msg = "Sorry but there was an error: ";
                            $("#content").hide().html(
                                can.view('views/error.ejs', {
                                    error: {
                                        statusText: xhr.statusText,
                                        responseText: xhr.responseText
                                    }
                                }));
                            $("#content").fadeIn();
                        }

                    });
                break;

            case "admin-server":
                this.page = new AdminServerPage("#content");
                break;

            case "admin-settings":
                this.page = new AdminSettingsPage("#content");
                break;

            case "admin-apps":
                this.page = new AdminAppsPage("#content");
                break;

            case "admin-templates":
                this.page = new AdminTemplatesPage("#content");
                break;


            default:
                this.page = new Error404Page("#content");
                break;

        }

    }
});
