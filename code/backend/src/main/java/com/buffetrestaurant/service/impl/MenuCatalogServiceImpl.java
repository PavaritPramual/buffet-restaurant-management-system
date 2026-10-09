package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.MenuCategory;
import com.buffetrestaurant.domain.MenuItem;
import com.buffetrestaurant.dto.request.MenuCategoryRequest;
import com.buffetrestaurant.dto.request.MenuItemRequest;
import com.buffetrestaurant.dto.response.MenuCategoryResponse;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.PageResponse;
import com.buffetrestaurant.exception.BusinessRuleException;
import com.buffetrestaurant.exception.DuplicateResourceException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.UserFacingMessages;
import com.buffetrestaurant.mapper.OrderingMapper;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.MenuCategoryRepository;
import com.buffetrestaurant.repository.MenuItemRepository;
import com.buffetrestaurant.repository.OrderItemRepository;
import com.buffetrestaurant.service.MenuAdminAccessProvider;
import com.buffetrestaurant.service.MenuCatalogService;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuCatalogServiceImpl implements MenuCatalogService {
    private static final Set<String> SORT_FIELDS = Set.of("id", "name", "available");
    private final MenuAdminAccessProvider adminAccessProvider;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final BuffetPackageRepository packageRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderingMapper mapper;

    public MenuCatalogServiceImpl(MenuAdminAccessProvider adminAccessProvider,
                                  MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository,
                                  BuffetPackageRepository packageRepository, OrderItemRepository orderItemRepository,
                                  OrderingMapper mapper) {
        this.adminAccessProvider = adminAccessProvider;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.packageRepository = packageRepository;
        this.orderItemRepository = orderItemRepository;
        this.mapper = mapper;
    }

    public List<MenuCategoryResponse> getCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(mapper::toResponse).toList();
    }

    public MenuCategoryResponse getCategory(Long id) { return mapper.toResponse(requireCategory(id)); }

    @Transactional
    public MenuCategoryResponse createCategory(MenuCategoryRequest request) {
        adminAccessProvider.requireMenuWriteAccess();
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) throw new DuplicateResourceException("Menu category already exists: " + name);
        return mapper.toResponse(categoryRepository.save(new MenuCategory(name)));
    }

    @Transactional
    public MenuCategoryResponse updateCategory(Long id, MenuCategoryRequest request) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuCategory category = requireCategory(id);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) throw new DuplicateResourceException("Menu category already exists: " + name);
        category.setName(name);
        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuCategory category = requireCategory(id);
        if (itemRepository.existsByCategoryId(id)) {
            throw new BusinessRuleException(UserFacingMessages.CATEGORY_HAS_AVAILABLE_ITEMS);
        }
        categoryRepository.delete(category);
    }

    public PageResponse<MenuItemResponse> getMenuItems(int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessRuleException("page must be >= 0 and size must be between 1 and 100");
        String[] parts = sort == null ? new String[]{"id", "asc"} : sort.split(",", -1);
        if (parts.length > 2) throw new BusinessRuleException("sort must be field,asc or field,desc");
        String field = parts[0].trim();
        if (!SORT_FIELDS.contains(field)) throw new BusinessRuleException("Unsupported sort field: " + field);
        String directionValue = parts.length == 2 ? parts[1].trim() : "asc";
        if (!"asc".equalsIgnoreCase(directionValue) && !"desc".equalsIgnoreCase(directionValue)) {
            throw new BusinessRuleException("Unsupported sort direction: " + directionValue);
        }
        Sort.Direction direction = Sort.Direction.fromString(directionValue);
        Sort ordering = Sort.by(direction, field);
        if (!"id".equals(field)) ordering = ordering.and(Sort.by("id"));
        Page<MenuItem> result = itemRepository.findAll(PageRequest.of(page, size, ordering));
        return new PageResponse<>(result.getContent().stream().map(mapper::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public MenuItemResponse getMenuItem(Long id) { return mapper.toResponse(requireItem(id)); }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        adminAccessProvider.requireMenuWriteAccess();
        String name = request.name().trim();
        if (itemRepository.existsByNameIgnoreCase(name)) throw new DuplicateResourceException("Menu item already exists: " + name);
        requirePackages(request.packageIds());
        MenuItem item = new MenuItem(requireCategory(request.categoryId()), name, clean(request.description()),
                request.available(), clean(request.imageUrl()), request.packageIds());
        return mapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuItem item = requireItem(id);
        String name = request.name().trim();
        if (itemRepository.existsByNameIgnoreCaseAndIdNot(name, id)) throw new DuplicateResourceException("Menu item already exists: " + name);
        requirePackages(request.packageIds());
        item.setCategory(requireCategory(request.categoryId()));
        item.setName(name);
        item.setDescription(clean(request.description()));
        item.setAvailable(request.available());
        item.setImageUrl(clean(request.imageUrl()));
        item.setPackageIds(request.packageIds());
        return mapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuItem item = requireItem(id);
        if (orderItemRepository.existsByMenuItemId(id)) {
            throw new BusinessRuleException(UserFacingMessages.MENU_ITEM_HAS_ORDER_HISTORY);
        }
        itemRepository.delete(item);
    }

    private MenuCategory requireCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu category not found with id: " + id));
    }

    private MenuItem requireItem(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
    }

    private void requirePackages(Set<Long> packageIds) {
        Set<Long> foundIds = packageRepository.findAllById(packageIds).stream()
                .map(packageEntry -> packageEntry.getId())
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> missingIds = new TreeSet<>(packageIds);
        missingIds.removeAll(foundIds);
        if (!missingIds.isEmpty()) {
            throw new BusinessRuleException("Buffet packages not found: " + missingIds);
        }
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
