package com.example.eventfinder.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    public static String getRelativeTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date past = sdf.parse(timestamp);

            if (past == null) {
                return "";
            }

            Date now = new Date();
            long seconds = (now.getTime() - past.getTime()) / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            if (seconds < 60) {
                return seconds + " second" + (seconds == 1 ? "" : "s") + " ago";
            } else if (minutes < 60) {
                return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
            } else if (hours < 24) {
                return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
            } else if (days < 7) {
                return days + " day" + (days == 1 ? "" : "s") + " ago";
            } else if (weeks < 4) {
                return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
            } else if (months < 12) {
                return months + " month" + (months == 1 ? "" : "s") + " ago";
            } else {
                return years + " year" + (years == 1 ? "" : "s") + " ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatEventDate(String date, String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsedDate = inputFormat.parse(date);

            if (parsedDate == null) {
                return date + (time != null ? " " + time : "");
            }

            SimpleDateFormat outputFormat;
            SimpleDateFormat currentYear = new SimpleDateFormat("yyyy", Locale.US);
            String eventYear = date.substring(0, 4);
            String currentYearStr = currentYear.format(new Date());

            if (eventYear.equals(currentYearStr)) {
                outputFormat = new SimpleDateFormat("MMM d", Locale.US);
            } else {
                outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            }

            String formattedDate = outputFormat.format(parsedDate);

            if (time != null && !time.isEmpty()) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                Date parsedTime = timeFormat.parse(time);
                if (parsedTime != null) {
                    SimpleDateFormat timeOutputFormat = new SimpleDateFormat("h:mm a", Locale.US);
                    formattedDate += ", " + timeOutputFormat.format(parsedTime);
                }
            }

            return formattedDate;
        } catch (Exception e) {
            return date + (time != null ? " " + time : "");
        }
    }
}