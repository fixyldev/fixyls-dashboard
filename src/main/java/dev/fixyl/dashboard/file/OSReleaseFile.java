package dev.fixyl.dashboard.file;

import static dev.fixyl.dashboard.constant.Paths.ETC_OSRELEASE;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class OSReleaseFile extends INICompositeFile {

    public OSReleaseFile(PathResolver pathResolver) {
        super(pathResolver.resolve(ETC_OSRELEASE), Set.of("PRETTY_NAME"));
    }

    public Optional<String> getPrettyName() {
        return getValue("PRETTY_NAME");
    }

}
