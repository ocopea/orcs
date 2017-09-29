// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
export class Quota {
  orgId;
  dsbQuota;
  psbQuota;

  constructor(quota) {
    this.orgId =    quota.orgId;
    this.psbQuota = quota.psbQuota;
    this.dsbQuota = this.getDsbQuotas(quota.dsbQuota)
  }

  getDsbQuotas(dsbQuotas) {
    return _.map(dsbQuotas, (value, key) => {
      return new DsbQuota({name: key, value: value});
    });
  }
}

class DsbQuota {
  name;
  value;
  constructor(dsbQuota) {
    this.name = dsbQuota.name;
    this.value = dsbQuota.value;
  }
}
