package imageFilter;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.Scanner;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        GaussFilter gf = null;
        File sourceImage = new File("images/source.jpg");
        File answerImage = new File("images/answer.jpg");
        System.out.print("Оберіть силу розмиття, від 1 до 100 (чим більше цифра, тим більше буде розмиття та час обробки):");
        int radius = new Scanner(System.in).nextInt();
        if (radius >= 1 && radius <= 100)
            gf = new GaussFilter(ImageIO.read(sourceImage), radius);
        else {
            System.out.println("Надано невірне значення радіусу розмиття");
            System.exit(0);
        }
        System.out.println("Оберіть бажану кількість ядер для обробки, доступно: 1\t2\t4\t6\t8\t10\t12\t14\t16");
        int choose = new Scanner(System.in).nextInt();
        switch (choose) {
            case 1 -> ImageIO.write(gf.useFilter(CORES.ONE_CORE), "JPG", answerImage);
            case 2 -> ImageIO.write(gf.useFilter(CORES.TWO_CORES), "JPG", answerImage);
            case 4 -> ImageIO.write(gf.useFilter(CORES.FOUR_CORES), "JPG", answerImage);
            case 6 -> ImageIO.write(gf.useFilter(CORES.SIX_CORES), "JPG", answerImage);
            case 8 -> ImageIO.write(gf.useFilter(CORES.EIGHT_CORES), "JPG", answerImage);
            case 10 -> ImageIO.write(gf.useFilter(CORES.TEN_CORES), "JPG", answerImage);
            case 12 -> ImageIO.write(gf.useFilter(CORES.TWELVE_CORES), "JPG", answerImage);
            case 14 -> ImageIO.write(gf.useFilter(CORES.FOURTEEN_CORES), "JPG", answerImage);
            case 16 -> ImageIO.write(gf.useFilter(CORES.SIXTEEN_CORES), "JPG", answerImage);
            default -> System.out.println("Надано невірне занчення!");
        }
    }
}
