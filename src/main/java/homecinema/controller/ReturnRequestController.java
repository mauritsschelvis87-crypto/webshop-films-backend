package homecinema.controller;

import homecinema.model.ReturnRequest;
import homecinema.service.ReturnRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/returns")
public class ReturnRequestController {
    private final ReturnRequestService returnRequestService;

    public ReturnRequestController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @GetMapping
    public List<ReturnRequest> getAllReturnRequests() {
        return returnRequestService.findAll();
    }

    @PostMapping
    public ReturnRequest createReturnRequest(@RequestBody ReturnRequest returnRequest) {
        return returnRequestService.save(returnRequest);
    }
}
