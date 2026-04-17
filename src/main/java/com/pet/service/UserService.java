package com.pet.service;

import com.pet.dto.PasswordChangeDTO;
import com.pet.dto.UserLoginDTO;
import com.pet.dto.UserRegisterDTO;
import com.pet.dto.UserUpdateDTO;
import com.pet.vo.LoginVO;
import com.pet.vo.UserVO;

public interface UserService {
    
    LoginVO register(UserRegisterDTO registerDTO);
    
    LoginVO login(UserLoginDTO loginDTO);
    
    UserVO getUserInfo(Long userId);
    
    UserVO updateUserInfo(Long userId, UserUpdateDTO updateDTO);
    
    void changePassword(Long userId, PasswordChangeDTO passwordDTO);
}
