// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { autorun } from 'mobx';
import userApi from './user-api';
import userService from './user-service';
import DataStore from '../../stores/data-store';

export default class UserHandler {

  static fetchUsers() {
    userService.fetchUsers(userApi.users);
  }

  static fetchLoggedInUser() {
    userService.fetchLoggedInUser(userApi.loggedInUser);
  }

  static fetchLoggedInUserAvatar() {
    userService.fetchLoggedInUserAvatar(userApi.avatar);
  }

  static getUserAvatarURL(id) {
    return `${APISERVER}/hub-web-api/user/${id}/avatar`;
  }

  static getUserById(id) {
    return DataStore.usersMap[id];
  }
}
