package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

public class PrayerTimeService {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();
    private final Map<String, String> cityCache = new HashMap<>(); // Cache for coordinates -> city name

    public String getPrayerTimes(double latitude, double longitude, String day, String language) {
        try {
            String date = day.equals("today") ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String url = String.format("http://api.aladhan.com/v1/timings/%s?latitude=%f&longitude=%f&method=2",
                    date, latitude, longitude);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(java.time.Duration.ofSeconds(10)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body()).getJSONObject("data").getJSONObject("timings");
            String city = getCityName(latitude, longitude, language);
            return formatPrayerTimes(json, city, day, language);
        } catch (IOException | InterruptedException e) {
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
                String url = String.format("http://api.aladhan.com/v1/timings/%s?latitude=%f&longitude=%f&method=2",
                        date, latitude, longitude);
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(java.time.Duration.ofSeconds(10)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject json = new JSONObject(response.body()).getJSONObject("data").getJSONObject("timings");
                result.append(formatDayPrayerTimes(json, startDate.plusDays(i).toString(), language));
            }
            result.append(String.format("\n📍 %s: %s", LanguageUtil.getMessage("location", language), city));
            return result.toString();
        } catch (IOException | InterruptedException e) {
            return LanguageUtil.getMessage("error_prayer_times", language);
        }
    }

    private String getCityName(double latitude, double longitude, String language) {
        String cacheKey = latitude + "," + longitude;
        if (cityCache.containsKey(cacheKey)) {
            return cityCache.get(cacheKey);
        }

        try {
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", latitude, longitude);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "NamozBot/1.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            String city = json.getJSONObject("address").optString("city", "");
            if (city.isEmpty()) {
                city = json.getJSONObject("address").optString("town", "");
            }
            if (city.isEmpty()) {
                city = json.getJSONObject("address").optString("village", LanguageUtil.getMessage("unknown_location", language));
            }
            cityCache.put(cacheKey, city);
            return city;
        } catch (IOException | InterruptedException e) {
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
                LanguageUtil.getMessage("location", language), city);
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