package xxAROX.WDUtils.lang;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.ConsoleCommandSender;
import dev.waterdog.waterdogpe.logger.MainLogger;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.utils.types.TranslationContainer;
import jline.internal.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import xxAROX.WDUtils.event.lang.LanguagesLoadEvent;
import xxAROX.WDUtils.util.Permissions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@ToString
public final class LanguageManager {
    private static final String url_raw = "https://raw.githubusercontent.com/{owner}/{repository}/{branch}/";
    public static final List<String> MINECRAFT_LOCALES = Arrays.stream(new String[]{
            // See  ->  https://github.com/Mojang/bedrock-samples/blob/main/resource_pack/texts/language_names.json
            "en_US", // English (United States)
            "en_GB", // English (United Kingdom)
            "de_DE", // Deutsch (Deutschland)
            "es_ES", // Español (España)
            "es_MX", // Español (México)
            "fr_FR", // Français (France)
            "fr_CA", // Français (Canada)
            "it_IT", // Italiano (Italia)
            "ja_JP", // 日本語 (日本)
            "ko_KR", // 한국어 (대한민국)
            "pt_BR", // Português (Brasil)
            "pt_PT", // Português (Portugal)
            "ru_RU", // Русский (Россия)
            "zh_CN", // 中文(简体)
            "zh_TW", // 中文(繁體)
            "nl_NL", // Nederlands (Nederland)
            "bg_BG", // Български (България)
            "cs_CZ", // Čeština (Česko)
            "da_DK", // Dansk (Danmark)
            "el_GR", // Ελληνικά (Ελλάδα)
            "fi_FI", // Suomi (Suomi)
            "hu_HU", // Magyar (Magyarország)
            "id_ID", // Indonesia (Indonesia)
            "nb_NO", // Norsk bokmål (Norge)
            "pl_PL", // Polski (Polska)
            "sk_SK", // Slovenčina (Slovensko)
            "sv_SE", // Svenska (Sverige)
            "tr_TR", // Türkçe (Türkiye)
            "uk_UA", // Українська (Україна)
    }).toList();

    @Setter @Getter private String owner;
    @Setter @Getter private String repository;
    @Setter @Getter private String branch;
    @Setter @Getter @Nullable private String access_token;
    @Setter @Getter private Integer timeout = 5000;

    private final MainLogger logger;
    @Getter @Setter private String fallback = "en_US";
    @Getter private final HashMap<String, Language> languages = new HashMap<>();


    public LanguageManager(@NonNull String owner, @NonNull String repository, @NonNull String branch, @Nullable String access_token) {
        logger = ProxyServer.getInstance().getLogger();
        this.owner = owner;
        this.repository = repository;
        this.branch = branch;
        this.access_token = access_token;
        reload(ProxyServer.getInstance().getConsoleSender());
    }
    public LanguageManager(String owner, String repository, String branch) {
        this(owner, repository, branch, null);
    }
    public LanguageManager(String owner, String repository) {
        this(owner, repository, "main", null);
    }

    public void reload(CommandSender commandSender){
        if (!commandSender.hasPermission(Permissions.lang_reload)) {
            commandSender.sendMessage(new TranslationContainer("waterdog.command.permission.failed"));
            return;
        }
        if (owner == null || repository == null) {
            String message = "Repository credentials are not set!";
            commandSender.sendMessage(message);
            if (!(commandSender instanceof ConsoleCommandSender)) logger.warning(message);
            return;
        }
        boolean first_time = languages.size() == 0;
        String reloading = (first_time ? "L" : "Rel") + "oading languages from " + getFullRepository();
        commandSender.sendMessage(reloading);
        if (!(commandSender instanceof ConsoleCommandSender)) logger.debug(reloading);

        if (first_time && access_token == null) {
            String message = "Language repository should be public or an access token should be provided!";
            commandSender.sendMessage("§c§o" + message);
            if (!(commandSender instanceof ConsoleCommandSender)) logger.warning(message);
        }
        fetchLanguages(commandSender, first_time);
    }

