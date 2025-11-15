package com.spring.DeliveryApp.auth.Dto.Mapper;

import com.spring.DeliveryApp.auth.Dto.UserRequestDto;
import com.spring.DeliveryApp.auth.Dto.UserResponseDto;
import com.spring.DeliveryApp.auth.Entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);


    User toUserEntity(UserRequestDto requestDto);

}