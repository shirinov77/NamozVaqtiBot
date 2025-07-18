package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NamozBot extends TelegramLongPollingBot {
    private final PrayerTimeService prayerTimeService = new PrayerTimeService();
    private final KeyboardUtil keyboardUtil = new KeyboardUtil();
    private final Map<String, double[]> userLocations = new HashMap<>(); // chatId -> [latitude, longitude]
    private final Map<String, String> userLanguages = new HashMap<>(); // chatId -> language (uz, en)
    private final Map<String, Integer> lastMessageIds = new HashMap<>(); // chatId -> last message ID
    private final Map<String, String> lastPrayerType = new HashMap<>(); // chatId -> last prayer type (today, tomorrow, week)
    private final Map<String, String> lastMessageText = new HashMap<>(); // chatId -> last message text

    public NamozBot() {
        // Ma'lumotlar bazasini boshlash
        DatabaseUtil.initDatabase();
    }

    @Override
    public String getBotUsername() {
        return "Namoz Vaqtlari Bot";
    }

    @Override
    public String getBotToken() {
        return "7556559607:AAF4SqW5faqPYbzampz0Z-Gdq1_p8CEeHbM";
    }

    public void start() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            System.out.println("Bot ishga tushdi!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                if (text.equals("/start")) {
                    handleStartCommand(update);
                } else if (text.equals("/language")) {
                    handleLanguageCommand(update);
                } else if (text.equals("/notifications")) {
                    handleNotificationsCommand(update);
                }
            } else if (update.hasMessage() && update.getMessage().hasLocation()) {
                handleLocation(update);
            } else if (update.hasMessage() && update.getMessage().hasContact()) {
                handleContact(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleStartCommand(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String language = userLanguages.getOrDefault(chatId, "uz");
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Foydalanuvchi ma'lumotlarini olish
        String username = update.getMessage().getFrom().getUserName();
        String nickname = update.getMessage().getFrom().getFirstName();
        String phoneNumber = null; // Telefon raqami keyinroq contact orqali yuborilishi mumkin

        // Foydalanuvchi ma'lumotlarini ma'lumotlar bazasiga saqlash
        DatabaseUtil.saveUser(chatId, username != null ? username : "", nickname != null ? nickname : "", phoneNumber);

        // Send welcome message
        String welcomeText = LanguageUtil.getMessage("welcome_message", language) + "\n🕒 " +
                LanguageUtil.getMessage("current_time", language) + ": " + currentTime;

        SendMessage welcomeMessage = new SendMessage();
        welcomeMessage.setChatId(chatId);
        welcomeMessage.setText(welcomeText);
        org.telegram.telegrambots.meta.api.objects.Message sentWelcome = execute(welcomeMessage);
        lastMessageIds.put(chatId, sentWelcome.getMessageId());
        lastMessageText.put(chatId, welcomeText);

        // Prompt for location and contact
        String locationPrompt = LanguageUtil.getMessage("send_location_prompt", language);
        SendMessage locationMessage = new SendMessage();
        locationMessage.setChatId(chatId);
        locationMessage.setText(locationPrompt);
        locationMessage.setReplyMarkup(keyboardUtil.getLocationButton(language));
        org.telegram.telegrambots.meta.api.objects.Message sentLocationPrompt = execute(locationMessage);
        lastMessageIds.put(chatId, sentLocationPrompt.getMessageId());
        lastMessageText.put(chatId, locationPrompt);
    }

    private void handleContact(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String phoneNumber = update.getMessage().getContact().getPhoneNumber();
        String language = userLanguages.getOrDefault(chatId, "uz");

        // Telefon raqamini ma'lumotlar bazasida yangilash
        DatabaseUtil.updateUserPhoneNumber(chatId, phoneNumber);

        // Foydalanuvchiga tasdiqlash xabari
        String confirmationText = LanguageUtil.getMessage("phone_saved", language);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(confirmationText);
        message.setReplyMarkup(keyboardUtil.getMainMenu(language));
        org.telegram.telegrambots.meta.api.objects.Message sentMessage = execute(message);
        lastMessageIds.put(chatId, sentMessage.getMessageId());
        lastMessageText.put(chatId, confirmationText);
    }

    private void handleLanguageCommand(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String language = userLanguages.getOrDefault(chatId, "uz");
        Integer messageId = lastMessageIds.get(chatId);

        String newText = LanguageUtil.getMessage("language_prompt", language);
        if (Objects.equals(newText, lastMessageText.get(chatId))) {
            return; // Avoid redundant edit
        }

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId != null ? messageId : update.getMessage().getMessageId());
        message.setText(newText);
        message.setReplyMarkup(keyboardUtil.getLanguageMenu());
        execute(message);
        lastMessageText.put(chatId, newText);
    }

    private void handleNotificationsCommand(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String language = userLanguages.getOrDefault(chatId, "uz");
        Integer messageId = lastMessageIds.get(chatId);

        String newText = LanguageUtil.getMessage("notifications_prompt", language);
        if (Objects.equals(newText, lastMessageText.get(chatId))) {
            return; // Avoid redundant edit
        }

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId != null ? messageId : update.getMessage().getMessageId());
        message.setText(newText);
        message.setReplyMarkup(keyboardUtil.getNotificationsMenu(language));
        execute(message);
        lastMessageText.put(chatId, newText);
    }

    private void handleLocation(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String language = userLanguages.getOrDefault(chatId, "uz");
        double latitude = update.getMessage().getLocation().getLatitude();
        double longitude = update.getMessage().getLocation().getLongitude();

        // Store user location
        userLocations.put(chatId, new double[]{latitude, longitude});

        // Send new message with welcome text and inline keyboard
        String welcomeText = LanguageUtil.getMessage("welcome_message", language);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(welcomeText);
        message.setReplyMarkup(keyboardUtil.getMainMenu(language));
        org.telegram.telegrambots.meta.api.objects.Message sentMessage = execute(message);
        lastMessageIds.put(chatId, sentMessage.getMessageId());
        lastMessageText.put(chatId, welcomeText);

        System.out.println("Location received for chatId " + chatId + ": [" + latitude + ", " + longitude + "]");
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String callbackData = update.getCallbackQuery().getData();
        String language = userLanguages.getOrDefault(chatId, "uz");
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        System.out.println("Callback received for chatId " + chatId + ": callbackData = " + callbackData);

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setReplyMarkup(keyboardUtil.getMainMenu(language));

        String newText;
        if (callbackData.startsWith("lang_")) {
            String newLanguage = callbackData.split("_")[1];
            userLanguages.put(chatId, newLanguage);
            newText = LanguageUtil.getMessage("language_changed", newLanguage);
            if (userLocations.containsKey(chatId)) {
                double[] coords = userLocations.get(chatId);
                String prayerType = lastPrayerType.getOrDefault(chatId, "today");
                switch (prayerType) {
                    case "today":
                        newText = prayerTimeService.getPrayerTimes(coords[0], coords[1], "today", newLanguage);
                        break;
                    case "tomorrow":
                        newText = prayerTimeService.getPrayerTimes(coords[0], coords[1], "tomorrow", newLanguage);
                        break;
                    case "week":
                        newText = prayerTimeService.getWeeklyPrayerTimes(coords[0], coords[1], newLanguage);
                        break;
                }
            }
        } else if (callbackData.startsWith("notif_")) {
            String status = callbackData.split("_")[1];
            newText = LanguageUtil.getMessage("notifications_" + status, language);
        } else if (callbackData.equals("tasbih")) {
            newText = LanguageUtil.getMessage("tasbih", language) + ":\n" +
                    "📿 Elektron tasbihni ochish uchun quyidagi havolaga o‘ting:\n" +
                    "https://your-tasbih-app.netlify.app"; // O‘zingizning URL manzilingizni qo‘ying
        } else {
            if (!userLocations.containsKey(chatId)) {
                newText = LanguageUtil.getMessage("send_location_prompt", language);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(newText);
                sendMessage.setReplyMarkup(keyboardUtil.getLocationButton(language));
                execute(sendMessage);
                lastMessageText.put(chatId, newText);
                lastPrayerType.put(chatId, callbackData);
                return;
            }
            lastPrayerType.put(chatId, callbackData);
            double[] coords = userLocations.get(chatId);
            switch (callbackData) {
                case "today":
                    newText = prayerTimeService.getPrayerTimes(coords[0], coords[1], "today", language);
                    break;
                case "tomorrow":
                    newText = prayerTimeService.getPrayerTimes(coords[0], coords[1], "tomorrow", language);
                    break;
                case "week":
                    newText = prayerTimeService.getWeeklyPrayerTimes(coords[0], coords[1], language);
                    break;
                default:
                    newText = LanguageUtil.getMessage("unknown_command", language);
            }
        }

        if (!Objects.equals(newText, lastMessageText.get(chatId))) {
            message.setText(newText);
            execute(message);
            lastMessageText.put(chatId, newText);
            lastMessageIds.put(chatId, messageId);
        } else {
            System.out.println("No update needed for chatId " + chatId + ": same text");
        }
    }
}