    private void fetchLanguages(CommandSender commandSender, boolean first_time){
        final String raw = url_raw.replace("{branch}", branch).replace("{owner}", owner).replace("{repository}", repository);
        ProxyServer.getInstance().getScheduler().scheduleAsync(() -> {
            try {
                String raw_locales = getUrl(new URL(raw + ".loader.json"), timeout);
                if (raw_locales.contains("404: Not Found")) {
                    commandSender.sendMessage("§c§oError while loading locales: File '" + getRepositoryFileUrl(".loader.json") + "' not found!");
                    return;
                }
                HashMap<String, Language> langs = new HashMap<>();
                List<String> locales = new Gson().fromJson(raw_locales, JsonArray.class).asList().stream().map(JsonElement::getAsString).toList();
                for (String locale : locales) {
                    try {
                        String lang = getUrl(new URL(raw + locale + ".json"), timeout);
                        if (lang.contains("404: Not Found")) {
                            commandSender.sendMessage("§c§oError while loading " + locale + ": File '" + getRepositoryFileUrl(locale + ".json") + "' not found!");
                            continue;
                        }
                        JsonObject json = new Gson().fromJson(lang, JsonObject.class);
                        if (!(json.has("name") && !json.get("name").isJsonNull())) {
                            commandSender.sendMessage("§c§oError while loading " + locale + ": Translation key 'name' not found in '" + getRepositoryFileUrl(locale + ".json") + "'!");
                            continue;
                        }
                        langs.put(locale, new Language(locale, new Gson().fromJson(lang, JsonObject.class)));
                    } catch (Throwable e) {
                        commandSender.sendMessage("§o§cError: " + e.getMessage());
                    }
                }
                languages.clear();
                languages.putAll(langs);
                ProxyServer.getInstance().getEventManager().callEvent(new LanguagesLoadEvent(languages));
            } catch (IOException e) {
                commandSender.sendMessage("§o§cError: " + e.getMessage());
            } finally {
                String reloaded = (first_time ? "Loaded" : "Reloaded") + " " + languages.size() + " language" + (languages.size() == 1 ? "" : "s") + "!";
                logger.info(reloaded);
                if (!(commandSender instanceof ConsoleCommandSender)) logger.debug(reloaded);
            }
        });
    }

    public void register(Language language) throws InvalidKeyException {
        register(language, false);
    }

    public void register(Language language, boolean override) throws InvalidKeyException {
        if (!MINECRAFT_LOCALES.contains(language.getLocale())) throw new InvalidKeyException("Language "+ language.getLocale() +" is not available in minecraft");
        if (!override && isRegistered(language.getLocale())) throw new InvalidKeyException("Trying to overwrite an already registered Language");
        languages.put(language.getLocale(), language);
    }

    public boolean isRegistered(String locale){
        return languages.containsKey(locale);
    }

    public String translate(@Nullable CommandSender target, @NonNull String key, @NonNull Map<String, String> replacements) {
        Language language = target instanceof ProxiedPlayer ? this.languages.get(((ProxiedPlayer) target).getLoginData().getClientData().get("LanguageCode").getAsString()) : languages.get(getFallback());
        if (language == null) language = languages.get(fallback);
        if (language == null) {
            logger.error("Unknown fallback language " + fallback);
            return key;
        }
        String translation = language.getTranslation(key);
        if (translation == null) {
            logger.error("Unknown translation key " + key);
            return key;
        }
        for (Map.Entry<String, String> entry : replacements.entrySet()) translation = translation.replace(entry.getKey(), entry.getValue());
        return translation;
    }

    public String translate(@Nullable CommandSender target, @NonNull String key) {
        return translate(target, key, new HashMap<>());
    }

    public String getFullRepository(){
        return owner + "/" + repository + "@" + branch;
    }

    public String getRepositoryUrl(){
        return "https://github.com/" + owner + "/" + repository + "/tree/" + branch;
    }

    public String getRepositoryFileUrl(@NonNull String filename){
        return "https://github.com/" + owner + "/" + repository + "/blob/" + branch + "/" + filename;
    }

    private String getUrl(URL url, int timeout) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        if (access_token != null) con.setRequestProperty("Authorization" , "token " + access_token);
        con.setConnectTimeout(timeout);

        int status = con.getResponseCode();
        InputStreamReader streamReader;
        if (status > 299) streamReader = new InputStreamReader(con.getErrorStream());
        else streamReader = new InputStreamReader(con.getInputStream());
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();
        con.disconnect();
        return content.toString();
    }
}
