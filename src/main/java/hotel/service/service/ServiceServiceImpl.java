package hotel.service.service;


import hotel.db.entity.Service;
import hotel.db.repository.service.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    @Override
    public List<Service> findAll() {
        return serviceRepository.findByIsDeletedFalse();
    }

    @Override
    public Page<Service> findAll(Pageable pageable) {
        return serviceRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    public Service findById(Integer id) {
        return serviceRepository.findById(id)
                .filter(service -> !service.getIsDeleted())
                .orElse(null);
    }

    @Override
    public Service save(Service service) {
        if (service.getServiceId() == null) {
            service.setIsDeleted(false);
        }
        return serviceRepository.save(service);
    }

    @Override
    public void deleteById(Integer id) {
        Service service = serviceRepository.findById(id).orElse(null);
        if (service != null) {
            service.setIsDeleted(true);
            serviceRepository.save(service);
        }
    }

    @Override
    public Page<Service> searchByName(String name, Pageable pageable) {
        return serviceRepository.findByServiceNameContainingIgnoreCaseAndIsDeletedFalse(name, pageable);
    }

    @Override
    public Page<Service> filterByPrice(Double minPrice, Double maxPrice, Pageable pageable) {
        return serviceRepository.findByPriceBetweenAndIsDeletedFalse(minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Service> findWithFilters(String name, Double minPrice, Double maxPrice, Pageable pageable) {
        return serviceRepository.findServicesWithFilters(name, minPrice, maxPrice, pageable);
    }

    @Override
    public boolean existsByName(String serviceName) {
        return serviceRepository.findByIsDeletedFalse().stream()
                .anyMatch(service -> service.getServiceName().equalsIgnoreCase(serviceName));
    }

    @Override
    public List<Service> findByIds(List<Integer> serviceIds) {
        return serviceRepository.findAllById(serviceIds).stream()
                .filter(service -> !service.getIsDeleted())
                .toList();
    }

    @Override
    public List<Service> findFeaturedServices() {
        return serviceRepository.findByIsFeaturedTrueAndIsDeletedFalse();
    }

    @Override
    public List<Service> findActiveServices() {
        return serviceRepository.findByStatusAndIsDeletedFalse("ACTIVE");
    }
}
