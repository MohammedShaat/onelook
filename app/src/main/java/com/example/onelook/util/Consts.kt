package com.example.onelook.util

import android.app.Activity

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

const val DATE_TIME_FORMAT = "y-MM-dd HH:mm:ss"
const val DATE_SEVENTIES = "1970-1-1 00:00:00"

const val ACTIVITIES_TIMER_CHANNEL_ID = "activities_timer"
const val ACTIVITIES_TIMER_CHANNEL_NAME = "Activities timer"
const val NOTIFICATION_ID = 0
const val TIMER_ONGOING_NOTIFICATION_ID = NOTIFICATION_ID + 1
const val SYNC_ONGOING_NOTIFICATION_ID = TIMER_ONGOING_NOTIFICATION_ID + 1

const val TIMER_PLAYING_ACTION = "TIMER_STATUS_ACTION"
const val TIMER_VALUE_ACTION = "TIMER_UPDATES_ACTION"

const val TIMER_FRAGMENT_REQ = ONE_TAP_REQ + 1
