package com.knapsack.service;

import com.knapsack.model.Address;
import com.knapsack.model.User;
import com.knapsack.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUser_UserId(user.getUserId());
    }

    public Optional<Address> getAddressById(Integer addressId) {
        return addressRepository.findById(addressId);
    }

    public Address saveAddress(User user, String fullAddress, String city, String state, String pincode, Boolean isDefault) {
        if (Boolean.TRUE.equals(isDefault)) {
            List<Address> existing = getUserAddresses(user);
            for (Address addr : existing) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }

        Address address = new Address(user, fullAddress, city, state, pincode, isDefault);
        return addressRepository.save(address);
    }

    public void deleteAddress(Integer addressId) {
        addressRepository.deleteById(addressId);
    }
}
