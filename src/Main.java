import main.java.order.exception.OutOfStockException;
import main.java.order.models.Customer;
import main.java.order.models.Order;
import main.java.order.models.OrderStatus;
import main.java.order.models.Product;
import main.java.order.repository.OrderRepository;
import main.java.order.repository.ProductRepository;
import main.java.order.service.OrderService;
import main.java.order.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Демонстрация системы заказов ===\n");


        System.out.println("1. Создание товаров:");
        Product laptop = new Product(1, "Ноутбук", 1500.0, 5);
        Product mouse = new Product(2, "Мышь", 50.0, 10);
        Product keyboard = new Product(3, "Клавиатура", 100.0, 0); // Нет в наличии

        ProductRepository productRepository = new ProductRepository();
        productRepository.addProduct(laptop);
        productRepository.addProduct(mouse);
        productRepository.addProduct(keyboard);

        System.out.println("   - " + laptop.getName() + ": цена=" + laptop.getPrice() + ", остаток=" + laptop.getStock());
        System.out.println("   - " + mouse.getName() + ": цена=" + mouse.getPrice() + ", остаток=" + mouse.getStock());
        System.out.println("   - " + keyboard.getName() + ": цена=" + keyboard.getPrice() + ", остаток=" + keyboard.getStock());
        System.out.println();


        System.out.println("2. Создание покупателя:");
        Customer customer = new Customer(1, "ivan@example.com");
        System.out.println("   - Покупатель ID=" + customer.getId() + ", Email=" + customer.getEmail());
        System.out.println();

        // 3. Создание заказа (createOrder)
        System.out.println("3. Создание заказа (ноутбук x2, мышь x3):");
        ProductService productService = new ProductService(productRepository);
        OrderRepository orderRepository = new OrderRepository();
        OrderService orderService = new OrderService(orderRepository, productService);

        Map<Product, Integer> orderItems = new HashMap<>();
        orderItems.put(laptop, 2);
        orderItems.put(mouse, 3);

        Order order1 = orderService.createOrder(customer, orderItems);
        System.out.println("   - Заказ #" + order1.getId() + " создан, статус: " + order1.getStatus());
        System.out.println("   - Остаток ноутбуков после заказа: " + laptop.getStock());
        System.out.println("   - Остаток мышей после заказа: " + mouse.getStock());
        System.out.println();

        // 4. Попытка создания заказа с недостаточным количеством (перехват OutOfStockException)
        System.out.println("4. Попытка создать заказ с недостаточным количеством (клавиатура x1):");
        Map<Product, Integer> invalidOrder = new HashMap<>();
        invalidOrder.put(keyboard, 1);

        try {
            orderService.createOrder(customer, invalidOrder);
        } catch (OutOfStockException e) {
            System.out.println("   - Ошибка: " + e.getMessage());
            System.out.println("   - Товар ID=" + e.getProductId() + ", запрошено=" + e.getRequested() + ", доступно=" + e.getAvailable());
        }
        System.out.println();

        // 5. Оплата заказа (payOrder)
        System.out.println("5. Оплата заказа #" + order1.getId() + ":");
        orderService.payOrder(order1.getId());
        System.out.println("   - Статус заказа: " + order1.getStatus());
        System.out.println();

        // 6. Отмена заказа (cancelOrder)
        System.out.println("6. Создадим ещё один заказ и отменим его:");
        Map<Product, Integer> secondOrderItems = new HashMap<>();
        secondOrderItems.put(mouse, 2);
        Order order2 = orderService.createOrder(customer, secondOrderItems);
        System.out.println("   - Заказ #" + order2.getId() + " создан, статус: " + order2.getStatus());
        System.out.println("   - Остаток мышей до отмены: " + mouse.getStock());

        orderService.cancelOrder(order2.getId());
        System.out.println("   - После отмены статус: " + order2.getStatus());
        System.out.println("   - Остаток мышей после отмены (возврат): " + mouse.getStock());
        System.out.println();

        // 7. Поиск заказов по условию (findOrdersByCustomer)
        System.out.println("7. Поиск заказов покупателя по условию:");

        // Поиск оплаченных заказов
        List<Order> paidOrders = orderService.findOrdersByCustomer(customer,
                order -> order.getStatus() == OrderStatus.PAID);
        System.out.println("   - Оплаченные заказы: " + paidOrders.stream().map(Order::getId).toList());

        // Поиск отменённых заказов
        List<Order> cancelledOrders = orderService.findOrdersByCustomer(customer,
                order -> order.getStatus() == OrderStatus.CANCELLED);
        System.out.println("   - Отменённые заказы: " + cancelledOrders.stream().map(Order::getId).toList());

        // Поиск всех заказов
        List<Order> allOrders = orderService.findOrdersByCustomer(customer,
                order -> true);
        System.out.println("   - Все заказы покупателя: " + allOrders.stream().map(Order::getId).toList());
        System.out.println();

        // 8. Расчет суммы со скидкой (calculateTotal)
        System.out.println("8. Расчёт суммы заказа со скидкой:");

        // Без скидки
        double totalWithoutDiscount = orderService.calculateTotal(order1, subtotal -> subtotal);
        System.out.println("   - Сумма заказа #" + order1.getId() + " без скидки: $" + totalWithoutDiscount);

        // Скидка 10%
        double totalWith10PercentOff = orderService.calculateTotal(order1, subtotal -> subtotal * 0.9);
        System.out.println("   - Сумма со скидкой 10%: $" + totalWith10PercentOff);

        // Скидка 20% для заказов дороже 2000 (демонстрация сложной логики)
        double totalWithConditionalDiscount = orderService.calculateTotal(order1, subtotal -> {
            if (subtotal > 2000) {
                return subtotal * 0.8; // 20% скидка
            }
            return subtotal * 0.95; // 5% скидка
        });
        System.out.println("   - Сумма с условной скидкой (5% или 20%): $" + totalWithConditionalDiscount);

        // Демонстрация расчёта для второго заказа
        double mouseOrderTotal = orderService.calculateTotal(order2, subtotal -> subtotal);
        System.out.println("   - Сумма заказа #" + order2.getId() + " (только мыши): $" + mouseOrderTotal);

        System.out.println("\n=== Демонстрация завершена ===");
    }
}