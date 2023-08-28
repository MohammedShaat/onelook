# OneLook ![brand logo](/docs/ic_brand_logo.png)

**OneLook** app enables the user to track his daily tasks (activities and supplements).<br><br>

It's not a traditional to-do app, since a new instance of a task is automatically created for the next day.

The task can be one of two types: *Activity* or *Supplement*. Each type has its own specifics, for example *Supplement* has expiration date, frequency, etc...

## Features:
* **Offline caching:** <br>The user can browse tasks offilne. Not only that but also create, delete, and update them.
* **Reminders:** <br>The user can set predefined alarms.
* **Synchronization:** <br>Since the tasks could be updated offline the app synchronizes them every 24-hours and when the user swipes to refresh.
* **Timer:** <br>Can be used to progress a specific activity or as an indpendent timer. It runs in background even when the app gets closed.


## Core Technologies & APIs:
* MVVM + Clean Architecture
* Firebase Auth
* Room
* Retrofit
* Dagger Hilt
* Coroutines, Flows, LiveData
* DataStore
* WorkManager, BroadcastReceiver, Service
* Notifications
* Custom View

Since there wasn't a ready API, I built a simple one myself using Laravel. [onelook-api](https://onelook-api.fly.dev/api/).


## Installation
* [apk](https://drive.google.com/file/d/1M7hSIIXIyeCLqCoxXTyA_o3J3pGKljHx/view?usp=drive_link)
* Google Play (soon)<br/>


## Demo
* Video <br/>
[![video-cover](/docs/screens/video-cover.png)](https://youtu.be/O81U_9KPX9M)

* Screenshots
![screenshots-1](/docs/screens/screenshots-1.png)
![screenshots-2](/docs/screens/screenshots-2.png)
![screenshots-3](/docs/screens/screenshots-3.png)
![screenshots-4](/docs/screens/screenshots-4.png)
![screenshots-5](/docs/screens/screenshots-5.png)
