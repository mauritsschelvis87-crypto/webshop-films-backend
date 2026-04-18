package homecinema.service;

import homecinema.model.ReturnRequest;
import homecinema.repository.ReturnRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReturnRequestService {
    private final ReturnRequestRepository returnRequestRepository;

    public ReturnRequestService(ReturnRequestRepository returnRequestRepository) {
        this.returnRequestRepository = returnRequestRepository;
    }

    public ReturnRequest save(ReturnRequest returnRequest) {
        if (returnRequest.getRequestDate() == null) {
            returnRequest.setRequestDate(LocalDateTime.now());
        }
        if (returnRequest.getStatus() == null) {
            returnRequest.setStatus("PENDING");
        }
        return returnRequestRepository.save(returnRequest);
    }

    public List<ReturnRequest> findAll() {
        return returnRequestRepository.findAll();
    }

    public List<ReturnRequest> findByStatus(String status) {
        return returnRequestRepository.findByStatus(status);
    }

    public List<ReturnRequest> findByUserEmail(String userEmail) {
        return returnRequestRepository.findByOrderItem_Order_User_Email(userEmail);
    }

    public List<ReturnRequest> findByOrderId(Long orderId) {
        return returnRequestRepository.findByOrderItem_Order_Id(orderId);
    }

    public boolean hasReturnRequestForOrderItem(Long orderItemId) {
        return returnRequestRepository.existsByOrderItem_Id(orderItemId);
    }
}
