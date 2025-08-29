package com.burock.jwt_2.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.burock.jwt_2.dto.ResponseWrapper;
import com.burock.jwt_2.dto.CreateOrderRequest;
import com.burock.jwt_2.dto.OrderResponse;
import com.burock.jwt_2.model.OrderStatus;
import com.burock.jwt_2.service.MessageService;
import com.burock.jwt_2.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Siparişler", description = "Sipariş yönetimi ve Elasticsearch arama işlemleri")
public class OrderController {

    private final OrderService orderService;
    private final MessageService messageService;

    // Kullanıcı İşlemleri

    @Operation(summary = "Sipariş Oluştur", description = "Sepetteki ürünlerden sipariş oluşturur ve stokları günceller. Sipariş durumu lokalize edilir.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sepet boş veya yetersiz stok"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PostMapping("/create")
    public ResponseEntity<ResponseWrapper<OrderResponse>> createOrder(
            @Parameter(description = "Sipariş bilgileri", required = true) @Valid @RequestBody CreateOrderRequest request,
            Principal principal) {
        try {
            OrderResponse order = orderService.createOrder(request, principal.getName());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("order.created"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Siparişlerimi Listele", description = "Kullanıcının tüm siparişlerini lokalize durum metinleri ile getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siparişler başarıyla getirildi", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders")
    public ResponseEntity<ResponseWrapper<List<OrderResponse>>> getMyOrders(Principal principal) {
        try {
            List<OrderResponse> orders = orderService.getUserOrders(principal.getName());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Siparişlerimi Sayfalı Listele", description = "Kullanıcının siparişlerini sayfalı olarak getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siparişler başarıyla getirildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders/paged")
    public ResponseEntity<ResponseWrapper<Page<OrderResponse>>> getMyOrdersPaged(
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        try {
            Page<OrderResponse> orders = orderService.getUserOrdersPaged(principal.getName(),
                    PageRequest.of(page, size));
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Detayı", description = "Belirtilen ID'ye sahip siparişin detaylarını getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş detayı başarıyla getirildi", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Erişim reddedildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders/{orderId}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> getMyOrderById(
            @Parameter(description = "Sipariş ID'si", required = true) @PathVariable Long orderId,
            Principal principal) {
        try {
            OrderResponse order = orderService.getOrderById(orderId, principal.getName());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @Operation(summary = "Duruma Göre Siparişlerimi Listele", description = "Kullanıcının belirtilen durumdaki siparişlerini getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siparişler başarıyla getirildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders/status/{status}")
    public ResponseEntity<ResponseWrapper<List<OrderResponse>>> getMyOrdersByStatus(
            @Parameter(description = "Sipariş durumu (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)") @PathVariable OrderStatus status,
            Principal principal) {
        try {
            List<OrderResponse> orders = orderService.getUserOrdersByStatus(principal.getName(), status);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Siparişi İptal Et", description = "Kullanıcı sadece PENDING durumundaki siparişlerini iptal edebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş başarıyla iptal edildi", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sipariş iptal edilemez"),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Erişim reddedildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> cancelMyOrder(
            @Parameter(description = "İptal edilecek sipariş ID'si", required = true) @PathVariable Long orderId,
            Principal principal) {
        try {
            OrderResponse order = orderService.cancelOrder(orderId, principal.getName());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("order.cancelled"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.cannot.cancel"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Sayımı", description = "Kullanıcının toplam sipariş sayısını getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş sayısı başarıyla getirildi", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("my-orders/count")
    public ResponseEntity<ResponseWrapper<Long>> getMyOrderCount(Principal principal) {
        try {
            Long count = orderService.getUserOrderCount(principal.getName());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Toplam Harcama Miktarı", description = "Kullanıcının tüm siparişlerindeki toplam harcama miktarını getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Toplam harcama miktarı başarıyla getirildi", content = @Content(schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders/total-spending")
    public ResponseEntity<ResponseWrapper<BigDecimal>> getMyTotalSpending(Principal principal) {
        try {
            BigDecimal totalSpending = orderService.getUserTotalSpending(principal.getName());
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    totalSpending));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Özeti", description = "Belirtilen sipariş için detaylı özet bilgisi getirir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş özeti başarıyla getirildi", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Erişim reddedildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders/{orderId}/summary")
    public ResponseEntity<ResponseWrapper<String>> getMyOrderSummary(
            @Parameter(description = "Sipariş ID'si", required = true) @PathVariable Long orderId,
            Principal principal) {
        try {
            orderService.getOrderById(orderId, principal.getName());
            String summary = orderService.getOrderSummary(orderId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    summary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @Operation(summary = "Teslimat Süresi Hesapla", description = "Sipariş için tahmini teslimat süresini hesaplar", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teslimat süresi başarıyla hesaplandı", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Erişim reddedildi"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @GetMapping("/my-orders/{orderId}/delivery-time")
    public ResponseEntity<ResponseWrapper<String>> getMyOrderDeliveryTime(
            @Parameter(description = "Sipariş ID'si", required = true) @PathVariable Long orderId,
            Principal principal) {
        try {
            orderService.getOrderById(orderId, principal.getName());
            String deliveryTime = orderService.calculateDeliveryTime(orderId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    deliveryTime));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    // Genel Arama İşlemleri

    @Operation(summary = "Sipariş Ara", description = "Elasticsearch ile sipariş numarası, adres veya notlarda arama yapar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arama başarılı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz arama parametresi")
    })
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<Page<OrderResponse>>> searchOrders(
            @Parameter(description = "Arama terimi (sipariş numarası, adres, notlar)") @RequestParam String query,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<OrderResponse> orders = orderService.searchOrdersInElasticsearch(query,
                    PageRequest.of(page, size));
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("search.no.results"),
                    null));
        }
    }

    @Operation(summary = "Duruma Göre Sipariş Ara", description = "Elasticsearch ile belirtilen durumdaki siparişleri arar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arama başarılı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz durum parametresi")
    })
    @GetMapping("/search/status")
    public ResponseEntity<ResponseWrapper<Page<OrderResponse>>> searchOrdersByStatus(
            @Parameter(description = "Sipariş durumu (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)") @RequestParam String status,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<OrderResponse> orders = orderService.searchOrdersByStatusInElasticsearch(status,
                    PageRequest.of(page, size));
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("search.no.results"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Numarasına Göre Ara", description = "Belirtilen sipariş numarası ile siparişi getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş başarıyla bulundu", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz sipariş numarası")
    })
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> gerOrderByOrderNumber(
            @Parameter(description = "Sipariş numarası", required = true) @PathVariable String orderNumber) {
        try {
            Optional<OrderResponse> order = orderService.getOrderByOrderNumber(orderNumber);
            if (order.isPresent()) {
                return ResponseEntity.ok(new ResponseWrapper<>(
                        messageService.getMessage("success"),
                        order.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    // Admin İşlemleri

    @Operation(summary = "Tüm Siparişleri Listele (Admin)", description = "Sadece Admin kullanıcılar tüm siparişleri listeleyebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siparişler başarıyla listelendi"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all/orders")
    public ResponseEntity<ResponseWrapper<Page<OrderResponse>>> getAllOrders(
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        try {
            Page<OrderResponse> orders = orderService.getAllOrders(PageRequest.of(page, size));
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Duruma Göre Siparişleri Listele (Admin)", description = "Admin kullanıcılar belirtilen durumdaki tüm siparişleri listeleyebilir.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siparişler başarıyla listelendi"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<ResponseWrapper<Page<OrderResponse>>> getOrdersByStatus(
            @Parameter(description = "Sipariş durumu", required = true) @PathVariable OrderStatus status,
            @Parameter(description = "Sayfa numarası") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(defaultValue = "20") int size) {
        try {
            Page<OrderResponse> orders = orderService.getOrdersByStatus(status, PageRequest.of(page, size));
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Durumu Güncelle (Admin)", description = "Sadece Admin kullanıcılar sipariş durumunu güncelleyebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş durumu başarıyla güncellendi", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{orderId}/{status}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Güncellenecek sipariş ID'si", required = true) @PathVariable Long orderId,
            @Parameter(description = "Yeni sipariş durumu", required = true) @RequestParam OrderStatus status) {
        try {
            OrderResponse order = orderService.updateOrderStatus((orderId), status);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("order.updated"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @Operation(summary = "Siparişi Admin Olarak İptal Et", description = "Admin kullanıcı herhangi bir siparişi iptal edebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş başarıyla iptal edildi", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/cancel/{orderId}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> cancelOrderByAdmin(
            @Parameter(description = "İptal edilecek sipariş ID'si", required = true) @PathVariable Long orderId,
            @Parameter(description = "İptal nedeni") @RequestParam(required = false) String reason) {
        try {
            OrderResponse order = orderService.cancelOrderByAdmin(orderId, reason);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("order.cancelled.by.admin"),
                    order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.cannot.cancel"),
                    null));
        }
    }

    @Operation(summary = "Tarih Aralığındaki Siparişler (Admin)", description = "Admin kullanıcı belirtilen tarih aralığındaki siparişleri görebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Siparişler başarıyla getirildi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz tarih formatı"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/between-dates")
    public ResponseEntity<ResponseWrapper<List<OrderResponse>>> getOrdersBetweenDates(
            @Parameter(description = "Başlangıç tarihi (ISO format)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Bitiş tarihi (ISO format)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<OrderResponse> orders = orderService.getOrdersBetweenDates(startDate, endDate);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    orders));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("error"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Detayı (Admin)", description = "Admin kullanıcı herhangi bir siparişin detayını görebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş detayı başarıyla getirildi", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}")
    public ResponseEntity<ResponseWrapper<OrderResponse>> getOrderByIdAdmin(
            @Parameter(description = "Sipariş ID'si", required = true) @PathVariable Long orderId) {
        try {
            Optional<OrderResponse> order = orderService.getOrderByOrderNumber(orderService
                    .getAllOrders(PageRequest.of(0, Integer.MAX_VALUE)).stream().filter(o -> o.getId().equals(orderId))
                    .findFirst().map(OrderResponse::getOrderNumber).orElse(""));
            if (order.isPresent()) {
                return ResponseEntity.ok(new ResponseWrapper<>(
                        messageService.getMessage("success"),
                        order.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @Operation(summary = "Sipariş Özeti (Admin)", description = "Admin kullanıcı herhangi bir siparişin özetini görebilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sipariş özeti başarıyla getirildi", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}/summary")
    public ResponseEntity<ResponseWrapper<String>> getOrderSummaryAdmin(
            @Parameter(description = "Sipariş ID'si", required = true) @PathVariable Long orderId) {
        try {
            String summary = orderService.getOrderSummary(orderId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    summary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

    @Operation(summary = "Teslimat Süresi (Admin)", description = "Admin kullanıcı herhangi bir siparişin teslimat süresini hesaplayabilir", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teslimat süresi başarıyla hesaplandı", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi gerekli"),
            @ApiResponse(responseCode = "401", description = "Yetkilendirme gerekli")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}/delivery-time")
    public ResponseEntity<ResponseWrapper<String>> getOrderDeliveryTimeAdmin(
            @Parameter(description = "Sipariş ID'si", required = true) @PathVariable Long orderId) {
        try {
            String deliveryTime = orderService.calculateDeliveryTime(orderId);
            return ResponseEntity.ok(new ResponseWrapper<>(
                    messageService.getMessage("success"),
                    deliveryTime));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseWrapper<>(
                    messageService.getMessage("order.not.found"),
                    null));
        }
    }

}
