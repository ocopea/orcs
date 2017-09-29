// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import DataStore from '../../stores/data-store';
import UiStore from '../../stores/ui-store';
import { User } from '../../models/User/user-model';
import Request from '../../transportLayer';
import userApi from './user-api';
import mockUsers from './mock/mockUsers';
import mockLoggedInUser from './mock/mockLoggedInUser';


export default class UserService {

  static fetchUsers(url) {
     Request(url, {method: 'GET'}, response => {
       if(Array.isArray(response)){
         const users = response.map(user => {
           return new User(user);
         });
         DataStore.receiveUsers(users);
       }
     }, error => { console.log(error) })

//    const users = mockUsers.map(user => {
//      return new User(user);
//    });
//    DataStore.receiveUsers(users);
  }

  static fetchLoggedInUser(url) {
    // Request(url, {method: 'GET'}, response => {
    //   UiStore.receiveLoggedInUser(new User(response));
    // }, error => { console.log(error) })
    UiStore.receiveLoggedInUser(new User(mockLoggedInUser));
  }
}
