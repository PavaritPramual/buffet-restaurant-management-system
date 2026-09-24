package com.buffetrestaurant.mapper;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.dto.response.BuffetPackageResponse;
import com.buffetrestaurant.dto.response.SoupResponse;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {
    public BuffetPackageResponse toResponse(BuffetPackage buffetPackage) {
        return new BuffetPackageResponse(
                buffetPackage.getId(),
                buffetPackage.getName(),
                buffetPackage.getPrice(),
                buffetPackage.getDescription(),
                buffetPackage.isActive()
        );
    }

    public SoupResponse toResponse(Soup soup) {
        return new SoupResponse(soup.getId(), soup.getName(), soup.isActive());
    }
}