package xxAROX.WDUtils.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jline.internal.Nullable;
import lombok.Getter;
import lombok.NonNull;

import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class Language {
    private final String locale;
    private final String name;
    private final HashMap<String, String> translations = new HashMap<>();

    public Language(@NonNull String locale, @NonNull JsonObject json) throws InvalidKeyException {
        this.locale = locale;
        if (!json.has("name") || json.get("name").isJsonNull()) throw new InvalidKeyException("Translation 'name' not found in " + locale);
        name = json.get("name").getAsString();
        for (Map.Entry<String, JsonElement> entry : json.get("translations").getAsJsonObject().asMap().entrySet()) translations.put(entry.getKey(), entry.getValue().getAsString());
    }

    @Nullable
    public String getTranslation(String key){
        return translations.getOrDefault(key, null);
    }
}
