package com.example.onelook.util

import android.app.Activity

const val GLOBAL_TAG = "GlobalTag"


const val ONE_TAP_REQ = Activity.RESULT_FIRST_USER
const val PASSWORD_REST_EMAIL_REQ_KEY = "password_reset_email"
const val ADD_SUPPLEMENT_REQ_KEY = "supplement_added_successfully"
const val SUPPLEMENT_NAME_KEY = "supplement_name"
const val ADD_ACTIVITY_REQ_KEY = "activity_added_successfully"
const val ACTIVITY_TYPE_KEY = "activity_name"
const val UPDATE_SUPPLEMENT_REQ_KEY = "supplement_updated_successfully"
const val UPDATE_ACTIVITY_REQ_KEY = "activity_updated_successfully"
const val DELETE_SUPPLEMENT_REQ_KEY = "supplement_deleted_successfully"
const val DELETE_ACTIVITY_REQ_KEY = "activity_deleted_successfully"
const val CHANGE_NAME_REQ_KEY = "change_name"

const val DATE_TIME_FORMAT = "y-MM-dd HH:mm:ss"
const val TIME_FORMAT = "HH:mm"
const val DATE_SEVENTIES = "1970-1-1 00:00:00"

const val ACTIVITIES_TIMER_CHANNEL_ID = "activities_timer"
const val REMINDERS_CHANNEL_ID = "reminders_timer"
const val NOTIFICATION_ID = 1
const val TIMER_ONGOING_NOTIFICATION_ID = NOTIFICATION_ID + 1

const val ACTION_TIMER_PLAYING = "ACTION_TIMER_PLAYING"
const val ACTION_TIMER_VALUE = "ACTION_TIMER_VALUE"
const val ACTION_OPEN_TIMER = "ACTION_OPEN_TIMER"
const val ACTION_OPEN_SUPPLEMENT_NOTIFICATION = "ACTION_OPEN_SUPPLEMENT_NOTIFICATION"
const val ACTION_OPEN_ACTIVITY_NOTIFICATION = "ACTION_OPEN_ACTIVITY_NOTIFICATION"

const val OPEN_TIMER_REQ = ONE_TAP_REQ + 1
const val NOTIFICATION_TASK_REQ = OPEN_TIMER_REQ + 1

const val REMINDER_TIME_ADDITION = 1    // 10