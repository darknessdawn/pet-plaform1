package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    
    @Select("SELECT * FROM address WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, create_time DESC")
    List<Address> selectByUserId(Long userId);
    
    @Select("SELECT * FROM address WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    Address selectDefaultByUserId(Long userId);
    
    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefaultByUserId(Long userId);
}
