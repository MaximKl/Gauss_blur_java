package imageFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GaussFilter {
    private BufferedImage sourceImage;
    private int radius;
    private double[][] weights;
    private BufferedImage answer;
    private long startTime;
    public GaussFilter(BufferedImage sourceImage, int radius) {
        this.sourceImage = sourceImage;
        this.radius = radius;
        this.weights = generateWeightMatrix(radius, Math.sqrt(150));
        this.answer = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    }
    public BufferedImage useFilter(CORES core) throws InterruptedException {
        System.out.println("\t\tРозмір зображення: " + sourceImage.getWidth() + "x" + sourceImage.getHeight());
        System.out.println(Thread.currentThread().getName() + " головний тред запускає фільтер\n");
        startTime = System.nanoTime();
        List<Thread> newThreads = new ArrayList<>();
        switch (core) {
            case ONE_CORE -> newThreads = createThreads(1);
            case TWO_CORES -> newThreads = createThreads(2);
            case FOUR_CORES -> newThreads = createThreads(4);
            case SIX_CORES -> newThreads = createThreads(6);
            case EIGHT_CORES -> newThreads = createThreads(8);
            case TEN_CORES -> newThreads = createThreads(10);
            case TWELVE_CORES -> newThreads = createThreads(12);
            case FOURTEEN_CORES -> newThreads = createThreads(14);
            case SIXTEEN_CORES -> newThreads = createThreads(16);
        }
        for (Thread t : newThreads)
            t.join();
        System.out.println("\n"+Thread.currentThread().getName() + " головний потік завершує роботу");
        System.out.println("Загальний час роботи: " + (System.nanoTime() - startTime) / 1_000_000_000 + " секунд");
        return answer;
    }
    private List<Thread> createThreads(final int tQuantity) {
        int count = tQuantity;
        int width = sourceImage.getWidth() / tQuantity;
        int from, to;
        List<Thread> threads = new ArrayList<>();
        while (count != 0) {
            from = width * (count - 1);
            to = width * count;
            if (count == tQuantity && count * width != sourceImage.getWidth())
                to = sourceImage.getWidth();
            Thread t = new BlurImage(from, 0, to, sourceImage.getHeight());
            t.setName("Потік #" + count);
            threads.add(t);
            count--;
        }
        return threads;
    }
    private double[][] generateWeightMatrix(int radius, double variance) {
        System.out.println("\t\tВагова матриця");
        double[][] weight = new double[radius][radius];
        double summation = 0;
        for (int i = 0; i < weight.length; i++) {
            for (int j = 0; j < weight[i].length; j++) {
                weight[i][j] = gaussianModel(i - radius / 2, j - radius / 2, variance);
                summation += weight[i][j];

                System.out.print(weight[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("--------------------------");
        for (int i = 0; i < weight.length; i++) {
            for (int j = 0; j < weight[i].length; j++) {
                weight[i][j] /= summation;
            }
        }
        return weight;
    }
    private int getWightedColorValue(double[][] weightedColor) {
        double summation = 0;
        for (int i = 0; i < weightedColor.length; i++) {
            for (int j = 0; j < weightedColor[i].length; j++) {
                summation += (weightedColor[i][j]);
            }
        }
        return (int) summation;
    }
    private double gaussianModel(double x, double y, double variance) {
        return (1 /
                (2 * Math.PI * Math.pow(variance, 2)) * Math.exp(-(Math.pow(x, 2) + Math.pow(y, 2)) /
                (2 * Math.pow(variance, 2))));
    }
    private class BlurImage extends Thread {
        int fromWidth;
        int fromHeight;
        int toWidth;
        int toHeight;
        long localStartTime;
        int opCounter;
        private BlurImage(int fromW, int fromH, int toW, int toH) {
            this.fromWidth = fromW;
            this.fromHeight = fromH;
            this.toWidth = toW;
            this.toHeight = toH;
            this.start();
        }
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " почав роботу\t" + "\tВідповідальний за частину з " + fromWidth + "x" + fromHeight + "\tдо\t" + toWidth + "x" + toHeight);
            localStartTime = System.nanoTime();
            blur();
            System.out.println(Thread.currentThread().getName() + " закінчує роботу" + "\t\tЧас роботи: " + (System.nanoTime() - localStartTime) / 1_000_000_000 + " секунд" + "\t\tПікселів оброблено потоком: " + opCounter);
        }
        private void blur() {
            for (int x = fromWidth; x < toWidth; x++) {
                for (int y = fromHeight; y < toHeight; y++) {
                    double[][] distributedColorRed = new double[radius][radius];
                    double[][] distributedColorGreen = new double[radius][radius];
                    double[][] distributedColorBlue = new double[radius][radius];
                    for (int weightX = 0; weightX < weights.length; weightX++) {
                        for (int weightY = 0; weightY < weights[weightX].length; weightY++) {
                            int sampleX = x + weightX - (weights.length / 2);
                            int sampleY = y + weightY - (weights.length / 2);

                            if (sampleX > sourceImage.getWidth() - 1) {
                                int errorOffset = sampleX - (sourceImage.getWidth() - 1);
                                sampleX = (sourceImage.getWidth() - 1) - errorOffset;
                            }

                            if (sampleY > sourceImage.getHeight() - 1) {
                                int errorOffset = sampleY - (sourceImage.getHeight() - 1);
                                sampleY = (sourceImage.getHeight() - 1) - errorOffset;
                            }
                            if (sampleX < 0) {
                                sampleX = Math.abs(sampleX);
                            }
                            if (sampleY < 0) {
                                sampleY = Math.abs(sampleY);
                            }
                            double currentWeight = weights[weightX][weightY];

                            Color sampleColor = new Color(sourceImage.getRGB(sampleX, sampleY));
                            distributedColorRed[weightX][weightY] = currentWeight * sampleColor.getRed();
                            distributedColorGreen[weightX][weightY] = currentWeight * sampleColor.getGreen();
                            distributedColorBlue[weightX][weightY] = currentWeight * sampleColor.getBlue();
                        }
                    }
                    answer.setRGB(x, y,
                            new Color(getWightedColorValue(distributedColorRed),
                                    getWightedColorValue(distributedColorGreen),
                                    getWightedColorValue(distributedColorBlue)).getRGB());
                    opCounter++;
                }
            }
        }
    }
}
