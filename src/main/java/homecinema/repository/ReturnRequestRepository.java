package homecinema.repository;

import homecinema.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByStatus(String status);
    List<ReturnRequest> findByOrderItem_Order_User_Email(String userEmail);
    List<ReturnRequest> findByOrderItem_Order_Id(Long orderId);
    boolean existsByOrderItem_Id(Long orderItemId);
}
