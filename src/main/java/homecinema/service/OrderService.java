package homecinema.service;

import homecinema.dto.OrderRequestDTO;
import homecinema.dto.OrderResponseDTO;
import homecinema.model.Film;
import homecinema.model.Order;
import homecinema.model.OrderItem;
import homecinema.model.User;
import homecinema.repository.FilmRepository;
import homecinema.repository.OrderRepository;
import homecinema.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    private static final Map<String, Double> GIFT_CARD_DISCOUNTS = Map.of(
            "FREE10", 10.0,
            "FREE20", 20.0,
            "FREE30", 30.0
    );
    private static final Map<String, Double> GIFT_CODE_PERCENTAGES = Map.of(
            "FREE5", 0.05,
            "FREE10", 0.10
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        FilmRepository filmRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    @Transactional
    public OrderResponseDTO createOrderFromDto(OrderRequestDTO dto) {
        User user = validateAndLoadUser(dto);
        CalculatedOrderPricing pricing = calculateOrderPricing(dto);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setNumber(generateOrderNumber());
        order.setStatus("PENDING");

        List<OrderItem> orderItems = createOrderItems(pricing.orderItemData(), order);
        if (pricing.appliedGiftCode() != null) {
            order.setAppliedGiftCode(pricing.appliedGiftCode());
        }

        if (pricing.appliedGiftCardCode() != null) {
            order.setAppliedGiftCardCode(pricing.appliedGiftCardCode());
        }

        order.setOrderItems(orderItems);
        order.setSubtotalPrice(pricing.subtotalPrice());
        order.setDiscountAmount(pricing.discountAmount());
        order.setTotalPrice(pricing.totalPrice());
        return toOrderResponseDto(orderRepository.save(order));
    }

    public OrderResponseDTO previewOrderFromDto(OrderRequestDTO dto) {
        validateAndLoadUser(dto);
        CalculatedOrderPricing pricing = calculateOrderPricing(dto);

        OrderResponseDTO dtoResponse = new OrderResponseDTO();
        dtoResponse.setStatus("PREVIEW");
        dtoResponse.setSubtotalPrice(pricing.subtotalPrice());
        dtoResponse.setDiscountAmount(pricing.discountAmount());
        dtoResponse.setTotalPrice(pricing.totalPrice());
        dtoResponse.setAppliedGiftCardCode(pricing.appliedGiftCardCode());
        dtoResponse.setAppliedGiftCode(pricing.appliedGiftCode());
        dtoResponse.setOrderItems(toOrderItemDtos(pricing.orderItemData()));
        return dtoResponse;
    }

    public List<Order> findOrdersByUserEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        return orderRepository.findByUser(userOpt.get());
    }

    public OrderResponseDTO findOrderDtoById(Long id) {
        Order order = orderRepository.findByIdWithItemsAndFilmBrand(id)
                .orElseThrow(() -> new RuntimeException("Bestelling met id " + id + " niet gevonden."));

        return toOrderResponseDto(order);
    }

    private OrderResponseDTO toOrderResponseDto(Order order) {

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setNumber(order.getNumber());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setSubtotalPrice(order.getSubtotalPrice());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setAppliedGiftCardCode(order.getAppliedGiftCardCode());
        dto.setAppliedGiftCode(order.getAppliedGiftCode());
        dto.setOrderItems(toOrderItemDtosFromOrders(order.getOrderItems()));
        return dto;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private User validateAndLoadUser(OrderRequestDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        return userRepository.findByUsername(dto.getUsername())
                .or(() -> userRepository.findByEmail(dto.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUsername()));
    }

    private CalculatedOrderPricing calculateOrderPricing(OrderRequestDTO dto) {
        Map<Long, Film> filmsById = loadFilmsById(dto);
        List<OrderItemData> orderItemData = new ArrayList<>();
        double subtotalPrice = 0.0;

        for (OrderRequestDTO.OrderItemDTO itemDto : dto.getItems()) {
            Film film = filmsById.get(itemDto.getProductId());
            if (film == null) {
                throw new IllegalArgumentException("Film not found with id: " + itemDto.getProductId());
            }
            if (itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for film id: " + itemDto.getProductId());
            }

            orderItemData.add(new OrderItemData(film, itemDto.getQuantity(), film.getPrice()));
            subtotalPrice += film.getPrice() * itemDto.getQuantity();
        }

        double discountAmount = 0.0;
        String appliedGiftCode = null;
        String appliedGiftCardCode = null;

        if (dto.getGiftCode() != null && !dto.getGiftCode().isBlank()) {
            String normalizedGiftCode = normalizeCode(dto.getGiftCode());
            Double percentage = GIFT_CODE_PERCENTAGES.get(normalizedGiftCode);
            if (percentage == null) {
                throw new IllegalArgumentException("The code is invalid.");
            }
            discountAmount += subtotalPrice * percentage;
            appliedGiftCode = normalizedGiftCode;
        }

        if (dto.getGiftCardCode() != null && !dto.getGiftCardCode().isBlank()) {
            String normalizedGiftCardCode = normalizeCode(dto.getGiftCardCode());
            Double fixedDiscount = GIFT_CARD_DISCOUNTS.get(normalizedGiftCardCode);
            if (fixedDiscount == null) {
                throw new IllegalArgumentException("The code is invalid.");
            }
            discountAmount += fixedDiscount;
            appliedGiftCardCode = normalizedGiftCardCode;
        }

        discountAmount = Math.min(discountAmount, subtotalPrice);
        double totalPrice = Math.max(0.0, subtotalPrice - discountAmount);
        return new CalculatedOrderPricing(orderItemData, subtotalPrice, discountAmount, totalPrice, appliedGiftCardCode, appliedGiftCode);
    }

    private Map<Long, Film> loadFilmsById(OrderRequestDTO dto) {
        Set<Long> productIds = new LinkedHashSet<>();
        for (OrderRequestDTO.OrderItemDTO item : dto.getItems()) {
            productIds.add(item.getProductId());
        }

        Map<Long, Film> filmsById = new HashMap<>();
        for (Film film : filmRepository.findAllById(productIds)) {
            filmsById.put(film.getId(), film);
        }
        return filmsById;
    }

    private List<OrderItem> createOrderItems(List<OrderItemData> itemData, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemData itemDataEntry : itemData) {
            OrderItem item = new OrderItem();
            item.setFilm(itemDataEntry.film());
            item.setQuantity(itemDataEntry.quantity());
            item.setPrice(itemDataEntry.price());
            item.setOrder(order);
            orderItems.add(item);
        }
        return orderItems;
    }

    private List<OrderResponseDTO.OrderItemDTO> toOrderItemDtos(List<OrderItemData> items) {
        List<OrderResponseDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItemData item : items) {
            OrderResponseDTO.OrderItemDTO itemDTO = new OrderResponseDTO.OrderItemDTO();
            itemDTO.setQuantity(item.quantity());
            itemDTO.setPrice(item.price());

            itemDTO.setFilm(toFilmDto(item.film()));
            itemDTOs.add(itemDTO);
        }
        return itemDTOs;
    }

    private List<OrderResponseDTO.OrderItemDTO> toOrderItemDtosFromOrders(List<OrderItem> items) {
        List<OrderResponseDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderResponseDTO.OrderItemDTO itemDTO = new OrderResponseDTO.OrderItemDTO();
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setFilm(toFilmDto(item.getFilm()));
            itemDTOs.add(itemDTO);
        }
        return itemDTOs;
    }

    private OrderResponseDTO.FilmDTO toFilmDto(Film film) {
        OrderResponseDTO.FilmDTO filmDTO = new OrderResponseDTO.FilmDTO();
        filmDTO.setId(film.getId());
        filmDTO.setTitle(film.getTitle());
        filmDTO.setPrice(film.getPrice());
        filmDTO.setImageUrl(film.getImageUrl());
        filmDTO.setType(film.getType());
        filmDTO.setDirector(film.getDirector());
        filmDTO.setCountry(film.getCountry());
        filmDTO.setRegion(film.getRegion());
        filmDTO.setYear(film.getYear());
        filmDTO.setRuntime(film.getRuntime());
        filmDTO.setAspectRatio(film.getAspectRatio());
        filmDTO.setColorOrBlackAndWhite(film.getColorOrBlackAndWhite());
        filmDTO.setDescription(film.getDescription());
        filmDTO.setStills(film.getStills());
        if (film.getBrand() != null) {
            OrderResponseDTO.BrandDTO brandDTO = new OrderResponseDTO.BrandDTO();
            brandDTO.setId(film.getBrand().getId());
            brandDTO.setName(film.getBrand().getName());
            filmDTO.setBrand(brandDTO);
        }
        return filmDTO;
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("The code is invalid.");
        }
        return normalized;
    }

    private record OrderItemData(Film film, int quantity, double price) {
    }

    private record CalculatedOrderPricing(
            List<OrderItemData> orderItemData,
            double subtotalPrice,
            double discountAmount,
            double totalPrice,
            String appliedGiftCardCode,
            String appliedGiftCode
    ) {
    }
}
