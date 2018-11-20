# Manage Jobs

Open the **Admin Panel** and click on the **Jobs** tab to see a list of all **running** and **completed** jobs.

<div class="screenshot">
<img src="../../images/screenshots/jobs.png">
</div>

Click on the job name to see details about the job or click on **Actions** to open a menu with all available actions.

## Queued Jobs

The following actions are available for jobs with status **waiting** and which are in one of the two queues.

  - **Add to top of queue**

    The prriority of the job is set to **0** which means that the job moves to the top of the queue and will processed as soon as the needed resources are available.

## Completed Jobs

The following actions are available for jobs with status **success**.


  - **Reset download counters**

    For security reasons, files can be downloaded 10 times (the value can be changed in the [configuration](configuration.md#downloads). In case of download errors or other reasons the number of possible downloads can be reset for all files.

  - **Deactivate download counters**

    Deactivates the download counter for all results of the selected job. Only files of this job can be downloaded unlimited times. All other jobs are unaffected. To change it globally see [configuration](configuration.md#downloads).


  - **Send retire notification**

    By clicking on this item, the notification of job retirement is sent to the user and the retire date (see column **valid until**) is set according the [configuration](configuration.md#auto-retire).

  - **Retire job now**

    By clicking on this item, the job is retired immediately **without** sending an notification to the user. Before all data of the selected job is deleted, a confirmation dialog appears.

## Retired Jobs

The following actions are available for jobs with status **success** and a **valid until** date.


  - **Increase retire date**

    If a job has a retire date (see column **valid until**), the date can be increased by clicking on this menu item. A dialog appears where the number of days of the extension can be set.
