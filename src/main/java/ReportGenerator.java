import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportGenerator {
    private static final String RESOURCES_DIR = "src/main/resources/";

    static class TaskRunnable implements Runnable {
        private final String path;
        private double totalCost;
        private int totalAmount;
        private int totalDiscountSum;
        private int totalLines;
        private Product mostExpensiveProduct;
        private double highestCostAfterDiscount;
        private int mostExpensiveAmount;

        public TaskRunnable(String path) {
            this.path = Objects.requireNonNull(path, "Order file path cannot be null");
        }

        @Override
        public void run() {
            try {
                List<String> lines = Files.readAllLines(Paths.get(path));
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(",");
                    if (parts.length != 3) {
                        System.err.println("Invalid line format in " + path + ": " + line);
                        continue;
                    }

                    try {
                        int productId = Integer.parseInt(parts[0].trim());
                        int amount = Integer.parseInt(parts[1].trim());
                        int discount = Integer.parseInt(parts[2].trim());

                        if (amount < 0 || discount < 0) {
                            System.err.println("Negative value in " + path + ": " + line);
                            continue;
                        }

                        Product product = findProduct(productId);
                        if (product == null) {
                            System.err.println("Product not found in " + path + ": " + productId);
                            continue;
                        }

                        double costBeforeDiscount = product.getPrice() * amount;
                        double costAfterDiscount = Math.max(0, costBeforeDiscount - discount);

                        totalCost += costAfterDiscount;
                        totalAmount += amount;
                        totalDiscountSum += discount;
                        totalLines++;

                        if (costAfterDiscount > highestCostAfterDiscount) {
                            highestCostAfterDiscount = costAfterDiscount;
                            mostExpensiveProduct = product;
                            mostExpensiveAmount = amount;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Number format error in " + path + ": " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + path + " - " + e.getMessage());
            }
        }

        public void makeReport() {
            System.out.println("\n Report for " + Path.of(path).getFileName() );
            System.out.printf("Total cost: %.2f%n", totalCost);
            System.out.println("Total items bought: " + totalAmount);

            double avgDiscount = totalLines > 0 ? (double)totalDiscountSum / totalLines : 0;
            System.out.printf("Average discount: %.2f%n", avgDiscount);

            if (mostExpensiveProduct != null) {
                System.out.println("Most expensive purchase after discount:");
                System.out.printf("- Product: %s (ID: %d)%n",
                        mostExpensiveProduct.getProductName(), mostExpensiveProduct.getProductID());
                System.out.printf("- Amount: %d%n", mostExpensiveAmount);
                System.out.printf("- Total cost after discount: %.2f%n", highestCostAfterDiscount);
            }
        }
    }

    static class Product {
        private final int productID;
        private final String productName;
        private final double price;

        public Product(int productID, String productName, double price) {
            this.productID = productID;
            this.productName = Objects.requireNonNull(productName);
            this.price = price;
        }

        public int getProductID() { return productID; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
    }

    private static final String[] ORDER_FILES = {
            "2021_order_details.txt",
            "2022_order_details.txt",
            "2023_order_details.txt",
            "2024_order_details.txt"
    };

    private static Product[] productCatalog = new Product[10];

    public static void loadProducts() throws IOException {
        Path productsPath = Paths.get(RESOURCES_DIR + "Products.txt");
        List<String> lines = Files.readAllLines(productsPath);

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length != 3) {
                System.err.println("Invalid product line: " + line);
                continue;
            }

            try {
                int productId = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                double price = Double.parseDouble(parts[2].trim());

                if (productId < 0 || productId >= productCatalog.length) {
                    System.err.println("Invalid product ID: " + productId);
                    continue;
                }

                productCatalog[productId] = new Product(productId, name, price);
            } catch (NumberFormatException e) {
                System.err.println("Number format error in product line: " + line);
            }
        }
    }

    private static Product findProduct(int productId) {
        if (productId >= 0 && productId < productCatalog.length) {
            return productCatalog[productId];
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            loadProducts();
        } catch (IOException e) {
            System.err.println("Failed to load product catalog: " + e.getMessage());
            return;
        }

        List<Thread> threads = new ArrayList<>();
        List<TaskRunnable> tasks = new ArrayList<>();

        for (String orderFile : ORDER_FILES) {
            String fullPath = RESOURCES_DIR + orderFile;
            TaskRunnable task = new TaskRunnable(fullPath);
            Thread thread = new Thread(task);
            tasks.add(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (TaskRunnable task : tasks) {
            task.makeReport();
        }
    }
}