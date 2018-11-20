# Administration

This page helps you to set your Cloudgene instance in the so called **Maintenance Mode** to prevent users submitting jobs during system updates.

## Maintenance Mode

During system updates, you can activate the **Maintenance Mode** for preventing any users other than administrators from submitting jobs. The front page will appear as normal when your site is in maintenance mode. Users will only see the maintenance mode message when they attempt to submit a job.

Open the **Admin Panel** and click on the **Server** tab. Scroll down to the section **Maintenance Mode**. By clicking on **Enter Maintenance Mode** a new dialog appears where you can enter a message. By clicking on **OK** the Maintenance Mode is activated.

<img src="../../images/screenshots/maintenance-message.png">

Users will see this message when they attempt to submit a job.

<div class="screenshot">
<img src="../../images/screenshots/maintenance-mode.png">
</div>

Once your update is finished, click on **Exit Maintenance Mode** and your Cloudgene instance is accessible for all users.


## Blocking Queue

If you block the queue, then all running jobs are executed and all other jobs are waiting until the queue is manually opened.

Open the **Admin Panel** and click on the **Server** tab. Scroll down to the section **Queue**. By clicking on **Block Queue** the queue is blocked. All new submitted jobs are in state **waiting** until you open the queue. The queue can be opened by clicking on **Open Queue**.


## Job Retirement

You can activate [Auto Retire](configuration.md#auto-retire) in the configuration file. Once *Auto Retire* is activated, a cron job checks all completed jobs and depending on the configuration intervals it sends notification to users or deletes the results of old jobs.

However, if *Auto Retire* is deactivated, it is also possible to trigger job retirement manually by clicking on the **Start Retire** button which is located at the bottom of the **Server** tab.

Please click [here](jobs.md#retired-jobs) to learn more about retirement on a job level.
