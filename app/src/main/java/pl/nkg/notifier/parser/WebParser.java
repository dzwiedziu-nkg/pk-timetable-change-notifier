package pl.nkg.notifier.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebParser {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy", new Locale("pl", "PL"));
    private final static String PARSE_START_TAG = "<b>STUDIA NIESTACJONARNE</b>";
    private final static String PARSE_STAGE_I_TAG = "Kierunek: <i>Informatyka I stopień i II stopień";
    private final static String PARSE_STAGE_II_TAG = "Kierunek: <i>Informatyka I stopień i II stopień";

    public ParsedData parse(BufferedReader reader) throws IOException, ParseException {
        String line;
        int automaton = 0;
        ParsedData parsedData = new ParsedData();
        int done = 0;

        while ((line = reader.readLine()) != null && done != 3) {
            switch (automaton) {
                case 0:
                    if (line.contains(PARSE_START_TAG)) {
                        automaton = 1;
                    }
                    break;

                case 1:
                    if (line.contains(PARSE_STAGE_I_TAG)) {
                        parsedData.setFirstStage(parseLine(line));
                        done |= 1;
                    } else if (line.contains(PARSE_STAGE_II_TAG)) {
                        parsedData.setSecondStage(parseLine(line));
                        done |= 2;
                    }
            }
        }

        return parsedData;
    }

    private static ParsedEntity parseLine(String line) throws MalformedURLException, ParseException {
        ParsedEntity parsedEntity = new ParsedEntity();
        Pattern p = Pattern.compile("a href=\"([^\"]+)\">.*?(\\d{2}-\\d{2}-\\d{4})");
        Matcher m = p.matcher(line);
        if (m.find()) {
            parsedEntity.setUrl(new URL(m.group(1)));
            parsedEntity.setDate(DATE_FORMAT.parse(m.group(2)));
        }
        return parsedEntity;
    }
}
