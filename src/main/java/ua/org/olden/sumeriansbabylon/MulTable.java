package ua.org.olden.sumeriansbabylon;

/**
 * Вавілонська таблиця множення клинописом.
 * Вавілоняни мали «таблиці множення» для фіксованих множників (1–59)
 * з результатами у системі base-60.
 */
public class MulTable {

    public static void main(String[] args) {
        int maxFactor = 12; // як на сучасній таблиці множення

        System.out.println("=== Вавілонська таблиця множення (base-60, клинопис) ===");
        System.out.println();

        // Заголовок
        System.out.printf("%-6s", "×");
        for (int col = 1; col <= maxFactor; col++) {
            System.out.printf("%8d", col);
        }
        System.out.println();
        System.out.println("-".repeat(6 + maxFactor * 8));

        for (int row = 1; row <= maxFactor; row++) {
            System.out.printf("%-6d", row);
            for (int col = 1; col <= maxFactor; col++) {
                Base60 product = Base60.fromInt(row).multiply(Base60.fromInt(col));
                System.out.printf("%8s", product.toString());
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("=== Те саме клинописом ===");
        System.out.println();

        for (int row = 1; row <= maxFactor; row++) {
            for (int col = 1; col <= maxFactor; col++) {
                Base60 a = Base60.fromInt(row);
                Base60 b = Base60.fromInt(col);
                Base60 product = a.multiply(b);
                System.out.printf("  %s × %s = %s%n",
                        a.toSumerianString(),
                        b.toSumerianString(),
                        product.toSumerianString());
            }
        }
    }
}
