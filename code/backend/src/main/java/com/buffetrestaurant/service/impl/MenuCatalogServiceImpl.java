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
import com.buffetrestaurant.exception.MenuConflictException;
import static com.buffetrestaurant.exception.MenuConflictException.Reason.*;
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
    private final com.buffetrestaurant.service.MenuStockUsageService stockUsage;

    public MenuCatalogServiceImpl(MenuAdminAccessProvider adminAccessProvider,
                                  MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository,
                                  BuffetPackageRepository packageRepository, OrderItemRepository orderItemRepository,
                                  OrderingMapper mapper, com.buffetrestaurant.service.MenuStockUsageService stockUsage) {
        this.stockUsage = stockUsage;
        this.adminAccessProvider = adminAccessProvider;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.packageRepository = packageRepository;
        this.orderItemRepository = orderItemRepository;
        this.mapper = mapper;
    }

    public List<MenuCategoryResponse> getCategories() {
        return categoryRepository.findByArchivedAtIsNull(Sort.by("name").and(Sort.by("id"))).stream().map(mapper::toResponse).toList();
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
        MenuCategory category = lockCategory(id);
        requireWorking(category);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) throw new DuplicateResourceException("Menu category already exists: " + name);
        category.setName(name);
        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuCategory category = lockCategory(id);
        if (category.isArchived()) return;
        if (itemRepository.existsByCategoryIdAndArchivedAtIsNull(id)) throw new MenuConflictException(CATEGORY_HAS_ITEMS);
        if (itemRepository.existsByCategoryId(id)) category.archive();
        else categoryRepository.delete(category);
        categoryRepository.flush();
    }

    public PageResponse<MenuItemResponse> getMenuItems(int page, int size, String sort) {
        return pageResponse(itemRepository.findByArchivedAtIsNullAndCategoryArchivedAtIsNull(pageRequest(page, size, sort)));
    }

    public List<MenuCategoryResponse> getArchivedCategories() {
        adminAccessProvider.requireMenuWriteAccess();
        return categoryRepository.findByArchivedAtIsNotNull(Sort.by("name").and(Sort.by("id"))).stream().map(mapper::toResponse).toList();
    }

    public PageResponse<MenuItemResponse> getArchivedMenuItems(int page, int size, String sort) {
        adminAccessProvider.requireMenuWriteAccess();
        return pageResponse(itemRepository.findByArchivedAtIsNotNull(pageRequest(page, size, sort)));
    }

    @Transactional
    public MenuCategoryResponse restoreCategory(Long id) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuCategory category = lockCategory(id);
        category.restore();
        return mapper.toResponse(category);
    }

    @Transactional
    public MenuItemResponse restoreMenuItem(Long id) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuItem item = lockItem(id, null);
        if (item.isArchived()) {
            requireWorking(item.getCategory());
            item.restore();
        }
        return mapper.toResponse(item);
    }

    private PageRequest pageRequest(int page, int size, String sort) {
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
        return PageRequest.of(page, size, ordering);
    }

    private PageResponse<MenuItemResponse> pageResponse(Page<MenuItem> result) {
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
        MenuCategory category = lockCategory(request.categoryId());
        requireWorking(category);
        MenuItem item = new MenuItem(category, name, clean(request.description()),
                request.available(), clean(request.imageUrl()), request.packageIds());
        stockUsage.configure(item, request);
        return mapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuItem item = lockItem(id, request.categoryId());
        if (item.isArchived()) throw new MenuConflictException(ITEM_ARCHIVED);
        String name = request.name().trim();
        if (itemRepository.existsByNameIgnoreCaseAndIdNot(name, id)) throw new DuplicateResourceException("Menu item already exists: " + name);
        requirePackages(request.packageIds());
        // Already held by lockItem; repeated acquisition takes no new lock.
        MenuCategory category = lockCategory(request.categoryId());
        requireWorking(category);
        item.setCategory(category);
        item.setName(name);
        item.setDescription(clean(request.description()));
        item.setAvailable(request.available());
        item.setImageUrl(clean(request.imageUrl()));
        item.setPackageIds(request.packageIds());
        stockUsage.configure(item, request);
        return mapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        adminAccessProvider.requireMenuWriteAccess();
        MenuItem item = lockItem(id, null);
        if (item.isArchived()) return;
        // Membership is also a reference: never silently cascade-delete package configuration.
        if (orderItemRepository.existsByMenuItemId(id) || !item.getPackageIds().isEmpty()) item.archive();
        else itemRepository.delete(item);
        // A racing/external FK remains authoritative: a failure rolls back all writes.
        itemRepository.flush();
    }

    private MenuCategory requireCategory(Long id) {
        MenuCategory category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu category not found with id: " + id));
        if (category.isArchived()) throw new ResourceNotFoundException("Menu category not found with id: " + id);
        return category;
    }

    private MenuItem requireItem(Long id) {
        return itemRepository.findById(id).filter(item -> !item.isArchived() && !item.getCategory().isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
    }

    private MenuCategory lockCategory(Long id) {
        return categoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found with id: " + id));
    }

    private void requireWorking(MenuCategory category) {
        if (category.isArchived()) throw new MenuConflictException(CATEGORY_ARCHIVED);
    }

    private MenuItem lockItem(Long id, Long targetCategoryId) {
        Long currentCategoryId = itemRepository.findCategoryId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        Set<Long> categoryIds = new TreeSet<>();
        categoryIds.add(currentCategoryId);
        if (targetCategoryId != null) categoryIds.add(targetCategoryId);
        // Parent locks precede item locks; moving items locks both parents in ID order.
        for (Long categoryId : categoryIds) lockCategory(categoryId);
        MenuItem item = itemRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        if (!item.getCategory().getId().equals(currentCategoryId)) throw new MenuConflictException(CATEGORY_CHANGED);
        return item;
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
