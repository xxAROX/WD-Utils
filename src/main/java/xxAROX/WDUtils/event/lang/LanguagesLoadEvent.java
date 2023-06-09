package xxAROX.WDUtils.event.lang;

import dev.waterdog.waterdogpe.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xxAROX.WDUtils.lang.Language;

import java.util.HashMap;

@Getter @Setter @AllArgsConstructor
public class LanguagesLoadEvent extends Event {
    private HashMap<String, Language> languages;
}
