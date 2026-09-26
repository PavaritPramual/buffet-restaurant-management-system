package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.MenuCategoryRequest;
import com.buffetrestaurant.dto.request.MenuItemRequest;
import com.buffetrestaurant.dto.response.MenuCategoryResponse;
import com.buffetrestaurant.dto.response.MenuItemResponse;
import com.buffetrestaurant.dto.response.PageResponse;
import java.util.List;

public interface MenuCatalogService {
    List<MenuCategoryResponse> getCategories();
    MenuCategoryResponse getCategory(Long id);
    MenuCategoryResponse createCategory(MenuCategoryRequest request);
    MenuCategoryResponse updateCategory(Long id, MenuCategoryRequest request);
    void deleteCategory(Long id);
    PageResponse<MenuItemResponse> getMenuItems(int page, int size, String sort);
    MenuItemResponse getMenuItem(Long id);
    MenuItemResponse createMenuItem(MenuItemRequest request);
    MenuItemResponse updateMenuItem(Long id, MenuItemRequest request);
    void deleteMenuItem(Long id);
}
