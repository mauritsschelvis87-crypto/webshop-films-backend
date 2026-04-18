package homecinema.controller;

import homecinema.model.ReturnRequest;
import homecinema.service.OrderService;
import homecinema.service.ReturnRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200", "https://s1156856.student.inf.st.hsleiden.nl"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/returns")
public class ReturnRequestController {
    private final ReturnRequestService returnRequestService;
    private final OrderService orderService;

    public ReturnRequestController(ReturnRequestService returnRequestService, OrderService orderService) {
        this.returnRequestService = returnRequestService;
        this.orderService = orderService;
    }

    @GetMapping
    public List<ReturnRequest> getAllReturnRequests() {
        return returnRequestService.findAll();
    }

    @GetMapping("/by-user")
    public ResponseEntity<?> getReturnRequestsByUser(@RequestParam String email) {
        try {
            List<ReturnRequest> returns = returnRequestService.findByUserEmail(email);
            return ResponseEntity.ok(returns);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fout bij ophalen retouren: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<?> getReturnRequestsByOrder(@PathVariable Long orderId) {
        try {
            List<ReturnRequest> returns = returnRequestService.findByOrderId(orderId);
            return ResponseEntity.ok(returns);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fout bij ophalen retouren: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> createReturnRequest(@RequestBody ReturnRequest returnRequest) {
        try {
            // Check if return request already exists for this order item
            if (returnRequest.getOrderItem() != null && 
                returnRequestService.hasReturnRequestForOrderItem(returnRequest.getOrderItem().getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Retourverzoek bestaat al voor dit item");
                return ResponseEntity.badRequest().body(error);
            }

            ReturnRequest savedReturn = returnRequestService.save(returnRequest);
            
            // Update order status to PENDING
            if (savedReturn.getOrderItem() != null && savedReturn.getOrderItem().getOrder() != null) {
                orderService.updateOrderStatus(savedReturn.getOrderItem().getOrder().getId(), "PENDING");
            }
            
            return ResponseEntity.ok(savedReturn);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fout bij aanmaken retourverzoek: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
