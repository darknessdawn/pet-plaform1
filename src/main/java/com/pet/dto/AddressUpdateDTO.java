package com.pet.dto;

import lombok.Data;

@Data
public class AddressUpdateDTO {
    
    private String receiverName;
    
    private String phone;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String detailAddress;
    
    private String zipCode;
    
    private Integer isDefault;
}
