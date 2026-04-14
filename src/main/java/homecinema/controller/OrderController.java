package homecinema.controller;

import homecinema.dto.OrderRequestDTO;
import homecinema.dto.OrderResponseDTO;
import homecinema.service.OrderNotFoundException;
import homecinema.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:4200", "https://s1156856.student.inf.st.hsleiden.nl"}, allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/by-user")
    public ResponseEntity<?> getOrdersByUser(@RequestParam String email) {
        try {
            var orders = orderService.findOrdersByUserEmail(email);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fout bij ophalen orders: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderResponseDTO dto = orderService.findOrderDtoById(id);
            return ResponseEntity.ok(dto);
        } catch (OrderNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fout bij ophalen order: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO request) {
        try {
            OrderResponseDTO order = orderService.createOrderFromDto(request);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException | OrderNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewOrder(@RequestBody OrderRequestDTO request) {
        try {
            OrderResponseDTO preview = orderService.previewOrderFromDto(request);
            return ResponseEntity.ok(preview);
        } catch (IllegalArgumentException | OrderNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
