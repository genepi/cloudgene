# Administration Tasks

This page helps you to set your Cloudgene instance in the so called **Maintenance Mode** to prevent users submitting jobs during system updates.

## Maintenance Mode

During system updates, you can activate the **Maintenance Mode** for preventing any users other than administrators from submitting jobs. The front page will appear as normal when your site is in maintenance mode. Users will only see the maintenance mode message when they attempt to submit a job.

Open the **Admin Panel** and click on the **Server** tab. Scroll down to the section **Maintenance Mode**. By clicking on **Enter Maintenance Mode** a new dialog appears where you can enter a message. Users will see this message when they attempt to submit a job.

Once your update is finished, click on **Exit Maintenance Mode** and your Cloudgene instance is accessible for all users.


## Blocking Queue

If you block the queue, then all running jobs are executed and all other jobs are waiting until the queue is manually opened.

Open the **Admin Panel** and click on the **Server** tab. Scroll down to the section **Queue**. By clicking on **Block Queue** the queue is blocked. You can open the queue by clicking on **Open Queue**.


## Job Retirement

Please click [here](jobs.md) to learn more about jobs and the auto retire features.
