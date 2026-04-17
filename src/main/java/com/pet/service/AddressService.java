package com.pet.service;

import com.pet.dto.AddressCreateDTO;
import com.pet.dto.AddressUpdateDTO;
import com.pet.vo.AddressVO;

import java.util.List;

public interface AddressService {
    
    List<AddressVO> getAddressList(Long userId);
    
    AddressVO getAddressDetail(Long userId, Long addressId);
    
    AddressVO addAddress(Long userId, AddressCreateDTO createDTO);
    
    AddressVO updateAddress(Long userId, Long addressId, AddressUpdateDTO updateDTO);
    
    void deleteAddress(Long userId, Long addressId);
    
    void setDefaultAddress(Long userId, Long addressId);
}
