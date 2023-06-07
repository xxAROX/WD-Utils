package xxAROX.WDUtils.event.lang;

import dev.waterdog.waterdogpe.event.Event;
import lombok.Getter;
import lombok.Setter;
import xxAROX.WDUtils.lang.Language;

import java.util.HashMap;

@Getter @Setter
public class LanguagesLoadEvent extends Event {
    private HashMap<String, Language> languages;

    public LanguagesLoadEvent(HashMap<String, Language> languages) {
        this.languages = languages;
    }
}
