package order.service;

import order.exception.OutOfStockException;
import order.models.Product;
import order.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository repository;
    @InjectMocks
    private ProductService service;


    Product testProduct;
    @BeforeEach
    void setUp(){
        testProduct = new Product(1L,"Laptop",1500,15);
    }

    @Test
    @Order(1)
    @DisplayName("1. If product is availability")
    void testCheckAvailability(){
        when(repository.findProductById(1L))
                .thenReturn(Optional.of(testProduct));
        boolean check = service.checkAvailability(1L,10);
        assertTrue(check);
        verify(repository,times(1)).findProductById(1L);
    }
    @Test
    @Order(2)
    @DisplayName("2. If product is not availability")
    void testCheckNotAvailability(){
        when(repository.findProductById(1L))
                .thenReturn(Optional.of(testProduct));
        boolean check = service.checkAvailability(1L,50);
        assertFalse(check);
        verify(repository,times(1)).findProductById(1L);
    }
    @Test
    @Order(3)
    @DisplayName("3. If there is no product")
    void checkIfThereIsNoProduct(){
        when(repository.findProductById(9L))
                .thenReturn(Optional.empty());
        boolean check = service.checkAvailability(9L,80);
        assertFalse(check);
        verify(repository,times(1)).findProductById(9L);
    }
    @Test
    @DisplayName("4. when product is in stock")
    @Order(4)
    void testIncrementStock(){
        when(repository.findProductById(1L))
                .thenReturn(Optional.of(testProduct));
        service.incrementStock(1L,15);
        assertEquals(30,testProduct.getStock());
        verify(repository,times(1)).updateProduct(any());
    }
    @Test
    @Order(5)
    @DisplayName("5. When product is out of stock")
    void testIncrementWithoutProduct(){
        when(repository.findProductById(5L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,()->service.incrementStock(5L,15));
        verify(repository, never()).updateProduct(any());
    }
    @Test
    @Order(6)
    @DisplayName("6. Successful reduction")
    void successfulReduction(){
        when(repository.findProductById(1L))
                .thenReturn(Optional.of(testProduct));
        service.decrementStock(1L,5);
        assertEquals(10,testProduct.getStock());
        verify(repository,times(1)).updateProduct(any());
    }
    @Test
    @Order(7)
    @DisplayName("7. Error when there is a shortage")
    void errorShortage(){
        when(repository.findProductById(1L))
                .thenReturn(Optional.of(testProduct));
        assertThrows(OutOfStockException.class,()->service.decrementStock(1L,80));
        verify(repository,never()).updateProduct(any());
    }
    @Test
    @Order(8)
    @DisplayName("8. Error in the absence")
    void errorAbsence(){
        when(repository.findProductById(9L))
                .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,() -> service.decrementStock(9, 6));
        verify(repository,never()).updateProduct(any());
    }

    @AfterEach
    void setClose(TestInfo info){
        System.out.println(info.getDisplayName());
        System.out.println("Compiled: \n");
        testProduct = null;
    }
}
