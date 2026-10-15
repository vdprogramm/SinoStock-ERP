package com.sinostock.erp.controller;

import com.sinostock.erp.domain.Role;
import com.sinostock.erp.domain.UserAccount;
import com.sinostock.erp.exception.ApiException;
import com.sinostock.erp.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public AdminUserController(UserRepository users, PasswordEncoder encoder) {
        this.users=users; this.encoder=encoder;
    }
    public record UserView(Long id, String username, Role role) {}
    public record CreateUser(@NotBlank String username,
                             @NotBlank @Size(min=12) String password,
                             @NotNull Role role) {}
    public record UpdateRole(@NotNull Role role) {}
    public record ResetPassword(@NotBlank @Size(min=12) String password) {}
    private UserView view(UserAccount u) {return new UserView(u.getId(),u.getUsername(),u.getRole());}
    @GetMapping @Transactional(readOnly=true)
    public List<UserView> list() {return users.findAll().stream().map(this::view).toList();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
    public UserView create(@Valid @RequestBody CreateUser req) {
        String name=req.username().trim();
        if (users.findByUsername(name).isPresent())
            throw new ApiException(HttpStatus.CONFLICT,"Tên đăng nhập đã tồn tại");
        return view(users.save(new UserAccount(name, encoder.encode(req.password()),req.role())));
    }
    @PatchMapping("/{id}/role") @Transactional
    public UserView updateRole(@PathVariable Long id,@Valid @RequestBody UpdateRole req) {
        UserAccount u=users.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy người dùng"));
        if(u.getRole()==Role.ADMIN && req.role()!=Role.ADMIN && users.findAll().stream().filter(x->x.getRole()==Role.ADMIN).count()<=1)
            throw new ApiException(HttpStatus.CONFLICT,"Không thể hạ quyền quản trị viên cuối cùng");
        u.setRole(req.role());
        return view(u);
    }
    @PatchMapping("/{id}/password") @Transactional
    public UserView resetPassword(@PathVariable Long id,@Valid @RequestBody ResetPassword req) {
        UserAccount u=users.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Không tìm thấy người dùng"));
        u.changePassword(encoder.encode(req.password()));
        return view(u);
    }
}
