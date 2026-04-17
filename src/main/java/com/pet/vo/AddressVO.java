package com.pet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressVO {
    
    private Long id;
    
    private Long userId;
    
    private String receiverName;
    
    private String phone;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String detailAddress;
    
    private String zipCode;
    
    private Integer isDefault;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
