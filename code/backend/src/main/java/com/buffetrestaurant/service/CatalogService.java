package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.dto.request.BuffetPackageRequest;
import com.buffetrestaurant.dto.request.SoupRequest;
import com.buffetrestaurant.dto.response.BuffetPackageResponse;
import com.buffetrestaurant.dto.response.SoupResponse;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.DuplicateResourceException;
import com.buffetrestaurant.repository.DiningSessionRepository;
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
    private final DiningSessionRepository sessions;
    private final MasterDataRemovalAccessProvider removalAccess;

    public CatalogService(BuffetPackageRepository packageRepository,
                          SoupRepository soupRepository,
                          CatalogMapper mapper, DiningSessionRepository sessions,
                          MasterDataRemovalAccessProvider removalAccess) {
        this.packageRepository = packageRepository;
        this.soupRepository = soupRepository;
        this.mapper = mapper;
        this.sessions = sessions;
        this.removalAccess = removalAccess;
    }

    public List<BuffetPackageResponse> getPackages(Boolean active) {
        List<BuffetPackage> packages = active == null
                ? packageRepository.findAll()
                : packageRepository.findByActive(active);
        return packages.stream().filter(p -> !p.isArchived()).map(mapper::toResponse).toList();
    }

    public BuffetPackageResponse getPackage(Long id) {
        return mapper.toResponse(requireOperational(findPackage(id)));
    }

    @Transactional
    public BuffetPackageResponse createPackage(BuffetPackageRequest request) {
        BuffetPackage buffetPackage = new BuffetPackage(
                request.name(), request.price(), request.description());
        return mapper.toResponse(packageRepository.save(buffetPackage));
    }

    @Transactional
    public BuffetPackageResponse updatePackage(Long id, BuffetPackageRequest request) {
        BuffetPackage buffetPackage = requireOperational(lockPackage(id));
        buffetPackage.update(request.name(), request.price(), request.description());
        return mapper.toResponse(buffetPackage);
    }

    @Transactional
    public BuffetPackageResponse setPackageActive(Long id, boolean active) {
        BuffetPackage buffetPackage = requireOperational(lockPackage(id));
        buffetPackage.setActive(active);
        return mapper.toResponse(buffetPackage);
    }

    @Transactional
    public void removePackage(Long id) {
        removalAccess.requireManagerAccess();
        BuffetPackage item = lockPackage(id);
        if (item.isArchived()) return;
        if (sessions.existsByBuffetPackageId(id) || packageRepository.existsMenuLinks(id)) item.archive();
        else packageRepository.delete(item);
        packageRepository.flush();
    }

    public List<BuffetPackageResponse> getArchivedPackages() {
        removalAccess.requireManagerAccess();
        return packageRepository.findByArchived(true).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public BuffetPackageResponse restorePackage(Long id) {
        removalAccess.requireManagerAccess();
        BuffetPackage item = lockPackage(id);
        item.restore();
        return mapper.toResponse(item);
    }

    public List<SoupResponse> getSoups(Boolean active) {
        List<Soup> soups = active == null
                ? soupRepository.findAll()
                : soupRepository.findByActive(active);
        return soups.stream().filter(s -> !s.isArchived()).map(mapper::toResponse).toList();
    }

    public SoupResponse getSoup(Long id) {
        return mapper.toResponse(requireOperational(findSoup(id)));
    }

    @Transactional
    public SoupResponse createSoup(SoupRequest request) {
        return mapper.toResponse(soupRepository.save(new Soup(request.name())));
    }

    @Transactional
    public SoupResponse updateSoup(Long id, SoupRequest request) {
        Soup soup = requireOperational(lockSoup(id));
        soup.setName(request.name());
        return mapper.toResponse(soup);
    }

    @Transactional
    public SoupResponse setSoupActive(Long id, boolean active) {
        Soup soup = requireOperational(lockSoup(id));
        soup.setActive(active);
        return mapper.toResponse(soup);
    }

    @Transactional
    public void removeSoup(Long id) {
        removalAccess.requireManagerAccess();
        Soup item = lockSoup(id);
        if (item.isArchived()) return;
        if (sessions.existsBySoupId(id)) item.archive();
        else soupRepository.delete(item);
        soupRepository.flush();
    }

    public List<SoupResponse> getArchivedSoups() {
        removalAccess.requireManagerAccess();
        return soupRepository.findByArchived(true).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public SoupResponse restoreSoup(Long id) {
        removalAccess.requireManagerAccess();
        Soup item = lockSoup(id);
        item.restore();
        return mapper.toResponse(item);
    }

    private BuffetPackage lockPackage(Long id) {
        return packageRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buffet package not found with id: " + id));
    }

    private Soup lockSoup(Long id) {
        return soupRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Soup not found with id: " + id));
    }

    private BuffetPackage requireOperational(BuffetPackage item) {
        if (item.isArchived()) throw new DuplicateResourceException("รายการอยู่ในรายการเก็บออก กรุณาคืนรายการก่อนใช้งาน");
        return item;
    }

    private Soup requireOperational(Soup item) {
        if (item.isArchived()) throw new DuplicateResourceException("รายการอยู่ในรายการเก็บออก กรุณาคืนรายการก่อนใช้งาน");
        return item;
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
