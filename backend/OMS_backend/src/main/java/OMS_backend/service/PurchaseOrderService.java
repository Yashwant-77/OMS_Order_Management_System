package OMS_backend.service;

import OMS_backend.dto.request.CreatePurchaseOrderRequest;
import OMS_backend.dto.request.PurchaseOrderItemRequest;
import OMS_backend.dto.request.SupplierRequest;
import OMS_backend.dto.response.PurchaseOrderItemResponse;
import OMS_backend.dto.response.PurchaseOrderResponse;
import OMS_backend.dto.response.SupplierResponse;
import OMS_backend.model.*;
import OMS_backend.repository.ProductRepository;
import OMS_backend.repository.PurchaseOrderRepository;
import OMS_backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    private final SupplierRepository supplierRepository;

    private final ProductRepository productRepository;

    //create supplier
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Supplier with this email already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        return mapToSupplierResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    // create purchase order + items in one transaction
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) {

        // validate supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + request.getSupplierId()));

        // build purchase order
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setOrderDate(LocalDateTime.now());
        po.setStatus(PurchaseOrderStatus.PENDING);

        // build items + calculate total
        double total = 0.0;

        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice()); // negotiated price
            po.getItems().add(item);

            total += itemRequest.getUnitPrice() * itemRequest.getQuantity();
        }

        po.setTotalAmount(total);

        return mapToPOResponse(purchaseOrderRepository.save(po));
    }

    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToPOResponse)
                .collect(Collectors.toList());
    }

    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        return mapToPOResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse updateStatus(Long id, String status) {

        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));

        PurchaseOrderStatus newStatus =
                PurchaseOrderStatus.valueOf(status.toUpperCase());

        // Key business rule: when order is RECEIVED, update stock automatically
        if (newStatus == PurchaseOrderStatus.RECEIVED
                && po.getStatus() != PurchaseOrderStatus.RECEIVED) {

            for (PurchaseOrderItem item : po.getItems()) {
                Product product = item.getProduct();
                product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        po.setStatus(newStatus);
        return mapToPOResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public void cancelPurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new RuntimeException("Cannot cancel an already received purchase order");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(po);
    }

    //mapping PurchaseOrder to PurchaseOrderResponse
    private PurchaseOrderResponse mapToPOResponse(PurchaseOrder po) {
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

    // mapping Supplier to SupplierResponse
    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getSupplierId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress()
        );
    }
}