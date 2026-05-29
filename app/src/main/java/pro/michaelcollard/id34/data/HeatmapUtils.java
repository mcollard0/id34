package pro.michaelcollard.id34.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class HeatmapUtils {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "from", "has", "have", "i",
            "if", "in", "into", "is", "it", "its", "of", "on", "or", "that", "the", "their", "then",
            "there", "these", "this", "to", "was", "were", "will", "with", "you", "your"
    ));

    private HeatmapUtils() {
    }

    public static List<String> extractCleanWords(String content) {
        String normalized = content == null ? "" : content.toLowerCase(Locale.US).replaceAll("[^a-z0-9\\s#]", " ");
        String[] parts = normalized.split("\\s+");
        List<String> out = new ArrayList<>();
        for (String part : parts) {
            if (part.length() < 2) {
                continue;
            }
            if (STOP_WORDS.contains(part)) {
                continue;
            }
            out.add(part);
        }
        return out;
    }

    public static List<HeatWord> computeHeatmap(List<Idea> ideas) {
        Map<String, Integer> frequencies = new HashMap<>();
        int max = 1;
        for (Idea idea : ideas) {
            if (idea.deleted == 1) {
                continue;
            }
            for (String word : extractCleanWords(idea.content)) {
                int count = frequencies.containsKey(word) ? frequencies.get(word) + 1 : 1;
                frequencies.put(word, count);
                if (count > max) {
                    max = count;
                }
            }
        }
        List<HeatWord> words = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            int heat = Math.max(1, Math.min(6, (int) Math.ceil((entry.getValue() * 6.0f) / max)));
            words.add(new HeatWord(entry.getKey(), entry.getValue(), heat));
        }
        words.sort((a, b) -> Integer.compare(b.count, a.count));
        return words;
    }
}
