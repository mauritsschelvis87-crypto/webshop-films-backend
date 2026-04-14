package homecinema.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ReturnRequest extends BaseEntity {

    @ManyToOne
    private OrderItem orderItem;

    private String returnReason;

    @Column(length = 1000)
    private String otherReasonText;
    private String status;
    private String photoUrl;
    private LocalDateTime requestDate;
    private String backupAddress;
    public OrderItem getOrderItem() { return orderItem; }
    public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public String getOtherReasonText() { return otherReasonText; }
    public void setOtherReasonText(String otherReasonText) { this.otherReasonText = otherReasonText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public String getBackupAddress() { return backupAddress; }
    public void setBackupAddress(String backupAddress) { this.backupAddress = backupAddress; }
}
