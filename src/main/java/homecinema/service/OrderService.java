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

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, FilmRepository filmRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    @Transactional
    public Order createOrderFromDto(OrderRequestDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUsername()));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(dto.getTotalPrice());
        order.setNumber(generateOrderNumber());
        order.setStatus("PENDING");

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderRequestDTO.OrderItemDTO itemDto : dto.getItems()) {
            Film film = filmRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Film not found with id: " + itemDto.getProductId()));

            if (itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for film id: " + itemDto.getProductId());
            }

            OrderItem item = new OrderItem();
            item.setFilm(film);
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(film.getPrice());
            item.setOrder(order);

            orderItems.add(item);
        }

        order.setOrderItems(orderItems);
        return orderRepository.save(order);
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

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalPrice(order.getTotalPrice());

        List<OrderResponseDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            OrderResponseDTO.OrderItemDTO itemDTO = new OrderResponseDTO.OrderItemDTO();
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());

            Film film = item.getFilm();
            OrderResponseDTO.FilmDTO filmDTO = new OrderResponseDTO.FilmDTO();
            filmDTO.setId(film.getId());
            filmDTO.setTitle(film.getTitle());
            filmDTO.setPrice(film.getPrice());
            filmDTO.setImageUrl(film.getImageUrl());
            filmDTO.setType(film.getType());
            filmDTO.setDirector(film.getDirector());
            filmDTO.setCountry(film.getCountry());
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
            } else {
                filmDTO.setBrand(null);
            }

            itemDTO.setFilm(filmDTO);
            itemDTOs.add(itemDTO);
        }

        dto.setOrderItems(itemDTOs);
        return dto;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
