package com.pet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("address")
public class Address extends BaseEntity {
    
    private Long userId;
    
    private String receiverName;
    
    private String phone;
    
    private String province;
    
    private String city;
    
    private String district;
    
    private String detailAddress;
    
    private String zipCode;
    
    private Integer isDefault;
}
