package com.pet.service.impl;

import com.pet.common.BusinessException;
import com.pet.dto.AddressCreateDTO;
import com.pet.dto.AddressUpdateDTO;
import com.pet.entity.Address;
import com.pet.mapper.AddressMapper;
import com.pet.service.AddressService;
import com.pet.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    
    private final AddressMapper addressMapper;
    
    @Override
    public List<AddressVO> getAddressList(Long userId) {
        List<Address> addresses = addressMapper.selectByUserId(userId);
        return addresses.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AddressVO getAddressDetail(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        return convertToVO(address);
    }
    
    @Override
    @Transactional
    public AddressVO addAddress(Long userId, AddressCreateDTO createDTO) {
        // 如果设置的是默认地址，先将其他地址设为非默认
        if (createDTO.getIsDefault() != null && createDTO.getIsDefault() == 1) {
            List<Address> addresses = addressMapper.selectByUserId(userId);
            for (Address addr : addresses) {
                addr.setIsDefault(0);
                addressMapper.updateById(addr);
            }
        }
        
        Address address = new Address();
        BeanUtils.copyProperties(createDTO, address);
        address.setUserId(userId);
        addressMapper.insert(address);
        
        return convertToVO(address);
    }
    
    @Override
    @Transactional
    public AddressVO updateAddress(Long userId, Long addressId, AddressUpdateDTO updateDTO) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        
        // 如果设置的是默认地址，先将其他地址设为非默认
        if (updateDTO.getIsDefault() != null && updateDTO.getIsDefault() == 1) {
            List<Address> addresses = addressMapper.selectByUserId(userId);
            for (Address addr : addresses) {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(0);
                    addressMapper.updateById(addr);
                }
            }
            address.setIsDefault(1);
        }
        
        BeanUtils.copyProperties(updateDTO, address);
        addressMapper.updateById(address);
        
        return convertToVO(address);
    }
    
    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        addressMapper.deleteById(addressId);
    }
    
    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        
        // 先将所有地址设为非默认
        List<Address> addresses = addressMapper.selectByUserId(userId);
        for (Address addr : addresses) {
            addr.setIsDefault(0);
            addressMapper.updateById(addr);
        }
        
        // 再将指定地址设为默认
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }
    
    private AddressVO convertToVO(Address address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }
}
