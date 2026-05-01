package com.beyond.university.auth.model.mapper;

import com.beyond.university.auth.model.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    User selectUserByUsername(@Param("username") String username);
}
