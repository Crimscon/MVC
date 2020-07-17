package com.project.MVC.service;

import com.project.MVC.model.User;
import com.project.MVC.model.UserProfile;
import com.project.MVC.model.enums.Role;
import com.project.MVC.model.enums.Sex;
import com.project.MVC.repository.UserProfileRepository;
import com.project.MVC.repository.UserRepository;
import com.project.MVC.util.ThumbnailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;

    private final UserProfileRepository userProfileRepo;

    @Value("${upload.path}")
    private String uploadPath;

    public UserService(UserRepository userRepo, UserProfileRepository userProfileRepo) {
        this.userRepo = userRepo;
        this.userProfileRepo = userProfileRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsernameIgnoreCase(username);
    }

    public User findById(Long aLong) {
        return userRepo.getOne(aLong);
    }

    public List<User> getUserList(String filter) {
        List<User> users;

        if (filter != null && !filter.isEmpty()) {
            users = new ArrayList<>();

            User user = (User) loadUserByUsername(filter);
            if (user != null) users.add(user);
        } else {
            users = userRepo.findAll();
        }

        return users;
    }

    public void saveUser(String username, String password,
                         Map<String, String> form, Long userId) {
        User user = findById(userId);
        user.setUsername(username);
        user.setPassword(password);

        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        user.getRoles().clear();

        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }

        userRepo.save(user);
    }

    public void saveUser(User user, String password,
                         MultipartFile file,
                         Sex gender, String phoneNumber, String dateOfBirth) throws IOException {
        UserProfile userProfile = user.getUserProfile() == null ?
                new UserProfile() : userProfileRepo.getOne(user.getUserProfile().getId());

        boolean passwordChange = false,
                profilePicChange = false,
                phoneChange = false,
                dofChange = false,
                profileChange = false;

        if (!user.getPassword().equals(password)) {
            passwordChange = true;
            user.setPassword(password);
        }

        if (!dateOfBirth.equals("")) {
            String[] dateArr = dateOfBirth.split("-");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]) - 1, Integer.parseInt(dateArr[2]));

            if (!userProfile.getDateOfBirth().equals(calendar.getTime())) {
                dofChange = true;
                userProfile.setDateOfBirth(calendar.getTime());
            }
        }

        if (!userProfile.getPhoneNumber().equals(phoneNumber)) {
            phoneChange = true;
            userProfile.setPhoneNumber(phoneNumber);
        }

        if (phoneChange || dofChange) {
            profileChange = true;
            userProfile.setUser(user);
        }

        if (profileChange) {
            userProfileRepo.save(userProfile);
            user.setUserProfile(userProfile);
        }

        if (file != null && !file.getOriginalFilename().isEmpty()) {

            ThumbnailUtil.deleteIfExistFile(uploadPath, user.getProfile_pic());
            String filename = ThumbnailUtil.createFile(file, uploadPath, false);

            user.setProfile_pic(filename);
            profilePicChange = true;
        }

        if (passwordChange || profilePicChange || profileChange) userRepo.save(user);
    }


    public List<User> findAll() {
        return userRepo.findAll();
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public void deleteUser(User user) throws IOException {

        UserProfile userProfile = user.getUserProfile();

        if (userProfile != null) {
            userProfileRepo.delete(userProfile);
        }
        if (!(user.getProfile_pic() == null || user.getProfile_pic().isEmpty())) {
            ThumbnailUtil.deleteIfExistFile(uploadPath, user.getProfile_pic());
        }

        userRepo.delete(user);


    }
}
