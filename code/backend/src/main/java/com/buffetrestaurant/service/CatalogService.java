package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.dto.request.BuffetPackageRequest;
import com.buffetrestaurant.dto.request.SoupRequest;
import com.buffetrestaurant.dto.response.BuffetPackageResponse;
import com.buffetrestaurant.dto.response.SoupResponse;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.mapper.CatalogMapper;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.SoupRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final BuffetPackageRepository packageRepository;
    private final SoupRepository soupRepository;
    private final CatalogMapper mapper;

    public CatalogService(BuffetPackageRepository packageRepository,
                          SoupRepository soupRepository,
                          CatalogMapper mapper) {
        this.packageRepository = packageRepository;
        this.soupRepository = soupRepository;
        this.mapper = mapper;
    }

    public List<BuffetPackageResponse> getPackages(Boolean active) {
        List<BuffetPackage> packages = active == null
                ? packageRepository.findAll()
                : packageRepository.findByActive(active);
        return packages.stream().map(mapper::toResponse).toList();
    }

    public BuffetPackageResponse getPackage(Long id) {
        return mapper.toResponse(findPackage(id));
    }

    @Transactional
    public BuffetPackageResponse createPackage(BuffetPackageRequest request) {
        BuffetPackage buffetPackage = new BuffetPackage(
                request.name(), request.price(), request.description());
        return mapper.toResponse(packageRepository.save(buffetPackage));
    }

    @Transactional
    public BuffetPackageResponse updatePackage(Long id, BuffetPackageRequest request) {
        BuffetPackage buffetPackage = findPackage(id);
        buffetPackage.update(request.name(), request.price(), request.description());
        return mapper.toResponse(buffetPackage);
    }

    @Transactional
    public BuffetPackageResponse setPackageActive(Long id, boolean active) {
        BuffetPackage buffetPackage = findPackage(id);
        buffetPackage.setActive(active);
        return mapper.toResponse(buffetPackage);
    }

    @Transactional
    public void disablePackage(Long id) {
        findPackage(id).setActive(false);
    }

    public List<SoupResponse> getSoups(Boolean active) {
        List<Soup> soups = active == null
                ? soupRepository.findAll()
                : soupRepository.findByActive(active);
        return soups.stream().map(mapper::toResponse).toList();
    }

    public SoupResponse getSoup(Long id) {
        return mapper.toResponse(findSoup(id));
    }

    @Transactional
    public SoupResponse createSoup(SoupRequest request) {
        return mapper.toResponse(soupRepository.save(new Soup(request.name())));
    }

    @Transactional
    public SoupResponse updateSoup(Long id, SoupRequest request) {
        Soup soup = findSoup(id);
        soup.setName(request.name());
        return mapper.toResponse(soup);
    }

    @Transactional
    public SoupResponse setSoupActive(Long id, boolean active) {
        Soup soup = findSoup(id);
        soup.setActive(active);
        return mapper.toResponse(soup);
    }

    @Transactional
    public void disableSoup(Long id) {
        findSoup(id).setActive(false);
    }

    private BuffetPackage findPackage(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Buffet package not found with id: " + id));
    }

    private Soup findSoup(Long id) {
        return soupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Soup not found with id: " + id));
    }
}