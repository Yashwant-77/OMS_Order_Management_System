package OMS_backend.service;

import OMS_backend.dto.request.CreatePurchaseOrderRequest;
import OMS_backend.dto.request.PurchaseOrderItemRequest;
import OMS_backend.dto.request.SupplierRequest;
import OMS_backend.dto.response.PurchaseOrderItemResponse;
import OMS_backend.dto.response.PurchaseOrderResponse;
import OMS_backend.dto.response.SupplierResponse;
import OMS_backend.exception.BadRequestException;
import OMS_backend.exception.DuplicateResourceException;
import OMS_backend.exception.ResourceNotFoundException;
import OMS_backend.model.*;
import OMS_backend.repository.ProductRepository;
import OMS_backend.repository.PurchaseOrderRepository;
import OMS_backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    // create supplier
    public SupplierResponse createSupplier(SupplierRequest request) {

        log.info("Creating supplier. email={}", request.getEmail());

        if (supplierRepository.existsByEmail(request.getEmail())) {
            log.warn("Supplier creation failed - duplicate email={}", request.getEmail());
            throw new DuplicateResourceException("Supplier with this email already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        Supplier saved = supplierRepository.save(supplier);

        log.info("Supplier created successfully. supplierId={}, email={}",
                saved.getSupplierId(), saved.getEmail());

        return mapToSupplierResponse(saved);
    }

    public List<SupplierResponse> getAllSuppliers() {

        log.info("Fetching all suppliers");

        List<SupplierResponse> suppliers = supplierRepository.findAll()
                .stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} suppliers", suppliers.size());

        return suppliers;
    }

    // create purchase order
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) {

        log.info("Creating purchase order. supplierId={}, itemsCount={}",
                request.getSupplierId(), request.getItems().size());

        // validate supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> {
                    log.error("Supplier not found. supplierId={}", request.getSupplierId());
                    return new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId());
                });

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(LocalDateTime.now());
        po.setStatus(PurchaseOrderStatus.PENDING);

        double total = 0.0;

        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {

            log.debug("Processing PO item. productId={}, quantity={}, unitPrice={}",
                    itemRequest.getProductId(), itemRequest.getQuantity(), itemRequest.getUnitPrice());

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product not found. productId={}", itemRequest.getProductId());
                        return new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId());
                    });

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            po.getItems().add(item);

            total += itemRequest.getUnitPrice() * itemRequest.getQuantity();
        }

        po.setTotalAmount(total);

        PurchaseOrder savedPO = purchaseOrderRepository.save(po);

        log.info("Purchase order created successfully. poId={}, totalAmount={}",
                savedPO.getPurchaseOrderId(), total);

        return mapToPOResponse(savedPO);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {

        log.info("Fetching all purchase orders");

        List<PurchaseOrderResponse> list = purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToPOResponse)
                .collect(Collectors.toList());

        log.info("Fetched {} purchase orders", list.size());

        return list;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {

        log.info("Fetching purchase order by id={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Purchase order not found. id={}", id);
                    return new ResourceNotFoundException("Purchase order not found with id: " + id);
                });

        return mapToPOResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse updateStatus(Long id, String status) {

        log.info("Updating PO status. poId={}, newStatus={}", id, status);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Purchase order not found for update. id={}", id);
                    return new ResourceNotFoundException("Purchase order not found with id: " + id);
                });

        PurchaseOrderStatus newStatus;
        try {
            newStatus = PurchaseOrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.error("Invalid PO status: {}", status);
            throw new BadRequestException("Invalid purchase order status: " + status);
        }

        // stock update on RECEIVED
        if (newStatus == PurchaseOrderStatus.RECEIVED && po.getStatus() != PurchaseOrderStatus.RECEIVED) {

            log.info("Updating stock for received PO. poId={}", id);

            for (PurchaseOrderItem item : po.getItems()) {

                Product product = item.getProduct();
                int oldStock = product.getQuantityInStock();

                product.setQuantityInStock(oldStock + item.getQuantity());
                productRepository.save(product);

                log.debug("Stock increased. productId={}, addedQty={}, newStock={}",
                        product.getProductId(), item.getQuantity(), product.getQuantityInStock());
            }
        }

        po.setStatus(newStatus);
        PurchaseOrder updated = purchaseOrderRepository.save(po);

        log.info("PO status updated successfully. poId={}, status={}", id, updated.getStatus());

        return mapToPOResponse(updated);
    }

    @Transactional
    public void cancelPurchaseOrder(Long id) {

        log.info("Cancelling purchase order. poId={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Purchase order not found for cancellation. id={}", id);
                    return new ResourceNotFoundException("Purchase order not found with id: " + id);
                });

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            log.warn("Attempt to cancel received PO. poId={}", id);
            throw new BadRequestException("Cannot cancel an already received purchase order");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(po);

        log.info("Purchase order cancelled successfully. poId={}", id);
    }

    private PurchaseOrderResponse mapToPOResponse(PurchaseOrder po) {

        log.debug("Mapping PurchaseOrder to response. poId={}", po.getPurchaseOrderId());

        List<PurchaseOrderItemResponse> items = po.getItems()
                .stream()
                .map(item -> new PurchaseOrderItemResponse(
                        item.getPurchaseOrderItemId(),
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice() * item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new PurchaseOrderResponse(
                po.getPurchaseOrderId(),
                po.getSupplier().getSupplierId(),
                po.getSupplier().getName(),
                po.getOrderDate(),
                po.getTotalAmount(),
                po.getStatus().name(),
                items
        );
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {

        log.debug("Mapping Supplier to response. supplierId={}", supplier.getSupplierId());

        return new SupplierResponse(
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress()
        );
    }
}