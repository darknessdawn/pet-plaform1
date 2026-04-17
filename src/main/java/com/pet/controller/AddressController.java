package com.pet.controller;

import com.pet.common.Result;
import com.pet.dto.AddressCreateDTO;
import com.pet.dto.AddressUpdateDTO;
import com.pet.service.AddressService;
import com.pet.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    
    @GetMapping("/list")
    public Result<List<AddressVO>> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AddressVO> list = addressService.getAddressList(userId);
        return Result.success(list);
    }
    
    @GetMapping("/detail/{id}")
    public Result<AddressVO> getAddressDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressVO addressVO = addressService.getAddressDetail(userId, id);
        return Result.success(addressVO);
    }
    
    @PostMapping("/add")
    public Result<AddressVO> addAddress(@Valid @RequestBody AddressCreateDTO createDTO,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressVO addressVO = addressService.addAddress(userId, createDTO);
        return Result.success(addressVO);
    }
    
    @PutMapping("/update/{id}")
    public Result<AddressVO> updateAddress(@PathVariable Long id,
                                           @Valid @RequestBody AddressUpdateDTO updateDTO,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressVO addressVO = addressService.updateAddress(userId, id, updateDTO);
        return Result.success(addressVO);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.deleteAddress(userId, id);
        return Result.success();
    }
    
    @PutMapping("/default/{id}")
    public Result<Void> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.setDefaultAddress(userId, id);
        return Result.success();
    }
}
