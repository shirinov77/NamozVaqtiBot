package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardUtil {
    public InlineKeyboardMarkup getMainMenu(String language) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("📅 " + LanguageUtil.getMessage("today", language), "today"));
        row1.add(createInlineButton("📅 " + LanguageUtil.getMessage("tomorrow", language), "tomorrow"));
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("🗓 " + LanguageUtil.getMessage("week", language), "week"));
        rows.add(row2);

        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup getLocationButton(String language) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(LanguageUtil.getMessage("send_location", language));
        button.setRequestLocation(true);
        row.add(button);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;
    }

    public InlineKeyboardMarkup getLanguageMenu() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("🇺🇿 O'zbek", "lang_uz"));
        row.add(createInlineButton("🇬🇧 English", "lang_en"));
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }

    public InlineKeyboardMarkup getNotificationsMenu(String language) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("🔔 " + LanguageUtil.getMessage("enable_notifications", language), "notif_on"));
        row.add(createInlineButton("🔕 " + LanguageUtil.getMessage("disable_notifications", language), "notif_off"));
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}