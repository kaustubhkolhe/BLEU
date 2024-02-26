package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BLEU_1referenceData {

    public static void main(String[] args) {
        String candidate = "the picture the picture by me";
        String reference = "the picture is clicked by me";

        // Compute BLEU score and additional statistics
        BLEUStatistics bleuStats = computeBLEUStatistics(candidate, reference, 4);

        // Print BLEU score and additional statistics
        System.out.println("BLEU Score: " + bleuStats.bleuScore);
        System.out.println("Precisions: " + Arrays.toString(bleuStats.precisions));
        System.out.println("Brevity Penalty: " + bleuStats.brevityPenalty);
        System.out.println("Length Ratio: " + bleuStats.lengthRatio);
        System.out.println("Translation Length: " + bleuStats.translationLength);
        System.out.println("Reference Length: " + bleuStats.referenceLength);
    }

    public static BLEUStatistics computeBLEUStatistics(String candidate, String reference, int n) {
        double[] precisions = new double[n];
        double candidateLength = candidate.split("\\s+").length;
        double referenceLength = reference.split("\\s+").length;

        for (int i = 1; i <= n; i++) {
            double clipCountSum = 0.0;
            double candidateCount = 0.0;

            // Calculate clipped counts and total candidate n-grams
            List<String> candidateNgrams = generateNgrams(candidate, i);
            List<String> referenceNgrams = generateNgrams(reference, i);

            candidateCount += candidateNgrams.size();

            for (String candidateNgram : candidateNgrams) {
                int countInReference = countOccurrences(referenceNgrams, candidateNgram);
                clipCountSum += Math.min(1, countInReference);
            }

            precisions[i - 1] = clipCountSum / candidateCount;
        }

        double brevityPenalty = brevityPenalty(candidateLength, referenceLength);
        double bleuScore = geometricMean(precisions) * brevityPenalty;
        double lengthRatio = candidateLength / referenceLength;

        return new BLEUStatistics(bleuScore, precisions, brevityPenalty, lengthRatio, candidateLength, referenceLength);
    }

    public static List<String> generateNgrams(String text, int n) {
        List<String> ngrams = new ArrayList<>();
        String[] words = text.split("\\s+");

        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append((j > i ? " " : "") + words[j]);
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }

    public static int countOccurrences(List<String> list, String target) {
        int count = 0;
        for (String element : list) {
            if (element.equals(target)) {
                count++;
            }
        }
        return count;
    }

    public static double brevityPenalty(double candidateLength, double referenceLength) {
        return Math.min(1, candidateLength / referenceLength);
    }

    public static double geometricMean(double[] numbers) {
        double product = 1.0;
        for (double number : numbers) {
            product *= number;
        }
        return Math.pow(product, 1.0 / numbers.length);
    }

    static class BLEUStatistics {
        double bleuScore;
        double[] precisions;
        double brevityPenalty;
        double lengthRatio;
        double translationLength;
        double referenceLength;

        public BLEUStatistics(double bleuScore, double[] precisions, double brevityPenalty, double lengthRatio, double translationLength, double referenceLength) {
            this.bleuScore = bleuScore;
            this.precisions = precisions;
            this.brevityPenalty = brevityPenalty;
            this.lengthRatio = lengthRatio;
            this.translationLength = translationLength;
            this.referenceLength = referenceLength;
        }
    }
}
