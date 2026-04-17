package com.pet.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class AddressCreateDTO {
    
    @NotBlank(message = "收货人不能为空")
    private String receiverName;
    
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    @NotBlank(message = "省份不能为空")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "区县不能为空")
    private String district;
    
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
    
    private String zipCode;
    
    private Integer isDefault = 0;
}
