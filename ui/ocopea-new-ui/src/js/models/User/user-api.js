// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const userApi = {
  users:          `${APISERVER}/hub-web-api/user`,
  loggedInUser:   `${APISERVER}/hub-web-api/logged-in-user`,
  avatar:         id => { return `${APISERVER}/hub-web-api/user/${id}/avatar` }
}

export default userApi;
