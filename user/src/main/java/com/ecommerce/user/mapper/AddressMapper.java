package com.ecommerce.user.mapper;


import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.model.Address;
import org.springframework.stereotype.Service;

@Service
public class AddressMapper {
    public Address mapToAddress(AddressDTO address) {
        if(address == null)
            return null;
        return Address.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .zipCode(address.getZipcode())
                .state(address.getState())
                .country(address.getCountry())
                .build();
    }
}
