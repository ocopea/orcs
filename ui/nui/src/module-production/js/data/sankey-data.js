// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import WizardMainStore from '../stores/wizard/_wizard-main-store.js';

var selectedAppName;

if(selectedAppName == undefined){
    selectedAppName = "";
}else{
    selectedAppName = WizardMainStore.selectedApp.name;
}

var SankeyData = [
               [ selectedAppName, 'MongoDB', 22 ],
               [ selectedAppName, 'Logs', 7 ],
               [ 'MongoDB', 'Primary Storage', 2 ],
               [ 'MongoDB', 'Offline Backup', 13 ],
               [ 'MongoDB', 'Test/Dev', 3 ],
               [ 'MongoDB', 'Analytics', 4 ],
               [ 'Logs', 'Analytics', 3 ],
               [ 'Logs', 'Primary Storage', 2 ],
                 [ 'Logs', 'Test/Dev', 2 ],
                 [ 'Offline Backup', 'DDVE', 5 ],
                 [ 'Offline Backup', 'Amazon Glacier', 8 ],
                 [ 'Primary Storage', 'Amazon - EBS', 2 ],
                 [ 'Primary Storage', 'Amazon - S3', 2 ],
                 [ 'Test/Dev', 'CF On Prem - ScaleIO', 2 ],
                 [ 'Test/Dev', 'CF On Prem - EC2', 3 ],
                 [ 'Analytics', 'Amazon Redshift',  7]
             ];

export default SankeyData;