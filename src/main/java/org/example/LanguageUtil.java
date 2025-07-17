package org.example;

import java.util.HashMap;
import java.util.Map;

public class LanguageUtil {
    private static final Map<String, Map<String, String>> translations = new HashMap<>();

    static {
        Map<String, String> uz = new HashMap<>();
        uz.put("welcome_message", "🕌 Namoz Vaqtlari Botiga xush kelibsiz!");
        uz.put("send_location_prompt", "📍 Iltimos, joylashuvingizni yuboring!");
        uz.put("prayer_times", "uchun namoz vaqtlari");
        uz.put("today", "Bugun");
        uz.put("tomorrow", "Ertaga");
        uz.put("week", "Hafta");
        uz.put("send_location", "Joylashuv yuboring");
        uz.put("location", "Joylashuv");
        uz.put("fajr", "Bomdod");
        uz.put("dhuhr", "Peshin");
        uz.put("asr", "Asr");
        uz.put("maghrib", "Shom");
        uz.put("isha", "Xufton");
        uz.put("weekly_prayer_times", "Haftalik namoz vaqtlari");
        uz.put("error_prayer_times", "❌ Namoz vaqtlarini olishda xatolik yuz berdi.");
        uz.put("unknown_command", "Noma'lum buyruq!");
        uz.put("language_changed", "🌐 Til o'zgartirildi!");
        uz.put("language_prompt", "🌐 Tilni tanlang:");
        uz.put("notifications_prompt", "🔔 Bildirishnomalarni sozlash:");
        uz.put("enable_notifications", "Yoqish");
        uz.put("disable_notifications", "O'chirish");
        uz.put("notifications_on", "🔔 Bildirishnomalar yoqildi!");
        uz.put("notifications_off", "🔕 Bildirishnomalar o'chirildi!");
        uz.put("current_time", "Hozirgi vaqt");
        uz.put("unknown_location", "Noma'lum joylashuv");
        translations.put("uz", uz);

        Map<String, String> en = new HashMap<>();
        en.put("welcome_message", "🕌 Welcome to the Prayer Times Bot!");
        en.put("send_location_prompt", "📍 Please send your location!");
        en.put("prayer_times", "prayer times for");
        en.put("today", "Today");
        en.put("tomorrow", "Tomorrow");
        en.put("week", "Week");
        en.put("send_location", "Send Location");
        en.put("location", "Location");
        en.put("fajr", "Fajr");
        en.put("dhuhr", "Dhuhr");
        en.put("asr", "Asr");
        en.put("maghrib", "Maghrib");
        en.put("isha", "Isha");
        en.put("weekly_prayer_times", "Weekly Prayer Times");
        en.put("error_prayer_times", "❌ Error fetching prayer times.");
        en.put("unknown_command", "Unknown command!");
        en.put("language_changed", "🌐 Language changed!");
        en.put("language_prompt", "🌐 Select language:");
        en.put("notifications_prompt", "🔔 Configure notifications:");
        en.put("enable_notifications", "Enable");
        en.put("disable_notifications", "Disable");
        en.put("notifications_on", "🔔 Notifications enabled!");
        en.put("notifications_off", "🔕 Notifications disabled!");
        en.put("current_time", "Current time");
        en.put("unknown_location", "Unknown location");
        translations.put("en", en);
    }

    public static String getMessage(String key, String language) {
        return translations.getOrDefault(language, translations.get("uz")).getOrDefault(key, "Unknown message");
    }
}