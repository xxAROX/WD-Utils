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
            // https://minecraft.fandom.com/wiki/Language
            "af_ZA", // Afrikaans
            "ar_SA", // Arabic
            "ast_ES", // Asturian
            "az_AZ", // Azerbaijani
            "bg_BG", // Bulgarian
            "bn_BD", // Bengali
            "bs_BA", // Bosnian
            "ca_ES", // Catalan
            "cs_CZ", // Czech
            "cy_GB", // Welsh
            "da_DK", // Danish
            "de_DE", // German
            "el_GR", // Greek
            "en_AU", // English, Australian
            "en_CA", // English, Canadian
            "en_GB", // English, British
            "en_NZ", // English, New Zealand
            "en_PT", // English, Pirate
            "en_UD", // English, upside down
            "en_US", // English, US
            "eo_UY", // Esperanto
            "es_AR", // Spanish, Argentine
            "es_CL", // Spanish, Chilean
            "es_ES", // Spanish, Spain
            "es_MX", // Spanish, Mexican
            "et_EE", // Estonian
            "eu_ES", // Basque
            "fa_IR", // Farsi
            "fi_FI", // Finnish
            "fil_PH", // Filipino
            "fo_FO", // Faroese
            "fr_CA", // French, Canadian
            "fr_FR", // French, France
            "fy_NL", // Frisian
            "ga_IE", // Irish
            "gd_GB", // Scottish Gaelic
            "gl_ES", // Galician
            "gu_IN", // Gujarati
            "he_IL", // Hebrew
            "hi_IN", // Hindi
            "hr_HR", // Croatian
            "hu_HU", // Hungarian
            "hy_AM", // Armenian
            "id_ID", // Indonesian
            "ig_NG", // Igbo
            "io_EN", // Ido
            "is_IS", // Icelandic
            "it_IT", // Italian
            "ja_JP", // Japanese
            "jv_ID", // Javanese
            "ka_GE", // Georgian
            "kk_KZ", // Kazakh
            "km_KH", // Khmer
            "kn_IN", // Kannada
            "ko_KR", // Korean
            "ku_TR", // Kurdish
            "la_LA", // Latin
            "lb_LU", // Luxembourgish
            "lo_LA", // Lao
            "lt_LT", // Lithuanian
            "lv_LV", // Latvian
            "mg_MG", // Malagasy
            "mi_NZ", // Maori
            "mk_MK", // Macedonian
            "ml_IN", // Malayalam
            "mn_MN", // Mongolian
            "mr_IN", // Marathi
            "ms_MY", // Malay
            "mt_MT", // Maltese
            "nb_NO", // Norwegian Bokmål
            "ne_NP", // Nepali
            "nl_NL", // Dutch
            "nn_NO", // Norwegian Nynorsk
            "no_NO", // Norwegian
            "nso_ZA", // Northern Sotho
            "oc_FR", // Occitan
            "or_IN", // Oriya
            "pa_IN", // Punjabi
            "pl_PL", // Polish
            "pt_BR", // Portuguese, Brazil
            "pt_PT", // Portuguese, Portugal
            "qu_PE", // Quechua
            "ro_RO", // Romanian
            "ru_RU", // Russian
            "sc_IT", // Sardinian
            "se_NO", // Northern Sami
            "sk_SK", // Slovak
            "sl_SI", // Slovenian
            "sq_AL", // Albanian
            "sr_RS", // Serbian
            "sv_SE", // Swedish
            "sw_KE", // Swahili
            "ta_IN", // Tamil
            "te_IN", // Telugu
            "th_TH", // Thai
            "tl_PH", // Tagalog
            "tr_TR", // Turkish
            "tt_RU", // Tatar
            "udm_RU", // Udmurt
            "uk_UA", // Ukrainian
            "ur_PK", // Urdu
            "uz_UZ", // Uzbek
            "vi_VN", // Vietnamese
            "xh_ZA", // Xhosa
            "yi_DE", // Yiddish
            "yo_NG", // Yoruba
            "zh_CN", // Chinese, Simplified
            "zh_HK", // Chinese, Hong Kong
            "zh_TW", // Chinese, Traditional
            "zu_ZA", // Zulu
    }).toList();

    @Setter @Getter private String owner;
    @Setter @Getter private String repository;
    @Setter @Getter private String branch;
    @Setter @Getter @Nullable private String access_token;
    @Setter @Getter private Integer timeout = 5000;

    private final MainLogger logger;
    @Getter private Language fallback = null;
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
        if (access_token == null) {
            String message = "Language repository should be public or an access token should be provided!";
            commandSender.sendMessage(message);
            if (!(commandSender instanceof ConsoleCommandSender)) logger.debug(message);
            return;
        }
        boolean first_time = languages.size() == 0;
        String reloading = (first_time ? "Loading languages" : "Reloading languages") + " from " + owner + "/" + repository + "@" + branch;
        logger.info(reloading);
        if (!(commandSender instanceof ConsoleCommandSender)) logger.debug(reloading);
        fetchLanguages(commandSender, first_time);
    }

    private void fetchLanguages(CommandSender commandSender, boolean first_time){
        AtomicReference<String> raw = new AtomicReference<>(url_raw.replace("{branch}", branch).replace("{owner}", owner).replace("{repository}", repository));
        String token = (access_token != null ? "?token=" + access_token : "");
        ProxyServer.getInstance().getScheduler().scheduleAsync(() -> {
            try {
                String raw_locales = getUrl(new URL(raw.get() + ".loader.json" + token), timeout);
                if (raw_locales.contains("404: Not Found")) {
                    commandSender.sendMessage("§c§oError while loading locales: File '.loader.json' not found in repository root!");
                    return;
                }
                HashMap<String, Language> langs = new HashMap<>();
                List<String> locales = new Gson().fromJson(raw_locales, JsonArray.class).asList().stream().map(JsonElement::getAsString).toList();
                for (String locale : locales) {
                    String lang = getUrl(new URL(raw + locale + ".json" + token), timeout);
                    langs.put(locale, new Language(locale, new Gson().fromJson(lang, JsonObject.class)));
                }
                languages.clear();
                languages.putAll(langs);
            } catch (IOException | InvalidKeyException e) {
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

    public void setFallback(Language fallback) throws InvalidKeyException {
        if (languages.containsKey(fallback.getLocale())) register(fallback);
        this.fallback = fallback;
    }

    public boolean isRegistered(String locale){
        return languages.containsKey(locale);
    }

    public String translate(@Nullable CommandSender target, @NonNull String key, @NonNull Map<String, String> replacements) {
        Language language = ((target instanceof ProxiedPlayer) ? (this.languages.get(((ProxiedPlayer) target).getLoginData().getClientData().get("LanguageCode").getAsString())) : null);
        if (language == null) language = fallback;
        String translation = language.getTranslation(key);
        if (translation == null) {
            String defaultTranslation = fallback.getTranslation(key);
            if (defaultTranslation == null) {
                logger.error("Unknown translation key " + key);
                return "";
            } else translation = defaultTranslation;
        }
        for (Map.Entry<String, String> entry : replacements.entrySet()) translation = translation.replace(entry.getKey(), entry.getValue());
        return translation;
    }

    public String translate(@Nullable CommandSender target, @NonNull String key) {
        return translate(target, key, new HashMap<>());
    }

    private String getUrl(URL url, int timeout) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
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
