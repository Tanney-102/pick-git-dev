package com.woowacourse.pickgit.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.woowacourse.pickgit.authentication.domain.user.AppUser;
import com.woowacourse.pickgit.authentication.domain.user.GuestUser;
import com.woowacourse.pickgit.user.UserFactory;
import com.woowacourse.pickgit.user.application.dto.AuthUserServiceDto;
import com.woowacourse.pickgit.user.application.dto.FollowServiceDto;
import com.woowacourse.pickgit.user.application.dto.UserProfileServiceDto;
import com.woowacourse.pickgit.user.domain.UserRepository;
import com.woowacourse.pickgit.exception.user.DuplicateFollowException;
import com.woowacourse.pickgit.exception.user.InvalidFollowException;
import com.woowacourse.pickgit.exception.user.InvalidUserException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceMockTest {

    private static final String NAME = "yjksw";
    private static final String IMAGE = "http://img.com";
    private static final String DESCRIPTION = "The Best";
    private static final String GITHUB_URL = "https://github.com/yjksw";
    private static final String COMPANY = "woowacourse";
    private static final String LOCATION = "Seoul";
    private static final String WEBSITE = "www.pick-git.com";
    private static final String TWITTER = "pick-git twitter";

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private UserFactory userFactory;

    @BeforeEach
    void setUp() {
        this.userFactory = new UserFactory();
    }

    @DisplayName("????????? ????????? ????????? ??????????????? ????????????.")
    @Test
    void name() {

    }

    @DisplayName("?????????????????? ????????? User ???????????? ????????? ????????? ??????????????? ????????????.")
    @Test
    void getUserProfile_FindUserInfoByName_Success() {
        //given
        AppUser appUser = new GuestUser();
        given(
            userRepository.findByBasicProfile_Name(anyString())
        ).willReturn(Optional.of(userFactory.user()));

        UserProfileServiceDto expectedUserProfileDto = new UserProfileServiceDto(
            NAME, IMAGE, DESCRIPTION,
            0, 0, 0,
            GITHUB_URL, COMPANY, LOCATION, WEBSITE, TWITTER, null
        );

        //when
        UserProfileServiceDto actualUserProfileDto = userService.getUserProfile(appUser, NAME);

        //then
        assertThat(actualUserProfileDto)
            .usingRecursiveComparison()
            .isEqualTo(expectedUserProfileDto);

        verify(userRepository, times(1)).findByBasicProfile_Name(anyString());
    }

    @DisplayName("???????????? ?????? ?????? ???????????? ????????? ????????? ????????? ????????????.")
    @Test
    void getUserProfile_FindUserInfoByInvalidName_Success() {
        //given
        AppUser appUser = new GuestUser();
        //when
        //then
        assertThatThrownBy(
            () -> userService.getUserProfile(appUser, "InvalidName")
        ).hasMessage(new InvalidUserException().getMessage());

        verify(userRepository, times(1)).findByBasicProfile_Name(anyString());
    }

    @DisplayName("Source ????????? Target ????????? follow ?????? ????????????.")
    @Test
    void followUser_ValidUser_Success() {
        //given
        AuthUserServiceDto authUserServiceDto = new AuthUserServiceDto(NAME);
        String targetName = "target";

        given(
            userRepository.findByBasicProfile_Name(NAME)
        ).willReturn(Optional.of(userFactory.user()));

        given(
            userRepository.findByBasicProfile_Name("target")
        ).willReturn(Optional.of(userFactory.anotherUser()));

        //when
        FollowServiceDto followServiceDto = userService.followUser(authUserServiceDto, targetName);

        //then
        assertThat(followServiceDto.getFollowerCount()).isEqualTo(1);
        assertThat(followServiceDto.isFollowing()).isTrue();

        verify(userRepository, times(2)).findByBasicProfile_Name(anyString());
    }

    @DisplayName("?????? ???????????? Follow ?????? ??? ????????? ????????????.")
    @Test
    void followUser_ExistingFollow_ExceptionThrown() {
        //given
        AuthUserServiceDto authUserServiceDto = new AuthUserServiceDto(NAME);
        String targetName = "target";

        given(
            userRepository.findByBasicProfile_Name(NAME)
        ).willReturn(Optional.of(userFactory.user()));

        given(
            userRepository.findByBasicProfile_Name("target")
        ).willReturn(Optional.of(userFactory.anotherUser()));

        userService.followUser(authUserServiceDto, targetName);

        //when
        //then
        assertThatThrownBy(
            () -> userService.followUser(authUserServiceDto, targetName)
        ).hasMessage(new DuplicateFollowException().getMessage());

        verify(userRepository, times(4)).findByBasicProfile_Name(anyString());
    }

    @DisplayName("Source ????????? Target ????????? unfollow ?????? ????????????.")
    @Test
    void unfollowUser_ValidUser_Success() {
        //given
        AuthUserServiceDto authUserServiceDto = new AuthUserServiceDto(NAME);
        String targetName = "target";

        given(
            userRepository.findByBasicProfile_Name(NAME)
        ).willReturn(Optional.of(userFactory.user()));

        given(
            userRepository.findByBasicProfile_Name("target")
        ).willReturn(Optional.of(userFactory.anotherUser()));

        userService.followUser(authUserServiceDto, targetName);

        //when
        FollowServiceDto followServiceDto = userService
            .unfollowUser(authUserServiceDto, targetName);

        //then
        assertThat(followServiceDto.getFollowerCount()).isEqualTo(0);
        assertThat(followServiceDto.isFollowing()).isFalse();

        verify(userRepository, times(4)).findByBasicProfile_Name(anyString());
    }

    @DisplayName("???????????? ?????? Follow ????????? unfollow ?????? ????????? ????????????.")
    @Test
    void unfollowUser_NotExistingFollow_ExceptionThrown() {
        //given
        AuthUserServiceDto authUserServiceDto = new AuthUserServiceDto(NAME);
        String targetName = "target";

        given(
            userRepository.findByBasicProfile_Name(NAME)
        ).willReturn(Optional.of(userFactory.user()));

        given(
            userRepository.findByBasicProfile_Name("target")
        ).willReturn(Optional.of(userFactory.anotherUser()));

        //when
        //then
        assertThatThrownBy(
            () -> userService.unfollowUser(authUserServiceDto, targetName)
        ).hasMessage(new InvalidFollowException().getMessage());

        verify(userRepository, times(2)).findByBasicProfile_Name(anyString());
    }
}
