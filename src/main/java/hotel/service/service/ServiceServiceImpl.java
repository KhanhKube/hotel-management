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
    public List<Service> getServicesByHotel(Long hotelId) {
        return serviceRepository.findByHotel_Id(hotelId);
    }

    @Override
    public Page<Service> getServicesByHotelWithPagination(Long hotelId, Pageable pageable) {
        return serviceRepository.findByHotel_Id(hotelId, pageable);
    }

    @Override
    public Page<Service> searchServicesByName(Long hotelId, String name, Pageable pageable) {
        return serviceRepository.findByHotel_IdAndServiceNameContainingIgnoreCase(hotelId, name, pageable);
    }

    @Override
    public Page<Service> filterServicesByPrice(Long hotelId, Double minPrice, Double maxPrice, Pageable pageable) {
        return serviceRepository.findByHotel_IdAndPriceBetween(hotelId, minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Service> getServicesWithFilters(Long hotelId, String name, Double minPrice, Double maxPrice, Pageable pageable) {
        return serviceRepository.findServicesWithFilters(hotelId, name, minPrice, maxPrice, pageable);
    }

    @Override
    public Service getById(Integer id) {
        return serviceRepository.findById(id).orElse(null);
    }

    @Override
    public Service save(Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public void deleteById(Integer id) {
        serviceRepository.deleteById(id);
    }
}
