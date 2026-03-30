package dujiacun.userservice.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class UserRequestDto {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserRequestDto(String userName,String passWord,Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
