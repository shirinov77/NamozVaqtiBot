package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

public class PrayerTimeService {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(1)).build();
    private final Map<String, String> cityCache = new HashMap<>();

    public String getPrayerTimes(double latitude, double longitude, String day, String language) {
        try {
            String date = day.equals("today") ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String latStr = String.format(Locale.US, "%.6f", latitude);
            String lonStr = String.format(Locale.US, "%.6f", longitude);
            String url = String.format("http://api.aladhan.com/v1/timings/%s?latitude=%s&longitude=%s&method=2&school=1",
                    date, latStr, lonStr);
            System.out.println("Requesting prayer times from: " + url);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(java.time.Duration.ofSeconds(15)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API response status: " + response.statusCode());
            System.out.println("API response body: " + response.body());

            JSONObject json = new JSONObject(response.body()).getJSONObject("data").getJSONObject("timings");
            String city = getCityName(latitude, longitude, language);
            return formatPrayerTimes(json, city, day, language);
        } catch (IOException e) {
            System.out.println("IOException in getPrayerTimes: " + e.getMessage());
            return LanguageUtil.getMessage("error_prayer_times", language);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException in getPrayerTimes: " + e.getMessage());
            return LanguageUtil.getMessage("error_prayer_times", language);
        } catch (Exception e) {
            System.out.println("Unexpected error in getPrayerTimes: " + e.getMessage());
            return LanguageUtil.getMessage("error_prayer_times", language);
        }
    }

    public String getWeeklyPrayerTimes(double latitude, double longitude, String language) {
        try {
            StringBuilder result = new StringBuilder(LanguageUtil.getMessage("weekly_prayer_times", language) + "\n\n");
            LocalDate startDate = LocalDate.now();
            String city = getCityName(latitude, longitude, language);
            for (int i = 0; i < 7; i++) {
                String date = startDate.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
                String latStr = String.format(Locale.US, "%.6f", latitude);
                String lonStr = String.format(Locale.US, "%.6f", longitude);
                String url = String.format("http://api.aladhan.com/v1/timings/%s?latitude=%s&longitude=%s&method=2&school=1",
                        date, latStr, lonStr);
                System.out.println("Requesting weekly prayer times from: " + url);
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(java.time.Duration.ofSeconds(15)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("API response status: " + response.statusCode());
                System.out.println("API response body: " + response.body());

                JSONObject json = new JSONObject(response.body()).getJSONObject("data").getJSONObject("timings");
                result.append(formatDayPrayerTimes(json, startDate.plusDays(i).toString(), language));
            }
            result.append(String.format("\n📍 %s: %s", LanguageUtil.getMessage("location", language), city));
            return result.toString();
        } catch (IOException e) {
            System.out.println("IOException in getWeeklyPrayerTimes: " + e.getMessage());
            return LanguageUtil.getMessage("error_prayer_times", language);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException in getWeeklyPrayerTimes: " + e.getMessage());
            return LanguageUtil.getMessage("error_prayer_times", language);
        } catch (Exception e) {
            System.out.println("Unexpected error in getWeeklyPrayerTimes: " + e.getMessage());
            return LanguageUtil.getMessage("error_prayer_times", language);
        }
    }

    private String getCityName(double latitude, double longitude, String language) {
        String cacheKey = latitude + "," + longitude;
        if (cityCache.containsKey(cacheKey)) {
            return cityCache.get(cacheKey);
        }

        try {
            String latStr = String.format(Locale.US, "%.6f", latitude);
            String lonStr = String.format(Locale.US, "%.6f", longitude);
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s", latStr, lonStr);
            System.out.println("Requesting city name from: " + url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "NamozBot/1.0")
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Nominatim response status: " + response.statusCode() + ", Body: " + response.body());

            JSONObject json = new JSONObject(response.body());
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                String errorMessage = error.getString("message");
                System.out.println("Nominatim error: " + errorMessage);
                return LanguageUtil.getMessage("unknown_location", language);
            }
            String city = json.getJSONObject("address").optString("city", "");
            if (city.isEmpty()) {
                city = json.getJSONObject("address").optString("town", "");
            }
            if (city.isEmpty()) {
                city = json.getJSONObject("address").optString("village", LanguageUtil.getMessage("unknown_location", language));
            }
            cityCache.put(cacheKey, city);
            return city;
        } catch (IOException e) {
            System.out.println("IOException in getCityName: " + e.getMessage());
            return LanguageUtil.getMessage("unknown_location", language);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException in getCityName: " + e.getMessage());
            return LanguageUtil.getMessage("unknown_location", language);
        } catch (Exception e) {
            System.out.println("Unexpected error in getCityName: " + e.getMessage());
            return LanguageUtil.getMessage("unknown_location", language);
        }
    }

    private String formatPrayerTimes(JSONObject timings, String city, String day, String language) {
        String dateLabel = day.equals("today") ? LanguageUtil.getMessage("today", language)
                : LanguageUtil.getMessage("tomorrow", language);
        return String.format("🕌 %s %s:\n" +
                        "🌅 %s: %s\n" +
                        "☀️ %s: %s\n" +
                        "🌄 %s: %s\n" +
                        "🌇 %s: %s\n" +
                        "🌙 %s: %s\n" +
                        "📍 %s: %s",
                dateLabel, LanguageUtil.getMessage("prayer_times", language),
                LanguageUtil.getMessage("fajr", language), timings.getString("Fajr"),
                LanguageUtil.getMessage("dhuhr", language), timings.getString("Dhuhr"),
                LanguageUtil.getMessage("asr", language), timings.getString("Asr"),
                LanguageUtil.getMessage("maghrib", language), timings.getString("Maghrib"),
                LanguageUtil.getMessage("isha", language), timings.getString("Isha"),
                LanguageUtil.getMessage("location", language), city.isEmpty() ? LanguageUtil.getMessage("unknown_location", language) : city);
    }

    private String formatDayPrayerTimes(JSONObject timings, String date, String language) {
        return String.format("🗓 %s:\n" +
                        "🌅 %s: %s\n" +
                        "☀️ %s: %s\n" +
                        "🌄 %s: %s\n" +
                        "🌇 %s: %s\n" +
                        "🌙 %s: %s\n\n",
                date,
                LanguageUtil.getMessage("fajr", language), timings.getString("Fajr"),
                LanguageUtil.getMessage("dhuhr", language), timings.getString("Dhuhr"),
                LanguageUtil.getMessage("asr", language), timings.getString("Asr"),
                LanguageUtil.getMessage("maghrib", language), timings.getString("Maghrib"),
                LanguageUtil.getMessage("isha", language), timings.getString("Isha"));
    }
}