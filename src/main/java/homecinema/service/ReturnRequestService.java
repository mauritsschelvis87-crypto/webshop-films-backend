package homecinema.service;

import homecinema.model.ReturnRequest;
import homecinema.repository.ReturnRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReturnRequestService {
    private final ReturnRequestRepository returnRequestRepository;

    public ReturnRequestService(ReturnRequestRepository returnRequestRepository) {
        this.returnRequestRepository = returnRequestRepository;
    }

    public ReturnRequest save(ReturnRequest returnRequest) {
        return returnRequestRepository.save(returnRequest);
    }

    public List<ReturnRequest> findAll() {
        return returnRequestRepository.findAll();
    }

    public List<ReturnRequest> findByStatus(String status) {
        return returnRequestRepository.findByStatus(status);
    }
}
