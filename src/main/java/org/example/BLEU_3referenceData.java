package org.example;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLEU_3referenceData {
    // Declare maxNgramSize as a global variable
    private static int maxNgramSize;
    private static int candidateLength;
    private static int referenceLength;
    private static double smooth_mteval = 1.0; // Initialize smooth_mteval
    private static double smooth_value = 0.1; // Initialize smooth_value

    public static void main(String[] args) {
        // Example candidate and reference translations
        String candidate = "It's time for? me, to meet a doctor.";
        String reference = "I have a? doctor's appointment.";

        // Calculate BLEU score
        BLEUStats bleuStats = calculateBLEUScore(candidate, reference);

        // Print BLEU score and additional statistics in the requested format
        System.out.println("{");
        System.out.println(" 'bleu': " + bleuStats.bleuScore + ",");
        System.out.println(" 'precisions': " + Arrays.toString(bleuStats.precisions) + ",");
        System.out.println(" 'brevity_penalty': " + bleuStats.brevityPenalty + ",");
        System.out.println(" 'length_ratio': " + bleuStats.lengthRatio + ",");
        System.out.println(" 'translation_length': " + bleuStats.translationLength + ",");
        System.out.println(" 'reference_length': " + bleuStats.referenceLength + ",");
        System.out.println("}");
    }

    public static BLEUStats calculateBLEUScore(String candidate, String reference) {
        // Calculate candidate and reference lengths
        candidateLength = candidate.split("\\s+|(?=[.,!?])|(?<=[.,!?])").length;
        referenceLength = reference.split("\\s+|(?=[.,!?])|(?<=[.,!?])").length;
        double brevityPenalty = computeBrevityPenalty();
        double[] precisions = computePrecisions(candidate, reference, "exp");


        double bleuScore = brevityPenalty * bleuHelper(precisions);

        // Additional statistics
        double lengthRatio = (double) candidateLength / referenceLength;

        return new BLEUStats(bleuScore, precisions, brevityPenalty, lengthRatio, candidateLength, referenceLength);
    }



    public static double[] computePrecisions(String candidate, String reference, String smoothMethod) {
        List<String> candidateTokens = Arrays.asList(candidate.toLowerCase().split("\\s+|(?=[.,!?])|(?<=[.,!?])"));
        List<String> referenceTokens = Arrays.asList(reference.toLowerCase().split("\\s+|(?=[.,!?])|(?<=[.,!?])"));

        maxNgramSize = (candidateLength >= 4 && referenceLength >= 4) ? 4 : candidateLength; // Assign value to maxNgramSize

        double[] precisions = new double[maxNgramSize];
        int[] correct = new int[maxNgramSize];
        int[] total = new int[maxNgramSize];

        for (int n = 1; n <= maxNgramSize; n++) {
            int candidateMatches = countMatches(candidateTokens, referenceTokens, n);
            int candidateNgrams = candidateTokens.size() - n + 1;

            correct[n - 1] = candidateMatches;
            total[n - 1] = candidateNgrams;

            if (correct[n - 1] == 0) {
                if (smoothMethod.equals("exp")) {
                    smooth_mteval *= 2;
                    precisions[n - 1] = 100.0 / (smooth_mteval * total[n - 1]);
                } else if (smoothMethod.equals("floor")) {
                    precisions[n - 1] = 100.0 * smooth_value / total[n - 1];
                }
            } else {
                precisions[n - 1] = 100.0 * correct[n - 1] / total[n - 1];
            }
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

    public static double computeBrevityPenalty() {
        if (candidateLength > referenceLength) {
            return 1.0;
        } else {
            return Math.exp(1.0 - ((double) referenceLength / candidateLength));
        }
    }

    public static double bleuHelper(double[] values) {
        double product = 1.0;
        for (double number : values) {
            product *= number;
        }
        double result = Math.pow(product, 1.0 / values.length);

        DecimalFormat df = new DecimalFormat("#.##");
        String formattedResult = df.format(result);

        return Double.parseDouble(formattedResult);
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
