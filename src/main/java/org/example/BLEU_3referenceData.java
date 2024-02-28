
package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLEU_3referenceData {

    public static void main(String[] args) {
        // Example candidate and reference translations
        String candidate = "Good morning! How did you sleep last night?";
        String reference = "Good morning! How was your sleep last night?";

        // Calculate BLEU score
        BLEUStats bleuStats = calculateBLEUScore(candidate, reference);

        // Print BLEU score and additional statistics in the requested format
        System.out.println("{");
        System.out.println(" 'bleu': " + bleuStats.bleuScore + ",");
        System.out.println(" 'precisions': " + Arrays.toString(bleuStats.precisions) + ",");
        System.out.println(" 'brevity_penalty': " + bleuStats.brevityPenalty + ",");
        System.out.println(" 'length_ratio': " + bleuStats.lengthRatio + ",");
        System.out.println(" 'translation_length': " + bleuStats.translationLength + ",");
        System.out.println(" 'reference_length': " + bleuStats.referenceLength);
        System.out.println("}");
    }

    public static BLEUStats calculateBLEUScore(String candidate, String reference) {
        double brevityPenalty = computeBrevityPenalty(candidate, reference);
        double[] precisions = computePrecisions(candidate, reference);
        double bleuScore = brevityPenalty * bleuHelper(precisions);

        // Additional statistics
        int candidateLength = candidate.split(" ").length;
        int referenceLength = reference.split(" ").length;
        double lengthRatio = (double) candidateLength / referenceLength;

        return new BLEUStats(bleuScore, precisions, brevityPenalty, lengthRatio, candidateLength, referenceLength);
    }

    public static double[] computePrecisions(String candidate, String reference) {
        List<String> candidateTokens = Arrays.asList(candidate.toLowerCase().split(" "));
        List<String> referenceTokens = Arrays.asList(reference.toLowerCase().split(" "));

        int maxNgramSize = 4;
        double[] precisions = new double[maxNgramSize];

        for (int n = 1; n <= maxNgramSize; n++) {
            int candidateMatches = countMatches(candidateTokens, referenceTokens, n);
            int candidateNgrams = Math.max(candidateTokens.size() - n + 1, 0);
            precisions[n - 1] = (double) candidateMatches / candidateNgrams;
        }

        return precisions;
    }


    // Count number of matching n-grams
    public static int countMatches(List<String> candidateTokens, List<String> referenceTokens, int n) {
        int countMatches = 0;
        Map<List<String>, Integer> referenceNgrams = new HashMap<>();
        for (int i = 0; i <= referenceTokens.size() - n; i++) {
            List<String> ngram = referenceTokens.subList(i, i + n);
            referenceNgrams.put(ngram, referenceNgrams.getOrDefault(ngram, 0) + 1);
        }

        for (int i = 0; i <= candidateTokens.size() - n; i++) {
            List<String> ngram = candidateTokens.subList(i, i + n);
            if (referenceNgrams.containsKey(ngram) && referenceNgrams.get(ngram) > 0) {
                countMatches++;
                referenceNgrams.put(ngram, referenceNgrams.get(ngram) - 1); // Decrease reference count
            }
        }

        return countMatches;
    }

    public static double computeBrevityPenalty(String candidate, String reference) {
        int candidateLength = candidate.split(" ").length;
        int referenceLength = reference.split(" ").length;

        if (candidateLength > referenceLength) {
            return 1.0;
        } else {
            return Math.exp(1.0 - ((double) referenceLength / candidateLength));
        }
    }

    public static double bleuHelper(double[] values) {
        double logSum = 0.0;
        for (double value : values) {
            logSum += Math.log(value);
        }
        return Math.exp((1.0/4) * logSum);
    }

    static class BLEUStats {
        double bleuScore;
        double[] precisions;
        double brevityPenalty;
        double lengthRatio;
        int translationLength;
        int referenceLength;

        public BLEUStats(double bleuScore, double[] precisions, double brevityPenalty, double lengthRatio, int translationLength, int referenceLength) {
            this.bleuScore = bleuScore;
            this.precisions = precisions;
            this.brevityPenalty = brevityPenalty;
            this.lengthRatio = lengthRatio;
            this.translationLength = translationLength;
            this.referenceLength = referenceLength;
        }
    }
}
